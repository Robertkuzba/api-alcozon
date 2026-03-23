# Alcohol Factory API (Alkozon)

Backend: **Java 21**, **Spring Boot 4.x**, **PostgreSQL**, **Flyway**, **JWT**, **WebSocket (STOMP)**, **OpenAPI** (`/docs`).

## Wymagania

- JDK 21, Maven 3.9+
- Docker (dla Postgresa lokalnie; dla testów integracyjnych – Testcontainers, opcjonalnie gdy brak Dockera test `contextLoads` jest pomijany)

## Uruchomienie

1. Baza: `docker compose up -d` (port `5432`, baza `alcohol_db`, user `app` / `secret`).
2. Aplikacja: `./mvnw spring-boot:run`
3. Swagger: http://localhost:8080/docs  
4. WebSocket STOMP: `ws://localhost:8080/ws` – nagłówek `Authorization: Bearer <token>` przy `CONNECT`.

## Konta deweloperskie (profil domyślny, `@Profile("!test")`)

Po pierwszym starcie (pusta baza):

| Email               | Hasło         | Rola    |
|---------------------|---------------|---------|
| manager@example.com | `Manager123!` | MANAGER |
| employee@example.com| `Employee123!`| EMPLOYEE (kurier) |

Dodawany jest też przykładowy produkt ze stanem magazynowym.

## API

Prefiks REST: **`/api`** (np. `POST /api/auth/login`). Szczegóły endpointów w Swagger.

## Docker (obraz aplikacji)

```bash
./mvnw -q package -DskipTests
docker build -t alcohol-api .
```

## CI

GitHub Actions: `.github/workflows/ci.yml` (`mvn verify` – wymaga Dockera na runnerze).
