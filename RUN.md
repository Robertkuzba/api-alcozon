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
- Subskrypcja klienta (po zalogowaniu): `/user/queue/order-updates`
- Przy `CONNECT` przekaż nagłówek STOMP: `Authorization: Bearer <accessToken>`

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
Swagger: `/docs` · OpenAPI JSON: `/api-docs` · WebSocket: `wss://…/ws`

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

Render ustawia zmienne jako zmienne środowiskowe procesu Javy — **nie potrzebujesz** pliku `.env` na serwerze.

### Cold start (darmowy plan)

Po bezczynności pierwsze żądanie może trwać **50–90 s** (timeout w narzędziach ≠ błąd deployu). Otwórz `/docs` w przeglądarce i poczekaj na odpowiedź.

### Smoke test po deployu

1. Logi startu: Flyway migracje do **V11** (`fcm_device_tokens`), brak `missing table`.
2. `GET /api/orders/track?orderId=1&email=test@example.com` → 404 lub 200 (endpoint żyje).
3. `POST /api/auth/login` → token; `POST /api/devices/fcm` z Bearer → **204** (gdy FCM skonfigurowany).
4. STOMP: `CONNECT` z `Authorization: Bearer …` → subskrypcja `/user/queue/order-updates`.

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

### Docker (obraz własny, opcjonalnie)

```bash
./mvnw -q package -DskipTests
docker build -t alcohol-api .
```

Zmienne przekaż przy `docker run -e …` tak jak na Renderze.
