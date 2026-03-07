# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: `Spring Boot 3`, `PostgreSQL`, `JPA`, `Actuator`, `OpenAPI`, `RabbitMQ` (AMQP)  
Language: `Java 17`

Repository layout: `mono-repo` (`services/order-service`)

---

# 1. Project Vision

OrderFlow is a production-oriented backend system built to demonstrate:

- Clean layered architecture (`controller` → `service` → `repository` → `persistence`)
- Versioned `REST APIs` with stable contracts
- Validation and structured error handling
- Transaction management and disciplined service boundaries
- Observability via `Actuator`
- CI pipeline enforcement and reproducible builds
- Test isolation using profile-based configuration
- Dockerized runtime for local reproducibility
- Event-driven architecture patterns
- Portfolio-grade backend engineering maturity

The project evolves through incremental versioned milestones, each introducing a new real-world backend capability.

---

# 2. Current Milestone

Milestone: **DLQ Handling and Failure Observability (`v0.10.0`)**

Status: Implemented, Tests Passing, Ready for Version Bump / Tag / Release

Scope of milestone (complete):

## From previous milestones retained

### Messaging Publisher Foundations (`v0.8.0`)
- `EventPublisher` abstraction introduced
- `RabbitMqEventPublisher` implementation added
- `OrderCreatedEvent` event contract added
- `RabbitMqConfig` messaging topology introduced
- `OrderService` publishes `OrderCreatedEvent` after successful order creation

### Event Consumption and Reliability Foundations (`v0.9.0`)
- `OrderCreatedEventConsumer` introduced
- `@RabbitListener` consumer implemented
- `Spring AMQP` retry interceptor added
- `DLQ` republish strategy configured
- `IdempotencyStore` abstraction added
- `InMemoryIdempotencyStore` implementation added
- `OrderCreatedEventHandler` abstraction introduced
- `LoggingOrderCreatedEventHandler` added
- Consumer unit tests added

## New in `v0.10.0`

### DLQ Consumer
- Added `OrderCreatedDlqConsumer`
- Consumes dead-lettered messages from `order.created.dlq`
- Handles failed message visibility explicitly rather than leaving failures opaque

### Failure Observability
- Added `MessagingMetrics`
- Tracks:
    - consumed events
    - duplicate events
    - failed events
    - `DLQ` events
- Uses `Micrometer` counters compatible with `Actuator` metrics exposure

### Improved Consumer Instrumentation
- `OrderCreatedEventConsumer` now records:
    - successful consumptions
    - duplicates skipped by idempotency guard
    - failed payload processing

### DLQ Diagnostics
- `DLQ` consumer logs:
    - queue name
    - original exchange
    - original routing key
    - exception message
    - exception stacktrace
    - raw payload

### Retry Configuration Refinement
- `RabbitListenerRetryConfig` updated to republish failed messages with additional diagnostic headers:
    - `x-original-exchange`
    - `x-original-routingKey`

### Test Runtime Improvement
- `application-test.yml` disables `Rabbit` listener auto-start during tests to avoid unnecessary broker connection attempts

### Verification
- All tests pass:
  ```bash
  mvn clean test
  ```
- Current green test count:
  ```bash
  Tests run: 21
  Failures: 0
  Errors: 0
  Skipped: 0
  ```
- `JaCoCo` report generated successfully

---

# 3. Architecture Decisions (ADR Style)

## ADR-001: Layered Architecture
`Controller` → `Service` → `Repository` → `JPA` → `Database`  
Status: Implemented

## ADR-002: DTO Isolation
Entities are never exposed directly through API contracts.  
Status: Implemented

## ADR-003: API Versioning
URL versioning strategy:
`/api/v1/...`  
Status: Implemented

## ADR-004: Profile-Based Test Isolation
Profiles:
- `test` → `H2`
- `dev` / `docker` → `PostgreSQL`  
  Status: Implemented

## ADR-005: Maven Wrapper Usage
Ensures consistent build environment.  
Status: Implemented

## ADR-006: OpenAPI Contract Specification
Swagger UI and OpenAPI JSON exposed and verified.  
Status: Implemented

## ADR-007: Structured Exception Handling
Centralized error handling using:
- `ApiError`
- `ValidationError`
- `GlobalExceptionHandler`  
  Status: Implemented

## ADR-008: Dockerized Runtime
Local reproducibility via:
```bash
docker compose up --build
```  
Status: Implemented

## ADR-009: Coverage Enforcement
`JaCoCo` integrated with CI.
Coverage uploaded to `Codecov`.  
Status: Implemented

## ADR-010: API Contract Stabilization
Stable pagination envelope:
- `PageResponse`
- `OrderPageResponse`  
  Status: Implemented

## ADR-011: Messaging Boundary (Port-Adapter)
Domain logic publishes events through abstraction.
- `EventPublisher`
- `RabbitMqEventPublisher`  
  Status: Implemented

## ADR-012: Consumer Idempotency Guard
Consumers must tolerate duplicate message delivery.
Strategy:
- `IdempotencyStore` abstraction
- in-memory implementation for current milestone  
  Status: Implemented (`v0.9.0`)

## ADR-013: Retry + DLQ Strategy
Message processing failures handled using:
- `Spring AMQP` retry interceptor
- `RepublishMessageRecoverer`
- dead-letter queue routing  
  Status: Implemented (`v0.9.0`)

## ADR-014: Dedicated DLQ Processing
Dead-lettered messages should be consumed explicitly for diagnostics and operational visibility rather than silently accumulating in queue storage.
Status: Implemented (`v0.10.0`)

## ADR-015: Messaging Metrics via Micrometer
Messaging reliability should be observable through counters exposed via the application's metrics infrastructure.
Status: Implemented (`v0.10.0`)

---

# 4. Implemented Components

## Core Architecture
- Strict layered architecture with clear responsibility separation

## Domain Layer
- `Order`
- `OrderStatus`

## Service Layer
- Encapsulated business logic with transactional boundaries
- Key behavior:
    - create order
    - update order status
    - list orders with pagination
- Event publishing triggered after successful persistence

## Persistence Layer
- Spring Data repository:
    - `OrderRepository`

## Web Layer
- Versioned `REST` endpoints:
    - `/api/v1/orders`
- Validation via:
    - `@Valid`
- Pagination contract via:
    - `OrderPageResponse`
- Centralized error handling

## Messaging Layer

### Publisher
- `EventPublisher`
- `RabbitMqEventPublisher`
- `OrderCreatedEvent`

### Messaging Configuration
- `RabbitMqConfig`
- `RabbitListenerRetryConfig`
- Defines:
    - exchange
    - routing keys
    - queue
    - dead-letter exchange
    - dead-letter routing
    - retry interceptor
    - republish recoverer

### Consumer
- `OrderCreatedEventConsumer`
- Responsibilities:
    - consume events
    - validate payload shape
    - enforce idempotency
    - delegate to handler
    - emit messaging metrics

### Handler
- `OrderCreatedEventHandler`
- `LoggingOrderCreatedEventHandler`
- Current behavior:
    - logs successful event handling
- Future behavior:
    - can be replaced or extended with inventory, payment, notification, or analytics handlers

### Idempotency
- `IdempotencyStore`
- `InMemoryIdempotencyStore`
- Ensures duplicate events are ignored.

### DLQ Processing
- `OrderCreatedDlqConsumer`
- Responsibilities:
    - consume dead-lettered messages
    - increment `DLQ` metric
    - log failure context and payload for debugging

### Messaging Observability
- `MessagingMetrics`
- Counters:
    - `orderflow.messaging.events.consumed`
    - `orderflow.messaging.events.duplicates`
    - `orderflow.messaging.events.failed`
    - `orderflow.messaging.events.dlq`

## Observability
- `Spring Boot Actuator` endpoints enabled
- Messaging metrics integrated with `Micrometer`

## OpenAPI
- Swagger UI verified and documented

---

# 5. API Contract Summary

Base path:
- `/api/v1/orders`

Endpoints:

### Create Order
`POST /api/v1/orders`

Returns:
- `201` → `OrderResponse`

### Get Order
`GET /api/v1/orders/{id}`

Returns:
- `200` → `OrderResponse`
- `404` → `ApiError`

### List Orders
`GET /api/v1/orders?page=0&size=10`

Returns:
- `200` → `OrderPageResponse`

### Update Order Status
`PATCH /api/v1/orders/{id}/status`

Returns:
- `200` → `OrderResponse`
- `400` → `ApiError`
- `404` → `ApiError`

Contracts:

- `ApiError`
    - Fields:
        - `timestamp`
        - `status`
        - `error`
        - `message`
        - `path`
- `ValidationError`
    - Extends `ApiError` with:
        - `fieldErrors`

---

# 6. Testing Strategy

Test layers:

## Web Layer
- `@WebMvcTest`
- `MockMvc`
- Validates:
    - status codes
    - request validation
    - `JSON` response contracts

## Repository Tests
- `@DataJpaTest`
- Uses `H2` in-memory database
- Validates:
    - entity mapping
    - persistence behavior

## Service Unit Tests
- Mockito-based tests validate:
    - order creation
    - pagination
    - status updates
    - `NotFound` scenarios
    - event publication behavior from create flow

## Messaging Tests
- Consumer tests verify:
    - first-time processing
    - duplicate event skipping
    - invalid payload rejection

Current result:
```
Tests run: 21
Failures: 0
Errors: 0
```

Test profile note:
- `Rabbit` listeners are disabled during test startup to avoid broker dependency for standard test execution

---

# 7. Code Quality and CI

## JaCoCo
- Coverage reports generated locally and in CI
- Report path:
  `services/order-service/target/site/jacoco/index.html`

## Codecov
- Coverage uploaded through `GitHub Actions`
- Badge included in README

## CI Pipeline
- Pipeline steps:
    - build
    - test
    - coverage
    - coverage enforcement
    - `Codecov` upload

Status: Operational

---

# 8. Local Runtime

Run:
```bash
docker compose up --build
```

Access:

Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

OpenAPI JSON: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

Health endpoint: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

Notes:

- Root `/` is not mapped; endpoints are API and docs focused
- `RabbitMQ` runtime requires broker availability
- Full messaging flow is most meaningful when `PostgreSQL` and `RabbitMQ` are both running

---

# 9. Versioning and Releases

Current milestone target:
- `v0.10.0`

Current code state:
- `v0.10.0` implementation complete
- version bump / release closure pending unless already applied separately

Release discipline:
- commit → tests → version bump → tag → push → release

Version badge:
- automated via `GitHub` dynamic badge
- manual version badge removed from README

---

# 10. Planned Next Milestone

Milestone: Transactional Delivery Hardening (`v1.0.0`)

Goal:
Make event publication production-grade by addressing database/message consistency guarantees.

Planned scope:

- Outbox pattern
    - persisted outbox event record
    - outbox publisher job / relay
    - design notes for exactly-once illusions vs at-least-once reality
- stronger idempotency story beyond in-memory storage

Architecture extension:
- `service` → `database` transaction
- persist order
- persist outbox record
- outbox publisher → `RabbitMQ`
- `consumer` → `retry` → `DLQ` → `DLQ` consumer

---

# 11. Known Issues / Observations

1. `@MockBean` deprecation warnings in `Spring Boot 3.5.x` tests (non-breaking)
2. Local runtime requires broker availability for full messaging behavior
3. Current idempotency implementation is in-memory and therefore suitable for demonstration, not durable multi-instance production use
4. `DLQ` processing currently logs and counts failed events but does not yet persist failure audit records
5. `pom.xml` and release tags must be kept aligned during milestone closure

---

# 12. Current Stability Assessment

Build: Passing
Tests: Passing
Coverage: Enforced
CI: Operational
Docker runtime: Verified for core app
OpenAPI contract: Stable
Messaging: Publisher + Consumer + Retry + `DLQ` + Idempotency + Metrics implemented

Project maturity level:
Production-oriented backend service with:
- stable API contracts
- enforced quality gates
- event-driven messaging
- reliability primitives
- extensible architecture

---

END OF FILE
