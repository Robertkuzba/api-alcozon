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

## Obieg MVP (uzgodniony)

1. **Web (klient):** `POST /api/orders` → `SUBMITTED` + adres dostawy (tekst).
2. **Mobilka (magazyn):** `PATCH /api/orders/{id}/status` → `IN_PRODUCTION` → `IN_PACKING` → `IN_DELIVERY`. Po `IN_DELIVERY` zamówienie znika z listy magazynu.
3. **Backend:** przy `IN_DELIVERY` tworzy `Delivery` z `addressSnapshot` z zamówienia.
4. **Desktop (MANAGER):** `PATCH /api/deliveries/{id}/assign` — body: `{ "courierId": <userId> }`.
5. **Mobilka (kurier):** lista zleceń:
   - `GET /api/orders/for-courier/{courierUserId}` — zamówienia `IN_DELIVERY` przypisane do kuriera (EMPLOYEE: tylko własne `userId` z JWT / `GET /api/users/me`);
   - alternatywnie: `GET /api/deliveries/my` (JWT kuriera).
6. **Kurier:** `PATCH /api/deliveries/{id}/status` → `{ "status": "DELIVERED" }` — synchronizuje zamówienie na `DELIVERED`.

### Format adresu (rekomendowany)

```
ul. Nazwa 50, 50-001 Wrocław, Polska
```

API przechowuje jeden string (`deliveryAddress` / `addressSnapshot`); geokodowanie po stronie aplikacji mobilnej.

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
