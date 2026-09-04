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
* **Framework:** Spring Boot (Spring Data JPA, Validation, WebMVC, RestClient)
* **Database:** PostgreSQL 15
* **Persistence & ORM:** Hibernate ORM
* **Testing Infrastructure:** JUnit 5, Testcontainers, AssertJ, MockRestServiceServer
* **Containerization:** Docker & Docker Compose

---

## Database Design

![Database Schema](docs/images/schema.png)

### Structural Decisions:
* **Associative Entity Resolution:** The many-to-many (N:M) relationship between a Telegram `Chat` and a game `Item`
is resolved via the `Tracker` entity. This prevents hidden join tables and allows the relationship itself to hold 
business logic (e.g., specific `target_price` thresholds).
* **Composite Primary Keys:** A user should only be able to track a specific item once. This uniqueness is guaranteed 
at the database level using a composite key (`id_chat`, `id_item`) implemented via JPA's `@EmbeddedId` and mapped 
cleanly using `@MapsId`.
* **Financial Precision:** Floating-point math is notoriously dangerous for currency. All monetary values are strictly 
mapped to PostgreSQL's `numeric(5,2)` via Java's `BigDecimal` to ensure absolute precision when triggering price drop alerts.
* **External ID Mapping:** Instead of relying on auto-generated sequences for users, the application directly 
assigns Telegram's native `chat_id` as the Primary Key. This removes the need for expensive lookup queries during 
webhook processing.

### Technical Improvements
* **Schema Evolution (Trade-off):** The project currently utilizes Hibernate's `ddl-auto=update` for rapid prototyping and
  seamless schema generation. While highly efficient for local development, this is not good practice because Hibernate might
  drop an existing column and create a new one, deleting user data when syncing the schema with the java entities. The
  database could be managed with a tool like to **Flyway** to enforce strict, version-controlled SQL migrations and prevent
  accidental data loss.

---

## Architecture & Design Decisions

* **Domain Simplification:** The domain model was simplified by merging `Game` and `Edition` into a single 
`Item` entity. Since the PlayStation Store treats every SKU (Standard, Deluxe) as an individual product with its
own ID, maintaining separate tables introduced unnecessary complexity and database joins.
* **Stateless Search:** To prevent database bloat, user searches query the external API directly
without saving the results. When a user tracks a previously unsaved item, the `TrackerService`
dynamically fetches the item details via its ID and saves them to the database.
* **Target Price Calculation:** In order to meet the idea of notifying a user whenever a game has any discount, the system 
automatically calculates the target threshold as `currentPrice - 0.01` at the moment of tracking. This avoids
complex conditional logic for games already on sale while keeping the schema ready for custom user targets in V2.0.
* **Soft Deletes:** Untracking an item sets an `isActive` boolean flag to `false` instead of executing a hard 
SQL `DELETE`. This preserves user analytics, prevents foreign key cascade issues, and allows seamless 
"resurrection" if a user tracks the game again.
* **Database Delegation:** Retrieving a user's wishlist utilizes Spring Data JPA derived queries
(`findByChatIdAndIsActiveTrue`) to filter records directly at the PostgreSQL level, avoiding the severe 
memory leaks associated with fetching `findAll()` and filtering inside a Java loop.

---

## External API Integration (Sony GraphQL)

### Structural Decisions:
* **Centralized HTTP Client (DRY):** All  requests to the PlayStation store are handled by the 
`SonyStoreClient` adapter. A private generic helper method (`<T> T fetchFromSony`) handles the `RestClient`
HTTP execution to avoid repeating code.
* **Flexible Deserialization:** The Sony GraphQL API returns massive, deeply nested JSON trees. 
The data is mapped into immutable Java `Record` DTOs. Using Jackson's 
`@JsonIgnoreProperties(ignoreUnknown = true)` ensures the application only deserializes the specific data paths it 
needs (like price and ID).
* **Security & CSRF Bypass:** Safely accesses Sony's undocumented API by mimicking a browser, explicitly encoding 
user inputs (to handle spaces/special characters) and enforcing required `apollo-require-preflight` HTTP headers.

---

## Project Structure
```text
francisco.ps.tracker
├── game/           # Core Domain: Item entity, Repositories, ItemService
├── chat/           # Core Domain: Chat mappings and types
├── tracker/        # Core Domain: Associative Entity, composite keys, TrackerService
├── infrastructure/ # External Adapters: Sony API integration (SonyStoreClient, DTO records)
```

---

## Tests
1. **Data Layer Integration (`TrackerRepositoryTest`):** Validates the composite keys, constraints, and persistence 
logic using `@DataJpaTest`. Uses **Testcontainers** to run against a real, temporary PostgreSQL Docker container rather
than an in-memory H2 mock, leveraging `TestEntityManager.flush()` to ensure SQL queries hit the disk.
2. **HTTP Adapter Integration (`SonyStoreClientTest`):** Isolates the HTTP client using `@RestClientTest` and
`MockRestServiceServer`. Proves the client securely builds expected URLs and successfully maps deeply nested JSON
trees into Java records. Avoids testing tautology by utilizing Hamcrest matchers (`containsString`) to verify 
URI encoding dynamically without duplicating massive GraphQL URL strings.
3. **Business Logic Isolation (`TrackerServiceTest` and `ItemServiceTest`):** Uses Mockito and AssertJ to rigorously test
edge cases (API null responses, spam tracking prevention, inactive tracker resurrection) entirely in memory without relying 
on the database or network constraints.

(todo)

---

## Future Improvements
* **Custom Target Prices:** Expand commands to allow users to set specific monetary thresholds instead of 
defaulting to any discount.
* **Scheduled Polling Engine:** Implement a `@Scheduled` background worker to batch-poll the Sony API and evaluate price drops 
against stored targets.
* **Improve UI:** Upgrade the Telegram interface with inline keyboards for paginated wishlist management,
and attach official game cover images to search results and wishlist.
* **Multi-Region Support:** Expand the client architecture to support dynamic locale parameters, allowing users to track prices 
across different international PlayStation Store regions (e.g., US, UK, JP) instead of being locked to the PT store.

---

## How to Run
(todo)
