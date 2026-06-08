# Warunki zaliczenia – Projekt zespołowy (Firma produkująca alkohol)

Dokument zbiorczy wymagań wykładowcy. **Ocena końcowa:** Ω = 0,3·k₁ + 0,5·k₂ + 0,2·k₃.  
**Warunek konieczny:** każda składowa kₙ > 2,0. Oddanie pracy wymaga **obecności całego zespołu**.

---

## 1. Składowe oceny

| Symbol | Składowa | Waga |
|--------|----------|------|
| **k₁** | Ocena za **pracę projektową** | 30% |
| **k₂** | Ocena za **sprawozdanie** z pracy projektowej | 50% |
| **k₃** | Ocena za **prezentację** pracy projektowej | 20% |

---

## 2. Praca projektowa – wymagania

- Projekt jest **powiązany merytorycznie** z zajęciami **Programowanie urządzeń mobilnych** – w ramach projektu mają zostać zbudowane:
  - **API** (Java/Spring Boot – Twoja część),
  - **webowy panel administracyjny** do zarządzania danymi dla aplikacji mobilnej,
  - **aplikacja mobilna** (Flutter – z zajęć).
- Projekt jest wykonywany w **tych samych grupach** co na zajęciach z Programowania urządzeń mobilnych.

### Terminy i modyfikatory (−0,5 do Ω)

| Wymaganie | Termin | Konsekwencja niedotrzymania |
|-----------|--------|-----------------------------|
| **Skład grup + link do publicznego repozytorium/ów** | Najpóźniej do **drugich** zajęć projektowych, zgłoszenie prowadzącemu | **−0,5** do oceny końcowej |
| **Lista zadań na GitHub Issues** | Ukończona przed **czwartymi** zajęciami | **−0,5** do oceny końcowej |
| **GitHub Flow** (patrz poniżej) | Przez cały czas trwania projektu | **−0,5** do oceny końcowej |

### GitHub Issues (do 4. zajęć)

- Lista zadań na **GitHub Issues** musi:
  - zawierać **szczegółowo opisane wszystkie wymagania funkcjonalne**,
  - mieć **przypisane osoby** do zadań.

### GitHub Flow (obowiązkowo)

- Zmiany na **głównym branchu** tylko przez **pull requesty**.
- Każdy **pull request** musi otrzymać **code review** od pozostałych członków zespołu.
- PR może być dołączony do głównego brancha **dopiero po**:
  - code review,
  - przejściu **testów i linterów na CI**.
- Należy stosować:
  - **squashowanie commitów** w PR,
  - **podpisywanie PR-ów numerami zadań** z Issues (np. „Fixes #12”, „Closes #7”).

### Technologie

- Część projektowa może być wykonana w **dowolnym języku/językach**, z dowolnymi bibliotekami, komponentami, wtyczkami (w Waszym przypadku: Java/Spring Boot, Next.js, Flutter, Python/Flet, PostgreSQL, Docker itd.).

---

## 3. Sprawozdanie – wymagania

- **Format:** część opisowa w **LaTeX**, oddana w formacie **PDF** (zgodnie ze standardami naukowymi).
- **Termin:** najpóźniej w **dniu oddania całego projektu**, w formie elektronicznej (najlepiej **dodane do repozytorium**).

### Obowiązkowe rozdziały / sekcje sprawozdania

1. **Opis przedmiotu zamówienia** dostarczony przez prowadzącego (temat projektu: Firma produkująca alkohol).
2. **Opis technologiczny** zaproponowanego rozwiązania (stos technologiczny: Web, Mobile, Desktop, API, baza, Docker itd.).
3. **Podział obowiązków i odpowiedzialności** w zespole (kto za co odpowiada).
4. **Rozpisane przez zespół zadania** wraz z **czasochłonnością** (np. powiązane z Issues).
5. **Instrukcja lokalnego i zdalnego uruchomienia systemu** (kroki dla każdej części: API, panel web, mobilka, desktop; Docker, zmienne środowiskowe itd.).

### Dodatkowy wymóg

- Projekt musi być wyposażony w **dokumentację OpenAPI** **dołączoną do wyhostowanej aplikacji** (np. Swagger UI pod `/docs` na wdrożonym API – Springdoc spełnia ten warunek).

---

## 4. Prezentacja pracy projektowej

- Prezentacja odbywa się **całym zespołem**.
- Forma: **przedstawienie działającej i wdrożonej aplikacji** (live demo).
- Aplikacja musi być **wdrożona** (zdalnie dostępna), nie tylko uruchomiona lokalnie.

---

## 5. Checklist dla zespołu (do odhaczenia)

### Na początku semestru (do 2. zajęć)

- [ ] Skład grup ustalony i zgodny z zajęciami z Programowania urządzeń mobilnych.
- [ ] Link do publicznego repozytorium/ów zgłoszony prowadzącemu.

### Przed 4. zajęciami

- [ ] GitHub Issues utworzone i ukończone.
- [ ] W Issues: wszystkie wymagania funkcjonalne szczegółowo opisane.
- [ ] Do każdego zadania przypisana osoba.

### W trakcie projektu

- [ ] Zmiany na main/master tylko przez pull requesty.
- [ ] Każdy PR ma code review od innych członków zespołu.
- [ ] CI: testy i lintery; merge po przejściu CI i code review.
- [ ] API: job **Code style** w GitHub Actions (Spotless + Checkstyle) — `.github/workflows/ci.yml`.
- [ ] PR-y squashowane i podpisane numerami z Issues (np. „Fixes #5”).

### Przed oddaniem

- [ ] Sprawozdanie w LaTeX, oddane w PDF (w repozytorium).
- [ ] Sprawozdanie zawiera: opis przedmiotu, opis technologiczny, podział obowiązków, zadania z czasochłonnością, instrukcję uruchomienia (lokalnie i zdalnie).
- [ ] Wyhostowana aplikacja ma dostępną dokumentację OpenAPI (np. `/docs`).

### Prezentacja

- [ ] Aplikacja wdrożona i działająca.
- [ ] Prezentacja całego zespołu – demo działającej aplikacji.

---

*Dokument pomocniczy; w razie wątpliwości decyduje treść przekazana przez prowadzącego.*
