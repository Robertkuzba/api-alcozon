# Uruchomienie Alcohol Factory API

## 1. Baza danych (Docker)

W katalogu projektu:

```bash
docker compose up -d
```

Sprawdzenie, że kontener działa:

```bash
docker compose ps
```

Baza: `alcohol_db`, użytkownik: `app`, hasło: `secret`, port: `5432`.

**IntelliJ — czerwone podkreślenia w `V13__…sql` / encjach JPA:** to zwykle inspekcja IDE, nie błąd Flywaya. Ustaw dialekt SQL: *Settings → Languages & Frameworks → SQL Dialects* → katalog `src/main/resources/db/migration` → **PostgreSQL** (szablon: skopiuj `config/intellij/sqlDialects.xml` do `.idea/sqlDialects.xml`). Dla JPA: *Database* → połącz z `localhost:5432/alcohol_db` → po pierwszym `spring-boot:run` (Flyway V13) → *Assign Data Sources* w oknie Persistence.

**Mailpit (2FA e-mail dla staff):** ten sam `docker compose up -d` uruchamia Mailpit — SMTP `localhost:1025`, skrzynka http://localhost:8025

## 2. Aplikacja

```bash
./mvnw spring-boot:run
```

(lub w IntelliJ: uruchom `AlcoholFactoryApiApplication`).

## 3. Sprawdzenie Swagger UI

- Otwórz w przeglądarce: **http://localhost:8080/docs**
- Alternatywnie: http://localhost:8080/swagger-ui.html (zależnie od wersji Springdoc)
- OpenAPI JSON: http://localhost:8080/api-docs

## 4. WebSocket (STOMP)

- Endpoint: `ws://localhost:8080/ws`
- Przy `CONNECT` przekaż nagłówek STOMP: `Authorization: Bearer <accessToken>`
- Subskrypcje (zależnie od roli w JWT):
  - **CUSTOMER (Web):** `/user/queue/order-updates`
  - **Magazyn (mobilka):** `/topic/orders/staff`
  - **Desktop (manager):** `/topic/orders/staff` + `/topic/orders/dispatch`
  - **Kurier:** `/user/queue/courier-deliveries`
- Payload: `type`, `orderId`, `clientOrderNumber`, `status`, opcjonalnie `deliveryId`, `courierUserId` — szczegóły w [INTEGRATION_TEAM.md](INTEGRATION_TEAM.md)

## 5. Dane startowe

Przy pustej bazie aplikacja tworzy użytkowników `manager@example.com` / `Manager123!` oraz `employee@example.com` / `Employee123!` i przykładowy produkt (patrz [README.md](README.md)).

---

**Uwaga:** Jeśli używasz wbudowanego wsparcia Spring Boot dla Docker Compose, uruchomienie `spring-boot:run` może samo podnieść kontener z `compose.yaml` (gdy `spring-boot-docker-compose` jest na classpath). Wtedy nie musisz ręcznie odpalać `docker compose up`.

---

## 6. Zmienne środowiskowe (lokalnie)

Backend **nie używa** pliku `.env.local` tak jak Next.js (Kuba) — Spring czyta zmienne z **systemu operacyjnego** albo z panelu Render. W repozytorium jest tylko szablon [`.env.example`](.env.example).

### Domyślna praca bez `.env`

Przy `docker compose up` + `./mvnw spring-boot:run` wystarczą wartości domyślne z `application.yml` (baza `localhost:5432`, dev `JWT_SECRET`). Nie musisz tworzyć żadnego pliku `.env`.

### Gdy chcesz lokalnie testować FCM / inny JWT / CORS

1. Skopiuj szablon: `cp .env.example .env.local` (Windows: skopiuj ręcznie jako `.env.local`).
2. Uzupełnij wartości — **nie commituj** `.env.local` (jest w `.gitignore`).
3. Załaduj zmienne przed startem aplikacji (Spring ich sam z pliku nie czyta):

**PowerShell (sesja bieżąca):**

```powershell
Get-Content .env.local | ForEach-Object {
  if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
  }
}
./mvnw spring-boot:run
```

**IntelliJ:** Run Configuration → Environment variables → wklej z `.env.local` lub użyj pluginu „EnvFile”.

**Linux/macOS:**

```bash
set -a && source .env.local && set +a && ./mvnw spring-boot:run
```

### Różnica względem frontu (np. aquatracker / web-Alkozon)

| | Next.js (Kuba) | Ten API (Spring) |
|---|----------------|------------------|
| Plik | `.env.local` — **wczytywany automatycznie** | `.env.local` — tylko szablon dla Ciebie; trzeba wyeksportować do OS |
| Produkcja | Vercel / hosting — zmienne w panelu | **Render** → Environment |
| Sekrety w repo | Nie | Nie (`.env.example` bez haseł) |

---

## 7. Wdrożenie na Render (produkcja)

Publiczne API (przykład): **https://api-alcozon.onrender.com**  
Swagger: `/docs` · OpenAPI JSON: `/api-docs` · WebSocket: `wss://…/ws` · Health: `GET /actuator/health` (bez auth, dla Render / monitoringu)

Integracja zespołu (szkic, statusy, endpointy): [INTEGRATION_TEAM.md](INTEGRATION_TEAM.md)

### Wymagane zmienne (Render → Environment)

| Zmienna | Opis |
|---------|------|
| `SPRING_DATASOURCE_URL` | JDBC z bazy Postgres na Renderze (Internal URL) |
| `SPRING_DATASOURCE_USERNAME` | Użytkownik bazy |
| `SPRING_DATASOURCE_PASSWORD` | Hasło bazy |
| `JWT_SECRET` | Silny losowy sekret (min. 32 znaki); **nie** zostawiaj wartości dev z `application.yml` |

Render ustawia też **`PORT`** (np. `10000`) — aplikacja musi nasłuchiwać na tym porcie (`server.port=${PORT:8080}` w `application.yml`). Bez tego deploy kończy się błędem *No open ports detected*.

### Opcjonalne

| Zmienna | Opis |
|---------|------|
| `APP_CORS_ALLOWED_ORIGINS` | Dodatkowe originy frontu, CSV: `https://app.example.com,http://localhost:3000` |
| `APP_OPENAPI_SERVER_URL` | URL API w Swagger (domyślnie `https://api-alcozon.onrender.com`) |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | **Cały** JSON service account (Firebase Admin). Puste = FCM wyłączone (log: `FCM disabled`). Nie ustawiaj `{}`. |

### 2FA staff (e-mail) na Renderze

Domyślne `spring.mail.host=localhost:1025` to **Mailpit tylko lokalnie**. Na Renderze **nie ma** Mailpit — health check SMTP dawał `DOWN` i deploy wisiał (naprawione: `management.health.mail.enabled=false`).

**Szybki start (demo):**

| Zmienna | Wartość |
|---------|---------|
| `MAIL_LOG_ONLY` | `true` — kody 2FA w logach Rendera (nie na prawdziwy mail) |
| `APP_TWO_FACTOR_ENABLED` | `true` (domyślnie) |

**Docelowo (prawdziwy SMTP, np. SendGrid):**

| Zmienna | Przykład |
|---------|----------|
| `MAIL_LOG_ONLY` | `false` |
| `SMTP_HOST` | `smtp.sendgrid.net` |
| `SMTP_PORT` | `587` |
| `SMTP_USERNAME` | `apikey` |
| `SMTP_PASSWORD` | klucz API |
| `SMTP_AUTH` | `true` |
| `SMTP_STARTTLS` | `true` |
| `MAIL_FROM` | `noreply@twojadomena.pl` |

Health check path w Renderze: **`/actuator/health`** (initial delay ~180–300 s przy cold start).

Render ustawia zmienne jako zmienne środowiskowe procesu Javy — **nie potrzebujesz** pliku `.env` na serwerze.

### Cold start (darmowy plan)

Po bezczynności pierwsze żądanie może trwać **50–90 s** (timeout w narzędziach ≠ błąd deployu). Otwórz `/docs` w przeglądarce i poczekaj na odpowiedź.

### Smoke test po deployu

**Skrypt (PowerShell):**

```powershell
.\scripts\smoke-prod.ps1
# inny host: .\scripts\smoke-prod.ps1 -BaseUrl "https://api-alcozon.onrender.com"
```

Ręcznie:

1. Logi startu: Flyway migracje (np. **V12** katalog produktów), brak `missing table`.
2. `GET /actuator/health` → `{"status":"UP"}` (w Render: Health Check Path → `/actuator/health`).
3. `GET /api/orders/track?orderId=1&email=test@example.com` → 404 lub 200 (endpoint żyje).
4. `POST /api/auth/login` → token; `POST /api/devices/fcm` z Bearer → **204** (gdy FCM skonfigurowany).
5. STOMP: `CONNECT` z `Authorization: Bearer …` → subskrypcja `/user/queue/order-updates`.

### CI (GitHub Actions)

Przy każdym push/PR na `main`/`master`: `./mvnw verify` z profilem `test` i **Testcontainers** (PostgreSQL). Wymaga Dockera na runnerze (domyślnie na `ubuntu-latest`). Lokalnie bez Dockera testy integracyjne są pomijane (`disabledWithoutDocker`).

### Typowe błędy

| Objaw | Przyczyna | Działanie |
|-------|-----------|-----------|
| `No open ports detected` / port scan timeout | Aplikacja na `8080`, Render skanuje `PORT` | `server.port=${PORT:8080}` + redeploy; ewent. zwiększ health check timeout w Render |
| Start ~3 min, deploy killed | Wolny cold start Spring + Neon | Pierwszy deploy po poprawce PORT; rozważ płatny plan lub mniejszy obraz JVM |
| `missing table [fcm_device_tokens]` | Baza bez migracji V11 | Redeploy z aktualnym kodem; sprawdź logi Flyway |
| `FCM disabled` w logach | Brak / puste `FIREBASE_SERVICE_ACCOUNT_JSON` | Wklej pełny JSON service account; restart |
| FCM init failed | Zły JSON lub inny projekt niż front | Ten sam Firebase `projectId` co u Kuby (web) / Michała (mobile) |
| CORS / WS blocked w przeglądarce | Origin frontu poza listą | Dopisz URL do `APP_CORS_ALLOWED_ORIGINS` |
| 401 na `/api/devices/fcm` | Brak Bearer | Token po `POST /api/auth/login` |
| Push nie dociera do klienta www | Backend wysyła FCM tylko do EMPLOYEE/MANAGER | Status zamówienia dla CUSTOMER → **STOMP**, nie FCM |

### Katalog produktów (pliki txt)

Folder `Informacje do bazy danych/` — listy alkoholi (nazwa, %, pojemność w linii).

Po edycji plików wygeneruj migrację i wgraj na produkcję:

```powershell
py scripts/generate_product_seed.py
git add src/main/resources/db/migration/V12__seed_catalog_products.sql
# commit + push → Render uruchomi Flyway V12 na Neon
```

Migracja `V12__seed_catalog_products.sql` dodaje ~86 produktów + stan magazynowy (50 szt.), dezaktywuje demo `Demo Vodka 500ml`.

### Docker (obraz własny, opcjonalnie)

```bash
./mvnw -q package -DskipTests
docker build -t alcohol-api .
```

Zmienne przekaż przy `docker run -e …` tak jak na Renderze.
