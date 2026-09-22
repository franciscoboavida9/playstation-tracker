<div align="center">

# PlayStation Tracker

  <p>
    <img src="https://img.shields.io/badge/PlayStation-0070D1?style=for-the-badge&logo=playstation&logoColor=fff" alt="PlayStation" />
  </p>

  <p><b>Track PlayStation Store (🇵🇹) prices and get notified on discounts</b></p>

  <!-- Live Bot CTA Button -->
  <p>
    <a href="https://t.me/PsPriceTrackerBot">
      <img src="https://img.shields.io/badge/_START_BOT_ON_TELEGRAM-2496ED?style=for-the-badge&logo=telegram&logoColor=white" alt="Start Bot" />
    </a>
  </p>

</div>

A Spring Boot backend service that polls PlayStation store prices, evaluates target thresholds and automates
alert notifications to specific chat sessions.

---

## About the Project
PS-Tracker abstracts the complexity of the undocumented Sony GraphQL API into a seamless Telegram Bot experience. 
Users can search the PlayStation Store, add games to a wishlist, and instantly see active discounts directly within 
their chat client, without needing to navigate the web store. The MVP is fully containerized and currently self-hosted
24/7 on a headless Raspberry Pi.

---

## How to Use
You can interact with the live bot directly on Telegram: **[@PsPriceTrackerBot](https://t.me/PsPriceTrackerBot)** 

Interact with the bot directly via Telegram using the following commands:
* `/start` - Displays the welcome message and basic instructions.
* `/search <game name>` - Queries the PlayStation store and returns the top 3 results with accurate pricing.
* `/wishlist` - Retrieves your personal list of tracked games and their current discount status.
* **Inline Buttons:** Use the interactive "Track Game" and "Stop Tracking" buttons attached to bot messages 
to manage your wishlist without typing.

---

### Built With

<ul style="list-style-type: disc;">
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Java_21-%23ED8B00.svg?logo=openjdk&logoColor=white" alt="Java" style="vertical-align: middle;" /> — <em>Base Java 21 execution environment</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Spring_Boot_4.1.0-6DB33F?logo=springboot&logoColor=fff" alt="Spring Boot" style="vertical-align: middle;" /> — <em>Application core & REST client</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Postgres_15-%23316192.svg?logo=postgresql&logoColor=white" alt="PostgreSQL" style="vertical-align: middle;" /> — <em>Reliable relational storage for prices & user tracking</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Hibernate-59666C?logo=hibernate&logoColor=white" alt="Hibernate" style="vertical-align: middle;" /> — <em>ORM for seamless database mapping</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Telegram_Bot_API-2CA5E0?logo=telegram&logoColor=white" alt="Telegram" style="vertical-align: middle;" /> — <em>Long polling interface for real-time user alerts</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Caffeine_Cache-FF5722?logo=coffeescript&logoColor=white" alt="Caffeine Cache" style="vertical-align: middle;" /> — <em>In-memory TTL cache to optimize search pagination</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=fff" alt="Docker" style="vertical-align: middle;" /> — <em>Containerized multi-stage builds</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Raspberry_Pi_5-cd2355.svg?logo=raspberrypi" alt="Raspberry Pi" style="vertical-align: middle;" /> — <em>24/7 self-hosted local deployment server</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/JUnit_5-4C956C?logo=junit5&logoColor=fff" alt="JUnit" style="vertical-align: middle;" /> — <em>Unit testing framework</em></li>
  <li style="margin-bottom: 8px;"><img src="https://img.shields.io/badge/Testcontainers-008080?logo=codesandbox&logoColor=white" alt="Testcontainers" style="vertical-align: middle;" /> — <em>Integration tests backed by real Postgres containers</em></li>
</ul>

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

* **Financial Precision:** Floating-point math is dangerous for currency so all monetary values are strictly 
mapped to PostgreSQL's `numeric(5,2)` via Java's `BigDecimal` to ensure absolute precision when triggering price drop alerts.

* **External ID Mapping:** Instead of relying on auto-generated sequences for users, the application directly 
assigns Telegram's native `chat_id` as the Primary Key. This removes the need for lookup queries during 
webhook processing.

---

## Architecture & Design Decisions

### Deployment
* **Self-Hosting:** The application is deployed on a headless Raspberry Pi 5, running continuously via SSH management.

* **Multi-Stage Containerization:** A multi-stage `Dockerfile` uses a Maven base to build the `.jar` and a lightweight Alpine
JRE to run it. This prevents source code and build tools from bloating the production runtime image.

* **Isolated Docker Environment:** `docker-compose.yml` manages both the Spring Boot application and the PostgreSQL database 
(with persistent data volumes) inside an isolated internal Docker bridge network.

* **Native ARM64 Build:** To avoid `x86_64` to `arm64` architecture mismatches, the Docker image is built natively on the Raspberry Pi,
fetching source code directly via Git pulls.


### Telegram Bot Integration
* **Long Polling over Webhooks:** Opted for Long Polling for the MVP. It simplifies local development and 
deployment by eliminating the need for exposed ports and reverse proxies (ngrok), while still providing real-time responsiveness.

* **Command Dispatcher (Strategy Pattern):** Incoming Telegram updates are routed through a centralized `CommandDispatcher`. 
This replaces `if/else` blocks with clean, isolated `CommandHandler` classes, making the addition of future commands frictionless.

* **Stateless Callback Routing:** UI interactions (like clicking "Track Game") use Telegram's inline keyboards with 
callback payloads (e.g., `track:<itemId>`). The dispatcher parses this data and routes it to the correct handler, 
requiring zero session state in the application memory.


### Domain & Business Logic
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


### UI/UX & Navigation Architecture
* **Stateful-Feel in a Stateless Environment:** Telegram bots are inherently stateless — every button 
click is an isolated event carrying a `callbackData` payload (capped at 64 bytes). To prevent the bot from having amnesia, 
I combined **Spring Caching** with pagination keys (`srch:index:query`) to simulate a fluid card carousel without
cluttering the chat with vertical walls of images.

* **Caching Strategy & TTL (Time-To-Live):** To support the pagination carousel, search results are temporarily held 
in application memory using Spring Cache backed by **Caffeine**.
    * *Why In-Memory over Distributed (Redis)?* For this stage of the project, an in-memory cache avoids the 
  infrastructure overhead of deploying and maintaining a separate Redis container. Since the app currently runs as a
  single instance, horizontal scaling is not yet required.
    * *Solving Stale Data:* Pricing data is highly volatile. To prevent the cache from serving outdated prices, a 
  strict **10-minute TTL** is enforced. This balances fast UI pagination with data accuracy,
  while protecting Sony's API from rate-limiting. 


### Background Processing and Automation
* **Automated Price Polling (Scheduler):** The application utilizes Spring's `@Scheduled` annotation to run a daily 
background job that audits prices without user interaction.

* **Game Polling:** To avoid  $O(N \times M)$ API calls, the polling logic is game-centric rather than 
user-centric. The database is queried for distinct active games. If 1,000 users are tracking "Elden Ring", the application 
makes exactly 1 API call to Sony, updating the core item before distributing alerts to the subscribed users.

* **Spam Prevention:** When a price drop is detected, the database updates the item's current price and immediately 
adjusts the `target_price` threshold for all notified users to `newPrice - 0.01`. This ensures users are not spammed with 
identical alerts every 24 hours while a week-long sale is active, but will still be notified if the price drops even further.

* **Proactive Notifications:** The scheduler operates independently, injecting the `TelegramClient` to dynamically dispatch
`SendPhoto` alerts to users entirely asynchronously.
 
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

* **Data Aggregation & BFF Handling:** The Sony GraphQL API acts as a Backend-For-Frontend (BFF) optimized for their
web app, meaning data is heavily fragmented. To build a complete `Item`, the `ItemService` acts as an aggregator:
    1. **Search API:** Resolves user text input into exact product IDs.
    2. **Product Details API:** Fetches the exact pricing for the specific SKU (bypassing a known Sony bug where the
  API artificially forces expensive "Deluxe" bundles to the top of results).
    3. **Media API:** Fetches the high-resolution cover art, which is intentionally stripped from the Details API to save 
  bandwidth.
       By stitching these fragmented responses together in the service layer *before* persisting to PostgreSQL, the 
  application ensures high data integrity and good UI formatting. Once a game is tracked, all subsequent reads 
  (like viewing the wishlist) hit the local database, resulting in zero external API calls.

---

## Project Structure
```text
francisco.ps.tracker
├── game/           # Core Domain: Item entity, Repositories, ItemService
├── chat/           # Core Domain: Chat mappings and types
├── tracker/        # Core Domain: Associative Entity, composite keys, TrackerService
├── telegram/       # Bot Interface: CommandDispatcher, Handlers, Bot config
├── infrastructure/ # External Adapters: Sony API integration (SonyStoreClient, DTOs)
├── scheduler/      # Background Jobs: Cron-based PriceAlertScheduler
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

3. **Business Logic Isolation (`TrackerServiceTest`, `ItemServiceTest` and `PriceAlertSchedulerTest`):** Uses Mockito and AssertJ 
to rigorously test edge cases (API null responses, spam tracking prevention, inactive tracker resurrection, and asynchronous price 
drop detection) entirely in memory without relying on the database or network constraints. Ensures the scheduler gracefully continues
polling remaining items even if an external API call for a specific game throws an exception.

4. **Telegram Interface Isolation (`CommandDispatcherTest` and **Handlers**):** Uses Mockito to stub the `TelegramClient` and
`Update` objects. Verifies that the `CommandDispatcher` correctly routes text commands and callback queries to the appropriate
handlers, ensuring the bot formats and executes the expected API responses without making actual network calls to Telegram's 
servers.

