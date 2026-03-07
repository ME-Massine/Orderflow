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
- Versioned REST APIs with stable contracts
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

Milestone: Event Consumption and Reliability Foundations (`v0.9.0`)

Status: Implemented, Tests Passing, Ready for Release Tag

Scope of milestone (complete):

- Messaging Publisher (from `v0.8.0`)
    - Messaging boundary introduced
    - `EventPublisher` abstraction
    - `RabbitMqEventPublisher` implementation
    - `OrderCreatedEvent` event contract
    - `RabbitMqConfig` messaging topology
    - `OrderService` publishes event after successful order creation
- Event Consumer
    - Introduced `OrderCreatedEventConsumer`
    - Uses `@RabbitListener` to consume events
    - Demonstrates end-to-end event flow
- Retry Strategy
    - Implemented using `Spring AMQP RetryInterceptor`:
        - Automatic retry for failed message processing
        - Configurable exponential backoff
        - Maximum retry attempts
- Dead Letter Queue Strategy
    - Failed events republished to `DLQ` exchange
    - Messages routed to `order.created.dlq`
- Idempotency Guard
    - Consumer-side duplicate protection implemented via:
        - `IdempotencyStore` abstraction
        - `InMemoryIdempotencyStore` implementation
    - Prevents duplicate processing caused by at-least-once delivery semantics.
- Event Handler Abstraction
    - Consumer logic separated via:
        - `OrderCreatedEventHandler`
        - `LoggingOrderCreatedEventHandler`
    - This keeps the consumer thin and allows future service integrations.
- Documentation Updates
    - README updated with:
        - Event-driven architecture diagram
        - Sequence diagram for event lifecycle
        - Messaging infrastructure description
        - Event contract schema
- Verification
    - All tests pass:
      ```bash
      mvn clean test
      ```
    - Test results:
      ```
      Tests run: 21
      Failures: 0
      Errors: 0
      Skipped: 0
      ```
    - `JaCoCo` coverage report generated successfully.

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
- `dev`/`docker` → `PostgreSQL`  
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
- `IdempotencyStore`  
  Status: Implemented (`v0.9.0`)

## ADR-013: Retry + DLQ Strategy
Message processing failures handled using:
- `Spring AMQP` retry interceptor
- `DLQ` republish recoverer  
  Status: Implemented (`v0.9.0`)

---

# 4. Implemented Components

## Core Architecture
- Strict layered architecture with responsibility separation.

## Domain Layer
- `Order`
- `OrderStatus`

## Service Layer
- Encapsulated business logic with transactional boundaries.
- Key behavior:
    - create order
    - update order status
    - list orders with pagination
- Event publishing triggered on successful persistence.

## Persistence Layer
- Spring Data repository:
    - `OrderRepository`

## Web Layer
- Versioned REST endpoints:
    - `/api/v1/orders`
- Validation via:
    - `@Valid`
- Pagination contract via:
    - `OrderPageResponse`
- Centralized error handling.

## Messaging Layer

### Publisher
- `EventPublisher`
- `RabbitMqEventPublisher`
- `OrderCreatedEvent`

### Messaging Configuration
- `RabbitMqConfig`
- `RabbitListenerRetryConfig`
- Defines:
    - Exchange
    - Routing keys
    - Queue
    - Dead letter exchange
    - Retry interceptor

### Consumer
- `OrderCreatedEventConsumer`
- Responsibilities:
    - consume events
    - ensure idempotency
    - delegate to handler

### Handler
- `OrderCreatedEventHandler`
- `LoggingOrderCreatedEventHandler`
- Current behavior:
    - Logs consumption of events.
- Future behavior may integrate additional services.

### Idempotency
- `IdempotencyStore`
- `InMemoryIdempotencyStore`
- Ensures duplicate events are ignored.

## Observability
- Spring Boot Actuator endpoints enabled.

## OpenAPI
- Swagger UI verified and documented.

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

### List Orders
`GET /api/v1/orders?page=0&size=10`

Returns:
- `OrderPageResponse`

### Update Order Status
`PATCH /api/v1/orders/{id}/status`

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
    - JSON response contracts

## Repository Tests
- `@DataJpaTest`
- Uses `H2` in-memory database.
- Validates:
    - entity mapping
    - persistence behavior

## Service Unit Tests
- Mockito-based tests validate:
    - order creation
    - pagination
    - status updates
    - NotFound scenarios

## Messaging Tests
- Consumer tests verify:
    - first-time processing
    - duplicate event skipping
    - invalid payload rejection

Test results:
```
Tests run: 21
Failures: 0
Errors: 0
```

---

# 7. Code Quality and CI

## JaCoCo
- Coverage reports generated locally and in CI.
- Report path:
  `services/order-service/target/site/jacoco/index.html`

## Codecov
- Coverage uploaded through GitHub Actions.
- Badge included in README.

## CI Pipeline
- Pipeline steps:
    - build
    - test
    - coverage
    - coverage enforcement
    - Codecov upload

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

Root `/` is not mapped; endpoints are API and docs focused.

`RabbitMQ` runtime requires broker availability (Docker compose or local broker, depending on setup).

---

# 9. Versioning and Releases

Current release:
- `v0.9.0`

Release discipline:
- commit → tests → tag → push → release

Version badge now automated via GitHub tag badge.

Manual version badges removed from README.

---

# 10. Planned Next Milestone

Milestone: `DLQ` Handling and Failure Observability (`v0.10.0`)

Goal:
Strengthen reliability and operational visibility.

Planned scope:

- `DLQ` consumer implementation
- Failure logging with metadata
- Event failure observability
- Message retry metrics
- Operational debugging support

Architecture extension:
- `publisher` → `RabbitMQ` → `consumer` → `retry` → `DLQ` → `DLQ` consumer

---

# 11. Known Issues / Observations

1. `@MockBean` deprecation warnings in `Spring Boot 3.5.x` tests (non-breaking)
2. `RabbitMQ` connection attempts appear in tests when broker is not running
3. Test profile now disables `Rabbit` listener auto-start to avoid unnecessary broker connections
4. Maven dependency duplication previously detected for `AMQP` dependency (resolved)

---

# 12. Current Stability Assessment

Build: Passing
Tests: Passing
Coverage: Enforced
CI: Operational
Docker runtime: Verified
OpenAPI contract: Stable
Messaging: Publisher + Consumer + Retry + Idempotency implemented

Project maturity level:
Production-grade backend service with:
- stable API contracts
- enforced quality gates
- event-driven messaging
- reliability primitives
- extensible architecture

---

END OF FILE
