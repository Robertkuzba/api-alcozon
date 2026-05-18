# Integracja zespołu — API

> **Status:** uzgodnione 17.05.2026. Etap 1a (dostawy) + **1b (2FA staff)** wdrożone w backendzie.

**Produkcja:** `https://api-alcozon.onrender.com`  
**Swagger:** `/docs`  
**Health:** `GET /actuator/health`

---

## Konta demo

| Rola | E-mail | Hasło |
|------|--------|--------|
| Manager (desktop) | `manager@example.com` | `Manager123!` |
| Pracownik / kurier (mobilka) | `employee@example.com` | `Employee123!` |
| Klient (demo zamówienia) | `customer@example.com` | `Customer123!` |

### Zamówienia demo (seed przy starcie API, `DemoOrderSeeder`)

Numer klienta (`clientOrderNumber`) **701101–701111** — ponowny seed pomijany, jeśli istnieje `701101`.

| Nr | Status | Kurier (`employee@`) | Adres (skrót) |
|----|--------|----------------------|---------------|
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

- Kurier: `GET /api/orders/for-courier/{id}` — tylko **701104, 701106, 701108** (+ 701110 jeśli jeszcze IN_DELIVERY w UI; seed ma DELIVERED).
- Desktop: `GET /api/deliveries` — wszystkie `IN_DELIVERY`, m.in. **701105, 701107, 701109** bez kuriera (`courierId` null).

---

## Słownik statusów zamówienia (`OrderStatus`)

| Status API | PL (UI) |
|------------|---------|
| `SUBMITTED` | Złożone (klient, web) |
| `IN_PRODUCTION` | W produkcji |
| `IN_PACKING` | W pakowaniu |
| `IN_DELIVERY` | W drodze — **tutaj desktop przypisuje kuriera** |
| `DELIVERED` | Dostarczone |
| `CANCELLED` | Anulowane |

**Uwaga:** Rekord `Delivery` powstaje przy przejściu zamówienia na **`IN_DELIVERY`** (nie przy `IN_PACKING`).

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

**FCM** (`POST /api/devices/fcm`): jeśli na Renderze jest `FIREBASE_SERVICE_ACCOUNT_JSON` — push przy nowym zamówieniu (staff), `IN_DELIVERY` (manager), przypisaniu kuriera (ten kurier). Bez Firebase — tylko STOMP.

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
   - `GET /api/orders/for-courier/{courierUserId}` — zamówienia `IN_DELIVERY` przypisane do kuriera (EMPLOYEE: tylko własne `userId` z JWT / `GET /api/users/me`);
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
- `GET /api/orders/track?orderId=` — akceptuje `id`, `ORD-{id}` (kompatybilność) lub `clientOrderNumber`.

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
- **`GET /api/orders/for-courier/{courierUserId}`** — lista `IN_DELIVERY` przypisanych do kuriera
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
