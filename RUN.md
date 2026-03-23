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
