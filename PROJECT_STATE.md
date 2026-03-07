# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: `Spring Boot 3`, `PostgreSQL`, `RabbitMQ`, `JPA`, `Actuator`  
Language: `Java 17`

Repository layout: `mono-repo` (`services/order-service`)

---

# 1. Project Vision

`OrderFlow` is a production-oriented backend service designed to demonstrate real-world backend engineering practices and portfolio-grade architectural maturity.

The project focuses on building a backend system incrementally while applying disciplined engineering principles.

Core goals include:

- Clean layered architecture
- Versioned `REST APIs`
- `DTO` isolation from persistence entities
- Structured error handling
- Test isolation and multi-layer testing
- Containerized runtime environment
- `CI`-driven quality enforcement
- Event-driven architecture
- Messaging reliability patterns
- Observability and operational visibility

Each milestone introduces new system capabilities commonly found in production systems.

---

# 2. Current Milestone

Milestone: `DLQ` Handling and Failure Observability (`v0.10.0`)
Status: Implemented – Tests Passing – Ready for Release Tag

## Milestone Scope

### Messaging Publisher Foundations (`v0.8.0`)

Messaging infrastructure introduced.

Implemented components:

- `EventPublisher` abstraction
- `RabbitMqEventPublisher` implementation
- `OrderCreatedEvent` event contract
- `RabbitMqConfig` messaging topology
- `OrderService` publishes event after successful order creation

### Event Consumption and Reliability (`v0.9.0`)

Consumer infrastructure introduced.

Implemented:

- `OrderCreatedEventConsumer`
- `@RabbitListener` consumer
- Retry strategy using `Spring AMQP`
- `Dead Letter Queue` republish strategy
- Idempotency guard implementation

Components:

- `IdempotencyStore`
- `InMemoryIdempotencyStore`
- `OrderCreatedEventHandler`
- `LoggingOrderCreatedEventHandler`

Tests added:

- consumer processing
- duplicate event skipping
- invalid payload rejection

### DLQ Handling and Failure Observability (`v0.10.0`)

Operational reliability improvements introduced.

New components:

#### DLQ Consumer

- `OrderCreatedDlqConsumer`
- Consumes messages from `order.created.dlq`
- Logs full diagnostic context for failed events

#### Messaging Metrics

- `MessagingMetrics` component
- `Micrometer` counters added

Metrics tracked:

- `orderflow.messaging.events.consumed`
- `orderflow.messaging.events.duplicates`
- `orderflow.messaging.events.failed`
- `orderflow.messaging.events.dlq`

#### Improved Retry Configuration

`RabbitListenerRetryConfig` enhanced to attach diagnostic headers:

- `x-original-exchange`
- `x-original-routingKey`

These headers allow `DLQ` consumers to trace message origin.

#### Consumer Instrumentation

`OrderCreatedEventConsumer` now records:

- successful processing
- duplicate events
- invalid payloads
- retry-triggered failures

#### Test Environment Improvement

`Rabbit` listeners disabled during tests to avoid broker dependency:

`spring.rabbitmq.listener.simple.auto-startup=false`

### Verification

Tests executed successfully.

```bash
mvn clean test
```

Results:

```
Tests run: 21
Failures: 0
Errors: 0
Skipped: 0
```

`JaCoCo` coverage report generated.

---

# 3. Architecture Decisions (ADR Style)

## ADR-001 Layered Architecture
`Controller` → `Service` → `Repository` → `Database`

Status: Implemented

## ADR-002 DTO Isolation
Entities are not exposed through `API` responses.

Status: Implemented

## ADR-003 API Versioning
URL versioning strategy:
`/api/v1/...`

Status: Implemented

## ADR-004 Profile-Based Test Isolation
Profiles:

- `test` → `H2`
- `dev`/`docker` → `PostgreSQL`

Status: Implemented

## ADR-005 Maven Wrapper
Ensures reproducible builds.

Status: Implemented

## ADR-006 OpenAPI Documentation
Swagger UI and `OpenAPI` specification available.

Status: Implemented

## ADR-007 Structured Exception Handling
Centralized error responses using:

- `ApiError`
- `ValidationError`
- `GlobalExceptionHandler`

Status: Implemented

## ADR-008 Dockerized Runtime
Application runs via `Docker Compose`.

```bash
docker compose up --build
```

Status: Implemented

## ADR-009 Coverage Enforcement
`JaCoCo` coverage integrated with `CI` and `Codecov`.

Status: Implemented

## ADR-010 Messaging Boundary
Domain logic publishes events through abstraction.

Components:

- `EventPublisher`
- `RabbitMqEventPublisher`

Status: Implemented

## ADR-011 Idempotent Event Consumption
Duplicate message protection via `IdempotencyStore`.

Status: Implemented

## ADR-012 Retry and DLQ Strategy
Message failures handled using:

- `Spring AMQP` retry interceptor
- `RepublishMessageRecoverer`
- `Dead-letter` routing

Status: Implemented

## ADR-013 Dedicated DLQ Processing
Dead-lettered messages are consumed explicitly rather than left unprocessed.

Implemented via:

- `OrderCreatedDlqConsumer`

Status: Implemented

## ADR-014 Messaging Observability
Messaging events tracked using `Micrometer` metrics.

Status: Implemented

---

# 4. Implemented Components

## Core Architecture
- Strict layered architecture with clear responsibility separation.

## Domain Layer

Entities:

- `Order`
- `OrderStatus`

## Service Layer
- Business logic encapsulated inside transactional boundaries.

Responsibilities:

- create order
- update order status
- list orders with pagination
- publish `OrderCreatedEvent`

## Persistence Layer

`Spring Data` repository:

- `OrderRepository`

## Web Layer

`REST` endpoints:

- `/api/v1/orders`

Features:

- `Bean` validation
- structured error responses
- pagination support

## Messaging Layer

### Publisher

Components:

- `EventPublisher`
- `RabbitMqEventPublisher`
- `OrderCreatedEvent`

### Messaging Configuration

Components:

- `RabbitMqConfig`
- `RabbitListenerRetryConfig`

Defines:

- exchanges
- queues
- routing keys
- retry interceptors
- `DLQ` routing

### Event Consumer

- `OrderCreatedEventConsumer`

Responsibilities:

- consume messages
- validate payload
- enforce idempotency
- delegate event handling
- record messaging metrics

### Event Handler

Components:

- `OrderCreatedEventHandler`
- `LoggingOrderCreatedEventHandler`

Current behavior:

- Logs event consumption.

Future behavior:

- Integration with other services.

### Idempotency Protection

Components:

- `IdempotencyStore`
- `InMemoryIdempotencyStore`

Ensures duplicate events are skipped.

### Dead Letter Queue Processing

Component:

- `OrderCreatedDlqConsumer`

Responsibilities:

- consume `DLQ` messages
- increment `DLQ` metrics
- log diagnostic metadata

### Messaging Metrics

Component:

- `MessagingMetrics`

Counters tracked:

- consumed events
- duplicate events
- failed events
- `DLQ` events

## Observability
- `Spring Boot Actuator` enabled.

Available endpoints:

- `/actuator/health`
- `/actuator/metrics`

Messaging metrics exposed through `Micrometer`.

## OpenAPI
- Swagger UI and `OpenAPI JSON` verified.

---

# 5. API Contract Summary

Base path:

- `/api/v1/orders`

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

#### Error Contracts

- `ApiError`

Fields:

- `timestamp`
- `status`
- `error`
- `message`
- `path`

- `ValidationError` extends `ApiError` with:

- `fieldErrors`

---

# 6. Testing Strategy

Three testing layers implemented.

## Web Layer

Tools:

- `WebMvcTest`
- `MockMvc`

Validates:

- `HTTP` status codes
- validation rules
- response structure

## Repository Tests

Tool:

- `DataJpaTest`

Uses `H2` in-memory database.

Validates:

- entity mappings
- persistence behavior

## Service Unit Tests

Tool:

- `Mockito`

Validates:

- order creation
- pagination logic
- status updates
- `NotFound` scenarios
- Messaging Tests

Consumer tests validate:

- first-time processing
- duplicate event skipping
- invalid payload handling

Test results:

```
Tests run: 21
Failures: 0
Errors: 0
```

---

# 7. Code Quality and CI

## JaCoCo

Coverage reports generated locally and in `CI`.

Report location:

`services/order-service/target/site/jacoco/index.html`

## Codecov

Coverage uploaded automatically via `GitHub Actions`.

## CI Pipeline

Pipeline stages:

- build
- test
- coverage
- coverage enforcement
- `Codecov` upload

Status: Operational

---

# 8. Local Runtime

Start environment:

```bash
docker compose up --build
```

Access:

Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

OpenAPI spec: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

Health check: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

---

# 9. Versioning and Releases

Current milestone:

- `v0.10.0`

Release workflow:

- commit → tests → version bump → tag → release

Version badge automatically reflects `GitHub` release tags.

---

# 10. Next Planned Milestone

Milestone: Transactional Outbox Pattern (`v1.0.0`)

Goal:

Guarantee event delivery consistency between database writes and message publication.

Planned scope:

- Outbox table
- transactional event storage
- background event publisher
- durable idempotency
- distributed system reliability improvements

---

# 11. Known Observations

1. `@MockBean` deprecation warnings appear under `Spring Boot 3.5.x` (non-breaking).
2. Messaging runtime requires `RabbitMQ` broker availability.
3. Idempotency implementation is currently in-memory.
4. `DLQ` consumer logs failures but does not persist them.

---

# 12. Current Stability Assessment

Build: Passing
Tests: Passing
Coverage: Enforced
CI: Operational
Docker runtime: Verified

Messaging capabilities:

- publisher
- consumer
- retry
- `DLQ`
- idempotency
- messaging metrics

Project maturity:

Production-grade backend service demonstrating real-world event-driven architecture and reliability patterns.

END OF FILE