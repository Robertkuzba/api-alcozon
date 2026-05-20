# Integracja zespołu — API

> **Status:** uzgodnione 17.05.2026. Etap 1a (dostawy) + **1b (2FA staff)** wdrożone w backendzie.

**Produkcja:** `https://api-alcozon.onrender.com`  
**Swagger:** `/docs`  
**Health:** `GET /actuator/health`

---

## Weryfikacja aplikacji Android (app-check)

Przed logowaniem mobilka wywołuje (bez JWT):

```http
POST /api/security/app-check
Content-Type: application/json

{
  "platform": "android",
  "packageName": "com.alkozon.app",
  "versionName": "1.0.0",
  "versionCode": 1,
  "signingCertSha256": "<sha256_cert_lowercase_bez_dwukropkow>"
}
```

| Wynik | HTTP |
|--------|------|
| OK | **204** |
| Zły pakiet / SHA / wersja | **403** |
| Brak pola w body | **400** |

Konfiguracja (`application.yml` → `app.security.android`): `package-name`, `min-version-code`, `allowed-signing-cert-sha256` (lista).  
Debug SHA (dev): `e1b17830399a952b8ff905023d5dc98f0a202cbb18941beb06000717341ac7f6` — po release dopisać SHA keystore na Renderze.

**Diagnostyka 403:**

| Odpowiedź | Znaczenie |
|-----------|-----------|
| **204** | OK |
| **400** + JSON `Validation failed` | Endpoint działa, złe body |
| **403** + JSON `App not allowed` | Endpoint działa, zły SHA/pakiet/wersja |
| **403** + **puste body** | Stary deploy lub brak `permitAll` — żądanie nie dociera do app-check |

Na Renderze **nie ustawiaj** pustej zmiennej nadpisującej listę SHA (np. `APP_SECURITY_ANDROID_ALLOWED_SIGNING_CERT_SHA256=`). Wystarczy domyślny YAML z repo.

---

## JWT (logowanie / mobilka)

| Token | TTL (domyślnie) | Uwagi |
|-------|-----------------|--------|
| **Access** (Bearer, `expiresInSeconds` w `TokenResponse`) | **86400 s (24 h)** | Render: można nadpisać `JWT_ACCESS_TTL` (sekundy). Krótki access nie blokuje FCM (push idzie po stronie serwera); warto mieć **`POST /api/auth/refresh`** przy 401. |
| **Refresh** | **30 dni** | `jwt.refresh-ttl` / przyszłościowo osobny env jeśli potrzebny. |

---

## Konta demo

| Rola | E-mail | Hasło |
|------|--------|--------|
| Manager (desktop) | `manager@example.com` | `Manager123!` |
| Pracownik / kurier (mobilka, demo) | `employee@example.com` | `Employee123!` |
| Pracownik / kurier (Michał) | `michal.nocun@studenci.collegiumwitelona.pl` | `Asdasd123!` |
| Klient (demo zamówienia) | `customer@example.com` | `Customer123!` |

### Zamówienia demo (seed przy starcie API, `DemoOrderSeeder`)

Numer klienta (`clientOrderNumber`) **701101–701111** — seed podstawowy, jeśli brak `701101`.  
**701112–701113** — seed Michała (kurier), jeśli brak `701112` (działa też na już zaseedowanej prod DB).

| Nr | Status | Kurier | Adres (skrót) |
|----|--------|--------|---------------|
| 701101 | SUBMITTED | — | ul. Oławska 15 |
| 701102 | IN_PRODUCTION | — | Kazimierza Wielkiego 27 |
| 701103 | IN_PACKING | — | Hubska 52 |
| 701104 | IN_DELIVERY | **tak** | **Cesarzowicka 100, 52-408** |
| 701105 | IN_DELIVERY | **nie** | **Cesarzowicka 100, 52-408** |
| 701106 | IN_DELIVERY | **tak** | Świdnicka 12 |
| 701107 | IN_DELIVERY | **nie** | Legnicka 58 |
| 701108 | IN_DELIVERY | **tak** | Borowska 11 |
| 701109 | IN_DELIVERY | **nie** | Grabiszyńska 241 |
| 701110 | DELIVERED | tak | Klecińska 4 |
| 701111 | CANCELLED | — | Na Grobli 20 |
| **701112** | **IN_DELIVERY** | **nie** | Piłsudskiego 74 — **nowe, bez kuriera** (desktop: assign) |
| **701113** | **IN_DELIVERY** | **`michal.nocun@…`** | Powstańców Śl. 95 — **lista kuriera Michała** |

- Kurier `employee@example.com`: `GET /api/orders/for-courier/{id}` — **701104, 701106, 701108**.
- Kurier **Michał** (`michal.nocun@studenci.collegiumwitelona.pl`): ten sam endpoint z **jego `userId`** — **701113** (po deploy / restarcie API).
- Desktop: `GET /api/deliveries` — wszystkie `IN_DELIVERY`, m.in. **701105, 701107, 701109, 701112** bez kuriera (`courierId` null).

---

## Słownik statusów zamówienia (`OrderStatus`) — sklep **i** custom

| Status API | PL (UI) |
|------------|---------|
| `SUBMITTED` | Zgłoszone / złożone |
| `IN_PRODUCTION` | W produkcji |
| `IN_PACKING` | W pakowaniu |
| `IN_DELIVERY` | W drodze (dostawa) — tworzy `Delivery`, desktop przypisuje kuriera |
| `DELIVERED` | Dostarczone |
| `CANCELLED` | Anulowane |

**Custom:** ten sam enum w `PATCH /api/custom-orders/{id}/status`. Odpowiedź `GET /api/deliveries`: `customOrder: true`, `customOrderId`, `orderId` null.

**Uwaga:** Rekord `Delivery` (sklep lub custom) powstaje przy **`IN_DELIVERY`** (nie przy `IN_PACKING`).

---

## Real-time (STOMP + opcjonalnie FCM)

Endpoint WebSocket: `wss://api-alcozon.onrender.com/ws` (lokalnie `ws://localhost:8080/ws`).  
Przy `CONNECT`: nagłówek STOMP `Authorization: Bearer <accessToken>`.

| Subskrypcja | Kto | Kiedy wysyłane |
|-------------|-----|----------------|
| `/user/queue/order-updates` | CUSTOMER (Web) | Każda zmiana statusu zamówienia klienta |
| `/topic/orders/staff` | EMPLOYEE, MANAGER (mobilka magazyn) | Nowe zamówienie, zmiana statusu, anulowanie, dostarczenie |
| `/topic/orders/dispatch` | **MANAGER** (desktop) | Zamówienie `IN_DELIVERY` — czas przypisać kuriera |
| `/user/queue/courier-deliveries` | Kurier (EMPLOYEE/MANAGER) | Po `PATCH /deliveries/{id}/assign` oraz po `DELIVERED` |

**Payload** (JSON): `type`, `orderId`, `clientOrderNumber`, `status`, opcjonalnie `deliveryId`, `courierUserId`.  
Typy: `ORDER_SUBMITTED`, `ORDER_STATUS_CHANGED`, `DISPATCH_PENDING`, `DELIVERY_ASSIGNED`, `ORDER_DELIVERED`, `ORDER_CANCELLED`.

**FCM** (`POST /api/devices/fcm`): jeśli na Renderze jest `FIREBASE_SERVICE_ACCOUNT_JSON` — patrz tabela. Bez Firebase — tylko STOMP; w logach API: `FCM disabled`.

| Zdarzenie | Kto dostaje FCM | Kiedy (backend) |
|-----------|-----------------|-----------------|
| Nowe zamówienie sklepu | EMPLOYEE + MANAGER | `POST /api/orders` → `ORDER_SUBMITTED` |
| Gotowe do wysyłki | **MANAGER** | Status zamówienia → `IN_DELIVERY` (`DISPATCH_PENDING`) |
| **Przypisanie kuriera** | **Ten kurier** (userId z assign) | `PATCH /api/deliveries/{id}/assign` → `DELIVERY_ASSIGNED` |

**Mobilka (Michał) — Firebase / FCM**

1. **Ten sam projekt Firebase** co backend (`project_id` w service account = `project_id` w `google-services.json` / `GoogleService-Info.plist`).
2. **Backend (Render):** zmienna `FIREBASE_SERVICE_ACCOUNT_JSON` = cały JSON klucza *Firebase Console → Project settings → Service accounts → Generate new private key*. Nie commituj do repo — przekaż zespołowi bezpiecznym kanałem (1Password / DM).
3. **Aplikacja mobilna:** plik `google-services.json` (Android) z *Project settings → Your apps → Android* — ten sam projekt. iOS: `GoogleService-Info.plist`.
4. **Rejestracja tokenu:** po `POST /api/auth/staff/login` (+ 2FA jeśli włączone) jako kurier → `POST /api/devices/fcm` z Bearer, body: `{ "token": "<fcm-token>", "platform": "android" }` → **204**.
5. **Push „Nowa dostawa” (kurier)** leci dopiero po **`PATCH /api/deliveries/{id}/assign`** z `courierId` = `userId` zalogowanego kuriera — **nie** przy samym `IN_DELIVERY` (wtedy push idzie do managera). STOMP kuriera: `/user/queue/courier-deliveries` w tym samym momencie.
6. **Demo bez pełnego flow:** zamówienia **701104, 701106, 701108** są już `IN_DELIVERY` i przypisane do `employee@example.com` (seed) — do testu **listy** kuriera, nie nowego pusha. Nowy push: assign na **701105** (bez kuriera) albo hook poniżej.

**Tymczasowy hook testów (bez JWT)** — wyłącz po testach:

| Env Render | Wartość |
|------------|---------|
| `APP_DEV_NOTIFICATION_TEST_HOOK_ENABLED` | `true` (potem `false` lub usuń) |

```http
POST https://api-alcozon.onrender.com/api/dev/notification-test/order-assigned-to-employee
```

Tworzy zamówienie (`customer@example.com`), prowadzi do `IN_DELIVERY`, przypisuje dostawę do `employee@example.com` i odpala notifiery (w tym FCM kuriera, jeśli ma zarejestrowany token).

**Szkielety kodu klienta:**

| Klient | Ścieżka |
|--------|---------|
| Referencja TS (Desktop / kopiuj) | [`docs/realtime-skeletons/`](docs/realtime-skeletons/) |
| Web (Kuba) | `web-Alkozon/src/lib/realtime/` — `orderUpdates`, `staffOrderUpdates`, `dispatchOrderUpdates`, `courierOrderUpdates` |
| Mobilka (Michał) | `mobile-alkozon/Alkozon/lib/services/order_realtime_service.dart` + `order_realtime_example.dart` |

---

## Obieg MVP (uzgodniony)

1. **Web (klient):** `POST /api/orders` → `SUBMITTED` + adres dostawy (tekst).
2. **Mobilka (magazyn):** `PATCH /api/orders/{id}/status` → `IN_PRODUCTION` → `IN_PACKING` → `IN_DELIVERY`. Po `IN_DELIVERY` zamówienie znika z listy magazynu.
3. **Backend:** przy `IN_DELIVERY` tworzy rekord `Delivery` (adres w `deliveryDetails`).
4. **Desktop (MANAGER):** `PATCH /api/deliveries/{id}/assign` — body: `{ "courierId": <userId> }`.
5. **Mobilka (kurier):** lista zleceń:
   - `GET /api/orders/for-courier/{courierUserId}` — odpowiedź `{ shopOrders, customOrders }` (`IN_DELIVERY` + przypisany kurier);
   - alternatywnie: `GET /api/deliveries/my` (JWT kuriera).
6. **Kurier:** `PATCH /api/deliveries/{id}/status` → `{ "status": "DELIVERED" }` — synchronizuje zamówienie na `DELIVERED`.

### Dane dostawy przy zamówieniu (Web)

`POST /api/orders` — preferowany obiekt **`delivery`** (kolumny w DB + odpowiedź `deliveryDetails`):

```json
{
  "clientOrderNumber": "430721",
  "items": [{"productId": 1, "quantity": 2}],
  "delivery": {
    "recipientName": "Jakub Janiec",
    "streetAddress": "Wrocławska 12",
    "city": "Wrocław",
    "postalCode": "54-540",
    "country": "Polska",
    "deliveryNotes": "domek jednorodzinny",
    "paymentMethod": "Płatność przy odbiorze"
  }
}
```

- **`clientOrderNumber`** — jedyny numer widoczny dla klienta (Web generuje np. 6 cyfr); kolumna `client_order_number`.
- **`id`** — klucz techniczny (staff, wewnętrzne API).
- Adres tylko w **`deliveryDetails`** (kolumny strukturalne w DB) — bez `delivery_address` / `order_number` / `ORD-{id}`.
- `GET /api/orders/track?orderId=` — zamówienie sklepowe: `id`, `ORD-{id}` lub `clientOrderNumber`.
- `GET /api/custom-orders/track?orderId=` — zamówienie własne: `id`, `CUSTOM-{id}` lub `clientOrderNumber` + e-mail.
- `POST /api/custom-orders` — pole opcjonalne `clientOrderNumber` (lub w `preferences.clientOrderNumber`).

### Web (Kuba) — zmiany po stronie frontu (nie w tym repo API)

Backend **nie** łączy śledzenia sklepu z custom w jednym endpoincie — front musi:

1. **Checkout (tylko custom w koszyku)**  
   - Wysłać `clientOrderNumber` w `POST /api/custom-orders` (pole top-level **lub** `preferences.clientOrderNumber`).  
   - Na ekranie sukcesu pokazać **ten sam** numer (6 cyfr), nie `CUSTOM-{id}` jako domyślny, chyba że chcecie oba (numer + id techniczne).

2. **Śledzenie publiczne** (`order-status`)  
   - `GET /api/orders/track?orderId=&email=` — tylko zamówienia sklepowe (`orders`).  
   - Przy **404** wywołać `GET /api/custom-orders/track?orderId=&email=` (ten sam `orderId` / numer klienta / `CUSTOM-{id}`).  
   - Statusy custom = **te same co sklep** (`OrderStatus`): `SUBMITTED`, `IN_PRODUCTION`, `IN_PACKING`, `IN_DELIVERY`, `DELIVERED`, `CANCELLED` (migracja V19 z `PENDING`/`IN_PROGRESS`/…).
   - Przy `IN_DELIVERY` backend tworzy `Delivery` (jak sklep); desktop: `GET /api/deliveries` + `PATCH …/assign`; mobilka: `PATCH /api/custom-orders/{id}/status`.

3. **Moje zamówienia**  
   - Pobrać `GET /api/orders/my` **oraz** `GET /api/custom-orders/my`, scalić listę (np. po `createdAt`).  
   - W UI oznaczyć wpisy custom (np. „Zamówienie własne” + `description`).

4. **Odpowiedź API**  
   - `CustomOrderResponse` zawiera teraz `clientOrderNumber` (nie tylko `id`).  
   - `deliveryAddress` w zwykłym zamówieniu już **nie** ma w API — tylko `deliveryDetails` (sklep).

Migracje: **V18** (`client_order_number`), **V19** (statusy jak sklep + `deliveries.custom_order_id`).

---

## Role i uprawnienia

| Akcja | Rola |
|--------|------|
| Zmiana statusu zamówienia (magazyn) | `EMPLOYEE`, `MANAGER` |
| Przypisanie kuriera (`/deliveries/{id}/assign`) | **`MANAGER` tylko** |
| Lista wszystkich dostaw | `EMPLOYEE`, `MANAGER` |
| Moje dostawy (`/deliveries/my`) | `EMPLOYEE`, `MANAGER` (kurier) |
| Zamówienia kuriera (`/orders/for-courier/{id}`) | `EMPLOYEE` (własne id), `MANAGER` (dowolne id) |
| Oznaczenie dostarczone | przypisany kurier lub `MANAGER` |

---

## Endpointy — skrót

### Zamówienia
- `POST /api/orders` — klient
- `PATCH /api/orders/{id}/status` — staff
- **`GET /api/orders/staff/combined`** — magazyn: `{ shopOrders: Page, customOrders: [] }` (sklep + custom, jedno żądanie)
- **`GET /api/orders/for-courier/{courierUserId}`** — kurier: `{ shopOrders: [], customOrders: [] }` — `IN_DELIVERY` z przypisaną dostawą (oba typy)
- `GET /api/orders/track?orderId=&email=` — publiczne śledzenie

### Dostawy
- `GET /api/deliveries` — wszystkie (staff)
- `GET /api/deliveries/my` — JWT kuriera
- **`PATCH /api/deliveries/{id}/assign`** — tylko MANAGER, body: `{ "courierId": number }`
- `PATCH /api/deliveries/{id}/status` — `{ "status": "DELIVERED" | ... }`

### Auth — klienci (Web / Kuba)
- `POST /api/auth/register` — body m.in. `ageConfirmed: true` (wymagane); przy `true` ustawiane `ageConfirmedAt`
- `POST /api/auth/confirm-age` — **GUEST** (→ CUSTOMER) lub **CUSTOMER** bez `ageConfirmedAt` (idempotentne)
- `POST /api/auth/login` — **tylko CUSTOMER / GUEST** (staff → 400); w odpowiedzi: `role`, `ageConfirmedAt`
- `GET /api/users/me` — `role`, `ageConfirmedAt` (front: `CUSTOMER` + `ageConfirmedAt != null` = pełnoletni)
- `POST /api/orders` — **403** bez roli CUSTOMER lub bez `ageConfirmedAt`
- `POST /api/auth/refresh`

### Auth — staff (2FA, Etap 1b)

1. `POST /api/auth/staff/login` — body:
   ```json
   { "email": "employee@example.com", "password": "…", "deviceId": "uuid-lub-stały-id" }
   ```
   - Zaufane urządzenie → od razu `tokens` (jak zwykły login).
   - Nowe urządzenie → `verificationRequired: true`, `challengeId`, e-mail z kodem **4 cyfry**.

2. `POST /api/auth/staff/verify-device` — body:
   ```json
   { "challengeId": "uuid", "deviceId": "…", "code": "1234" }
   ```
   → `TokenResponse` (access + refresh). Urządzenie zapisane jako zaufane.

**Lokalnie:** Mailpit — `docker compose up` → SMTP `localhost:1025`, UI http://localhost:8025  
**Prod:** zmienne `SMTP_HOST`, `SMTP_PORT`, `MAIL_FROM` (np. SendGrid); awaryjnie `MAIL_LOG_ONLY=true` (kod w logach — tylko dev/debug).

---

## Środowiska

| Klient | URL |
|--------|-----|
| API prod | https://api-alcozon.onrender.com |
| Web | https://web-alkozon.vercel.app |
| Mobilka | prod API (Render) — potwierdzone przez Michała |

---

## Narzędzia

- CI: GitHub Actions (`mvn verify`, profil `test`, Testcontainers)
- Smoke: `.\scripts\smoke-prod.ps1`
