# PlayStation Tracker (PS-Tracker)
![Java 21](https://img.shields.io/badge/Java-21-ED8B00.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-brightgreen.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

A Spring Boot backend service that polls PlayStation store prices, evaluates target thresholds and automates
alert notifications to specific chat sessions.

---

## About the Project
(todo)

---

## Built With
* **Language:** Java 21
* **Framework:** Spring Boot (Spring Data JPA, Validation, WebMVC)
* **Database:** PostgreSQL 15
* **Persistence & ORM:** Hibernate ORM
* **Testing Infrastructure:** JUnit 5, Testcontainers, AssertJ
* **Containerization:** Docker & Docker Compose
---

## Database Design

![Database Schema](docs/images/schema.png)

### Structural Decisions:
* **Associative Entity Resolution:** The many-to-many (N:M) relationship between a Telegram `Chat` and a game `Edition`
is resolved via the `Tracker` entity. This prevents hidden join tables and allows the relationship itself to hold 
business logic (e.g., specific `target_price` thresholds).
* **Composite Primary Keys:** A user should only be able to track a specific edition once. This uniqueness is guaranteed 
at the database level using a composite key (`id_chat`, `id_edition`) implemented via JPA's `@EmbeddedId` and mapped 
cleanly using `@MapsId`.
* **Financial Precision:** Floating-point math is notoriously dangerous for currency. All monetary values are strictly 
mapped to PostgreSQL's `numeric(5,2)` via Java's `BigDecimal` to ensure absolute precision when triggering price drop alerts.
* **External ID Mapping:** Instead of relying on auto-generated sequences for users, the application directly 
assigns Telegram's native `chat_id` as the Primary Key. This removes the need for expensive lookup queries during 
webhook processing.

---

## Project Structure
```text
francisco.ps.tracker
├── game/           # Core Domain: Game, Edition, Repositories
├── chat/           # Core Domain: Chat mappings and types
├── tracker/        # Core Domain: Associative Entity, composite keys, thresholds
```

---

## Tests
1. **Data Layer Integration (`TrackerRepositoryTest`):** Validates the composite keys, constraints, and persistence 
logic using `@DataJpaTest`. Uses **Testcontainers** to run against a real, temporary PostgreSQL Docker container rather
than an in-memory H2 mock, leveraging `TestEntityManager.flush()` to ensure SQL queries hit the disk.

(todo)

---

## How to Run
(todo)
