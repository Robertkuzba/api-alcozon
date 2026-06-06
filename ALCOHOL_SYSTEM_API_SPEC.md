# Dokumentacja API – System dla firmy produkującej alkohol

**Wersja:** 1.1  
**Autor:** Backend (Java/Spring Boot)  
**Data:** 2026

---

## 1. Kontekst projektu i rola API

### 1.1 Architektura systemu

| Część | Technologie | Odpowiedzialność |
|-------|-------------|------------------|
| **Web** | Next.js, TypeScript, React, Tailwind | Klienci: sklep, zamówienia (standardowe i niestandardowe), śledzenie zamówienia, konto, gość + potwierdzenie 18+, informacje o alkoholach, dark mode, PL/ENG. |
| **Mobile** | Flutter, Android Studio | Pracownicy i kurierzy: inwentarz, lista zamówień niestandardowych, lista dostaw z adresami, HR (godziny, przerwy), zmiana statusów zamówień. |
| **Desktop** | Python, Flet | Manager: stan magazynu, zamawianie towaru, kurierzy i dostawy, ogłoszenia dostaw, oferty pracy, zatrudnianie/zwalnianie. |
| **API** | Java 21, Spring Boot 4.x, PostgreSQL | **Centralna logika biznesowa:** REST + WebSocket (STOMP), spięcie Web / Mobile / Desktop. |

### 1.2 Odpowiedzialności API (z założeń)

- **Centralna logika biznesowa** – spięcie aplikacji Web, Mobile i Desktop (architektura REST).
- **Zarządzanie zamówieniami** – pełny cykl życia zamówienia („Złożone” → „W produkcji” / „W pakowaniu” → „W dostawie” → „Dostarczone”) i synchronizacja statusów między klientem a magazynem/kurierem.
- **Powiadomienia w czasie rzeczywistym** – WebSocket do natychmiastowego informowania Webówki o zmianie statusu zamówienia (np. gdy kurier zaznaczy „Dostarczone”).
- **Moduł HR** – endpointy do rejestracji czasu pracy (Start/Stop/Przerwa) dla Mobile oraz agregacja danych o godzinach dla Managera (Desktop).
- **Analityka i raporty** – agregacja danych sprzedażowych i magazynowych dla wykresów w Desktop.
- **Obsługa zamówień niestandardowych** – przyjmowanie zapytań z Weba i wystawianie ich pracownikom na Mobile.
- **Dokumentacja API** – automatycznie generowana dokumentacja (Swagger UI / OpenAPI) dla zespołów frontendowych.

---

## 2. Opis funkcjonalny API (moduły)

### 2.1 Moduły i odbiorcy

| Moduł | Opis | Web | Mobile | Desktop |
|-------|------|-----|--------|---------|
| **Auth** | Rejestracja, logowanie (JWT), refresh, potwierdzenie 18+ (GUEST→CUSTOMER). Opcjonalnie 2FA. | ✓ | ✓ | ✓ |
| **Products** | Katalog alkoholi, ceny, dostępność, wyszukiwarka (z rate limitem). | ✓ | ✓ | ✓ |
| **Orders** | Zamówienia standardowe (z koszyka) + śledzenie statusu. | ✓ | ✓ | ✓ |
| **Custom Orders** | Zamówienia niestandardowe (zapytanie klienta → lista dla pracowników). | ✓ | ✓ | — |
| **Inventory** | Stan magazynu: **produkty** i **surowce**; zwiększanie/zmniejszanie ilości. | — | ✓ | ✓ |
| **Warehouse Replenishment** | Zamawianie nowego towaru (Manager). | — | — | ✓ |
| **Delivery** | Lista dostaw, adresy klientów, przypisanie kuriera, statusy. Ogłoszenia dostaw. | — | ✓ | ✓ |
| **HR / WorkLog** | Clock-in/out, przerwy; dane o godzinach (tydzień/miesiąc); agregacja dla managera. | — | ✓ | ✓ |
| **Notifications** | WebSocket (STOMP): push o zmianie statusu zamówienia. | ✓ | ✓ | ✓ |
| **Reports** | Sprzedaż, magazyn, wydajność pracowników – dane pod wykresy Desktop. | — | — | ✓ |
| **Admin / Kadry** | CRUD pracowników, oferty pracy, zatrudnianie, zwalnianie. | — | — | ✓ |

### 2.2 Dla Webówki (Next.js)

- **Zamawianie produktów** – katalog (`GET /products`), wyszukiwarka (rate limit 5 req/s na IP), koszyk, `POST /orders`.
- **Śledzenie zamówienia** – `GET /orders/my`, `GET /orders/{id}`; WebSocket `/user/queue/order-updates` przy zmianie statusu.
- **Zamówienie niestandardowe** – `POST /custom-orders` (opis, preferencje); po stronie Web wyświetlanie statusu.
- **Konto** – `POST /auth/register`, `POST /auth/login`; po zalogowaniu rola CUSTOMER (jeśli wiek potwierdzony).
- **Gość** – wejście bez konta (GUEST); **potwierdzenie pełnoletności** `POST /auth/confirm-age`. Bez potwierdzenia 18+ **brak możliwości zamawiania** (API zwraca 403 lub odpowiedni kod).
- **Sekcja informacji o alkoholach** – `GET /products`, `GET /products/{id}` (opis, kategoria, ABV itd.).
- **Walidacja** – server-side validation wszystkich danych; **ochrona przed Injection Attacks** (m.in. blokowanie wpisywania kodu SQL w pola typu Imię) – patrz sekcja Zabezpieczenia.

### 2.3 Dla Mobilki (Flutter – Pracownicy)

- **Inwentarz** – `GET /inventory` (produkty i surowce), `PATCH /inventory/items/{id}` lub podobnie – zwiększanie/zmniejszanie ilości.
- **Lista zamówień niestandardowych** – `GET /custom-orders` (do obsługi przez pracowników).
- **Lista zamówień do dostawy** – `GET /deliveries/my` lub `GET /deliveries` z filtrem; w odpowiedzi adres przesyłki i dane klienta.
- **HR** – `POST /work-log/clock-in`, `clock-out`, `break/start`, `break/end`; `GET /work-log/my` (godziny w tygodniu/miesiącu).
- **Zmiana statusów zamówień** – `PATCH /orders/{id}/status`: Złożone → W produkcji / W pakowaniu → W dostawie → Dostarczone.
- **Limit prób logowania** – realizowany po stronie API (rate limiting na `/auth/login`). **2FA** – możliwe do dodania później (API gotowe na rozszerzenie).

### 2.4 Dla Desktopu (Python/Flet – Manager)

- **Stan magazynu i zamawianie towaru** – `GET /inventory`, `POST /warehouse/replenishment` (zamówienie nowego towaru u dostawcy / do produkcji).
- **Kurierzy i stan dostaw** – `GET /deliveries`, `GET /admin/couriers` lub użytkownicy z rolą kuriera; statusy dostaw.
- **Ogłoszenia dostaw** – CRUD ogłoszeń (np. `GET/POST/PUT/DELETE /admin/delivery-announcements`).
- **Oferty pracy, zatrudnianie, zwalnianie** – `GET/POST/PUT/DELETE /admin/job-offers`, zatrudnianie/zwalnianie przez `POST /admin/employees/{id}/hire`, `POST /admin/employees/{id}/terminate` lub odpowiednie endpointy w `/admin/users`.

---

## 3. Stos technologiczny (Backend)

| Technologia | Wersja / wybór | Uzasadnienie |
|-------------|----------------|--------------|
| **Java** | 21 LTS | Długie wsparcie, Virtual Threads, aktualny standard w enterprise. |
| **Spring Boot** | 4.x | Kompatybilność z Java 21, Spring Security 6, JWT, WebSocket. |
| **PostgreSQL** | 15+ | ACID, JSONB pod rozszerzalne atrybuty, dobra integracja z Spring Data JPA i Dockerem. |
| **Spring Security** | 6 | JWT (access + refresh), RBAC (GUEST, CUSTOMER, EMPLOYEE, MANAGER). |
| **Spring Data JPA** | 3.x | CRUD, repozytoria, transakcje; Hibernate. |
| **Spring WebSocket (STOMP)** | — | Real-time: powiadomienia o statusie zamówienia. |
| **JWT** | jjwt / Spring Security | Stateless auth, role w claimach. |
| **Jakarta Validation** | 3.x | Server-side validation (druga linia obrony po frontendzie). |
| **BCrypt** | — | Hashowanie haseł. |
| **Bucket4j** | — | Rate limiting (np. 10 prób logowania / 15 min / IP, 5 zapytań/s do wyszukiwarki). |
| **Springdoc OpenAPI** | 2.x | Swagger UI + OpenAPI 3 pod `/docs`. |
| **Docker** | — | Konteneryzacja aplikacji i bazy. |

---

## 4. Model danych (główne encje i relacje)

### 4.1 Encje główne

- **User**  
  `id` (Long, PK), `email`, `passwordHash` (BCrypt), `role` (GUEST, CUSTOMER, EMPLOYEE, MANAGER), `ageConfirmedAt` (nullable), `isActive`, `createdAt`, `updatedAt`; opcjonalnie: `firstName`, `lastName`, `phone`. Dla 2FA: `twoFactorSecret`, `twoFactorEnabled`.

- **Product**  
  `id`, `name`, `description`, `category`, `price`, `volumeMl`, `abv`, `imageUrl`, `isActive`, `createdAt`, `updatedAt`. (Produkty gotowe – alkohole.)

- **RawMaterial** (surowiec)  
  `id`, `name`, `unit`, `quantity`, `lastUpdatedAt`. (Surowce magazynowe – osobna encja lub wspólna tabela `InventoryItem` z typem PRODUCT/RAW_MATERIAL.)

- **InventoryItem** (unifikacja: produkt lub surowiec)  
  `id`, `productId` (FK, nullable), `rawMaterialId` (FK, nullable), `quantity`, `warehouseZone` (opcjonalnie), `lastUpdatedAt`. Albo osobne tabele: **Inventory** (productId + quantity) i **RawMaterial** z polem quantity.

- **Order**  
  `id`, `customerId` (FK → User), `status` (enum: SUBMITTED, IN_PRODUCTION, IN_PACKING, IN_DELIVERY, DELIVERED, CANCELLED), `deliveryAddress`, `totalAmount`, `createdAt`, `updatedAt`, `deliveredAt` (nullable).

- **OrderItem**  
  `id`, `orderId` (FK → Order), `productId` (FK → Product), `quantity`, `unitPrice` (snapshot).

- **CustomOrder** (zamówienie niestandardowe)  
  `id`, `customerId` (FK → User), `description`, `preferences` (np. JSONB), `status` (PENDING, IN_PROGRESS, COMPLETED, REJECTED), `assignedTo` (FK → User, nullable), `createdAt`, `updatedAt`.

- **WorkLog**  
  `id`, `employeeId` (FK → User), `clockInAt`, `clockOutAt` (nullable), `breakStartedAt`, `breakEndedAt` (nullable), `notes`.

- **Delivery**  
  `id`, `orderId` (FK → Order), `courierId` (FK → User, nullable), `status` (PENDING, ASSIGNED, IN_TRANSIT, DELIVERED, FAILED), `addressSnapshot` (adres przesyłki), `startedAt`, `deliveredAt` (nullable).

- **DeliveryAnnouncement** (ogłoszenie dostaw – Desktop)  
  `id`, `title`, `content`, `publishedAt`, `createdBy` (FK → User), `createdAt`.

- **JobOffer** (oferta pracy)  
  `id`, `title`, `description`, `status` (OPEN, CLOSED), `createdAt`, `updatedAt`. Powiązanie z aplikacjami/kandydatami według potrzeb (np. osobna tabela JobApplication).

### 4.2 Relacje (skrót)

- **Order** 1:N **OrderItem**; **Order** N:1 **User** (customer); **Order** 1:1 **Delivery** (opcjonalnie).
- **Delivery** N:1 **User** (courier).
- **CustomOrder** N:1 **User** (customer, assignedTo).
- **WorkLog** N:1 **User** (employee).
- **Inventory** – produkt lub surowiec (jedna tabela pozycji magazynowych z referencją do Product lub RawMaterial).

### 4.3 Role a uprawnienia

- **GUEST** – przeglądanie katalogu; **brak zamawiania** do momentu potwierdzenia 18+ (`/auth/confirm-age`).
- **CUSTOMER** – zamówienia standardowe i niestandardowe, śledzenie, własne dane.
- **EMPLOYEE** – inwentarz, lista zamówień niestandardowych, lista dostaw, zmiana statusów zamówień, HR (własne clock-in/out, przerwy).
- **MANAGER** – pełny dostęp: magazyn, replenishment, kurierzy, ogłoszenia dostaw, oferty pracy, zatrudnianie/zwalnianie, raporty.

---

## 5. Kluczowe endpointy (pogrupowane)

Base path: `/api`. Autoryzacja: `Authorization: Bearer <accessToken>`.

### 5.1 Auth (`/api/auth`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| POST | `/register` | Rejestracja (email, hasło). | Public |
| POST | `/login` | Logowanie → accessToken + refreshToken. **Rate limit: 10 prób / 15 min / IP.** | Public |
| POST | `/refresh` | Odświeżenie accessToken. | Public |
| POST | `/confirm-age` | Potwierdzenie pełnoletności 18+ (GUEST → CUSTOMER). | GUEST |
| POST | `/logout` | Unieważnienie refresh tokena. | Authenticated |

### 5.2 Products (`/api/products`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| GET | `/` | Lista produktów; wyszukiwarka (query: q, category, minPrice, maxPrice). **Rate limit: 5 zapytań/s na IP.** | GUEST+ |
| GET | `/{id}` | Szczegóły produktu + dostępność. | GUEST+ |
| POST | `/` | Utworzenie produktu. | MANAGER |
| PUT | `/{id}` | Aktualizacja. | MANAGER |
| DELETE | `/{id}` | Deaktywacja (soft delete). | MANAGER |

### 5.3 Orders (`/api/orders`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| POST | `/` | Złożenie zamówienia (koszyk + adres). | CUSTOMER |
| GET | `/my` | Moje zamówienia (śledzenie). | CUSTOMER |
| GET | `/{id}` | Szczegóły zamówienia. | CUSTOMER (własne) / EMPLOYEE, MANAGER |
| PATCH | `/{id}/cancel` | Anulowanie (gdy dozwolone). | CUSTOMER |
| GET | `/` | Lista zamówień (filtr, paginacja). | EMPLOYEE, MANAGER |
| PATCH | `/{id}/status` | Zmiana statusu: SUBMITTED → IN_PRODUCTION → IN_PACKING → IN_DELIVERY → DELIVERED. | EMPLOYEE, MANAGER |

### 5.4 Custom Orders (`/api/custom-orders`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| POST | `/` | Złożenie zapytania niestandardowego (opis, preferencje). | CUSTOMER |
| GET | `/my` | Moje zapytania niestandardowe. | CUSTOMER |
| GET | `/` | Lista do obsługi (dla pracowników). | EMPLOYEE, MANAGER |
| GET | `/{id}` | Szczegóły. | CUSTOMER (własne) / EMPLOYEE, MANAGER |
| PATCH | `/{id}/status` | Zmiana statusu (np. IN_PROGRESS, COMPLETED). | EMPLOYEE, MANAGER |
| PATCH | `/{id}/assign` | Przypisanie do pracownika. | MANAGER, EMPLOYEE |

### 5.5 Inventory (`/api/inventory`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| GET | `/` | Stan magazynu (produkty i surowce). | EMPLOYEE, MANAGER |
| GET | `/products/{productId}` | Stan dla produktu. | EMPLOYEE, MANAGER |
| GET | `/raw-materials/{rawMaterialId}` | Stan dla surowca. | EMPLOYEE, MANAGER |
| PATCH | `/products/{productId}` | Zwiększenie/zmniejszenie ilości (body: delta lub quantity). | EMPLOYEE, MANAGER |
| PATCH | `/raw-materials/{rawMaterialId}` | Jak wyżej dla surowców. | EMPLOYEE, MANAGER |

### 5.6 Warehouse Replenishment (`/api/warehouse`) – Desktop

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| POST | `/replenishment` | Zamówienie nowego towaru (produkty/surowce, ilości). | MANAGER |
| GET | `/replenishment` | Historia zamówień uzupełnień. | MANAGER |
| PATCH | `/replenishment/{id}` | Przyjęcie dostawy: body `{ "status": "RECEIVED" }` lub `"COMPLETED"` — aktualizacja stanu magazynu. | MANAGER |

### 5.7 Delivery (`/api/deliveries`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| GET | `/my` | Dostawy przypisane do kuriera (z adresem i danymi klienta). | EMPLOYEE (kurier) |
| GET | `/` | Wszystkie dostawy (filtr, status). | EMPLOYEE, MANAGER |
| PATCH | `/{id}/assign` | Przypisanie kuriera. | MANAGER, EMPLOYEE |
| PATCH | `/{id}/status` | Status: IN_TRANSIT, DELIVERED, FAILED. | EMPLOYEE (kurier), MANAGER |

### 5.8 Delivery Announcements (`/api/admin/delivery-announcements`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| GET | `/` | Lista ogłoszeń. | MANAGER, EMPLOYEE (odczyt) |
| POST | `/` | Utworzenie ogłoszenia. | MANAGER |
| PUT | `/{id}` | Edycja. | MANAGER |
| DELETE | `/{id}` | Usunięcie. | MANAGER |

### 5.9 HR / WorkLog (`/api/work-log`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| POST | `/clock-in` | Rozpoczęcie pracy. | EMPLOYEE |
| POST | `/clock-out` | Zakończenie pracy. | EMPLOYEE |
| POST | `/break/start` | Początek przerwy. | EMPLOYEE |
| POST | `/break/end` | Koniec przerwy. | EMPLOYEE |
| GET | `/my` | Moje wpisy (query: from, to – np. tydzień/miesiąc). | EMPLOYEE |
| GET | `/reports/summary` | Agregacja godzin / pracowników (dla Desktop). | MANAGER |

### 5.10 Reports (`/api/reports`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| GET | `/sales` | Agregacja sprzedaży (daty, grupowanie). | MANAGER |
| GET | `/employees/work-summary` | Godziny, przerwy, wydajność. | MANAGER |
| GET | `/inventory` | Stan magazynu, ruch. | MANAGER |

### 5.11 Admin – Kadry, oferty pracy (`/api/admin`)

| Metoda | Ścieżka | Opis | Role |
|--------|---------|------|------|
| GET | `/users` | Lista użytkowników (role, aktywni). | MANAGER |
| POST | `/users` | Dodanie pracownika. | MANAGER |
| PUT | `/users/{id}` | Edycja. | MANAGER |
| DELETE | `/users/{id}` | Deaktywacja. | MANAGER |
| POST | `/users/{id}/hire` | Zatrudnienie. | MANAGER |
| POST | `/users/{id}/terminate` | Zwalnianie. | MANAGER |
| GET | `/job-offers` | Lista ofert pracy. | MANAGER |
| POST | `/job-offers` | Utworzenie oferty. | MANAGER |
| PUT | `/job-offers/{id}` | Edycja / zamknięcie. | MANAGER |
| DELETE | `/job-offers/{id}` | Usunięcie. | MANAGER |

---

## 6. Zabezpieczenia

### 6.1 Uwierzytelnianie i autoryzacja (JWT)

- **Access token:** krótki TTL (np. 15–30 min), nagłówek `Authorization: Bearer <token>`.
- **Refresh token:** dłuższy (np. 7 dni), endpoint `POST /api/auth/refresh`.
- W claimach: `sub` (userId), `role`, `email`.
- **2FA:** możliwe rozszerzenie (pole w User, endpointy weryfikacji kodu); Mobile może być pierwszym odbiorcą.

### 6.2 RBAC (Role-Based Access Control)

- Role: **GUEST**, **CUSTOMER**, **EMPLOYEE**, **MANAGER**.
- GUEST bez potwierdzenia 18+ **nie może** składać zamówień (API zwraca 403).
- Konfiguracja: `@PreAuthorize("hasRole('MANAGER')")` itd. na kontrolerach/serwisach.

### 6.3 Server-Side Validation (Jakarta Validation)

- **Druga linia obrony** po walidacji frontendowej; ochrona integralności bazy.
- Adnotacje na DTO: `@NotNull`, `@Size`, `@Min`, `@Max`, `@Email`, `@Pattern`.
- **Ochrona przed Injection Attacks:**  
  - Wszystkie wejścia użytkownika (Imię, Nazwisko, opis, adres itd.) walidowane pod kątem **dopuszczalnych znaków** i długości; **blokowanie wpisywania kodu SQL** lub skryptów (np. `@Pattern` odrzucający znaki `;`, `--`, `'`, `"`, `<`, `>` w polach tekstowych).  
  - Zapytania do bazy wyłącznie przez **parametryzowane zapytania** (JPA/Hibernate) – brak konkatenacji SQL z wejściem użytkownika.
- Błędy walidacji: **400** z listą pól i komunikatów.

### 6.4 Rate Limiting (Bucket4j)

- **Logowanie:** **10 prób na 15 minut na jedno IP** (endpoint `/api/auth/login`). Po przekroczeniu: **429** + `Retry-After`.
- **Wyszukiwarka produktów:** **5 zapytań na sekundę na IP** (np. `GET /api/products` z parametrem `q`).
- Opcjonalnie: limity na inne endpointy publiczne lub kosztowne.

### 6.5 Hashowanie haseł

- Wyłącznie **BCrypt** (np. `BCryptPasswordEncoder` w Spring Security).

### 6.6 Inne

- **HTTPS** w produkcji.
- **CORS** – skonfigurowane originy dla Web, Mobile, Desktop.

---

## 7. WebSocket (STOMP) – powiadomienia w czasie rzeczywistym

- **Endpoint:** `ws://host:port/ws` (SockJS fallback).
- **Protokół:** STOMP nad WebSocket.
- **Temat dla klienta (Web):** `/user/queue/order-updates` – subskrypcja zalogowanego użytkownika; serwer wysyła payload przy **zmianie statusu zamówienia** (np. „W dostawie”, „Dostarczone”), aby Web mógł odświeżyć widok bez odświeżania strony.
- **Topic ogólny (manager/employee):** np. `/topic/order-updates` – opcjonalnie dla listy zamówień na Mobile/Desktop.
- **Zabezpieczenie:** subskrypcja `/user/queue/*` tylko do własnej sesji; topic ogólny tylko dla EMPLOYEE/MANAGER.

---

## 8. Instrukcja uruchomienia

### 8.1 Wymagania

- **Java 21** (JDK).
- **Maven 3.9+**.
- **Docker** (dla PostgreSQL i opcjonalnie aplikacji).
- Zmienne środowiskowe / `application.yml`: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`, `jwt.secret`, `jwt.access-ttl`, `jwt.refresh-ttl`.

### 8.2 Baza danych (Docker)

```bash
docker run -d --name alcohol-db \
  -e POSTGRES_USER=app \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_DB=alcohol_db \
  -p 5432:5432 \
  postgres:15-alpine
```

### 8.3 Budowanie i uruchomienie (Maven)

```bash
./mvnw clean package -DskipTests
java -jar target/nazwa-projektu-0.0.1-SNAPSHOT.jar
```

Lub w trybie deweloperskim:

```bash
./mvnw spring-boot:run
```

### 8.4 Docker (aplikacja)

Przykład `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

W `docker-compose.yml`: serwis aplikacji zależny od `postgres`; zmienne środowiskowe z pliku `.env` lub wpisane w compose.

### 8.5 Po uruchomieniu

- **API:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/docs`
- **WebSocket:** `ws://localhost:8080/ws`

---

## 9. Podsumowanie

- **API** realizuje centralną logikę biznesową, REST + WebSocket (STOMP), z pełnym cyklem zamówień, zamówieniami niestandardowymi, inwentarzem (produkty + surowce), dostawami, HR, raportami i modułem kadr (ofert pracy, zatrudnianie/zwalnianie).
- **Zabezpieczenia:** JWT, RBAC (GUEST, CUSTOMER, EMPLOYEE, MANAGER), Jakarta Validation (w tym ochrona przed injection), Rate limiting (10 logowań/15 min, 5 req/s wyszukiwarka), BCrypt.
- **Web:** zamawianie, śledzenie, zamówienia niestandardowe, gość + potwierdzenie 18+, real-time status; **Mobile:** inwentarz, lista niestandardowych, dostawy z adresami, HR, zmiana statusów; **Desktop:** magazyn, replenishment, kurierzy, ogłoszenia dostaw, oferty pracy, zatrudnianie/zwalnianie, raporty.

Dokument można uzupełnić o szczegółowe schematy request/response (OpenAPI) oraz migracje bazy (Flyway/Liquibase).

---

## 10. Warunki zaliczenia projektu (wykładowca)

Zasady zaliczenia zajęć projektowych (ocena Ω, praca projektowa, sprawozdanie, prezentacja, GitHub Flow, Issues, OpenAPI przy wdrożeniu) są zebrane w osobnym dokumencie: **[WARUNKI_ZALICZENIA.md](./WARUNKI_ZALICZENIA.md)** – checklista dla zespołu i wymagane sekcje sprawozdania.
