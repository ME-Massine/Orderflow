# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: `Spring Boot 3`, `PostgreSQL`, `JPA`, `Actuator`, `OpenAPI`, `RabbitMQ` (AMQP)  
Language: `Java 17`

Repository layout: `mono-repo` (`services/order-service`)

---

# 1. Project Vision

OrderFlow is a production-oriented backend system built to demonstrate:

- Clean layered architecture (`controller` -> `service` -> `repository` -> `persistence`)
- Versioned REST APIs with stable contracts
- Validation and structured error handling
- Transaction management and disciplined service boundaries
- Observability via `Actuator`
- CI pipeline enforcement and reproducible builds
- Test isolation using profile-based configuration
- Dockerized runtime for local reproducibility
- Event-driven foundations via messaging, without breaking layered architecture
- Portfolio-grade engineering maturity

---

# 2. Current Milestone

Milestone: Event-Driven Foundations with `RabbitMQ` (`v0.8.0`)

Status: Implemented, Tests Passing, Ready to Tag and Release

Scope of milestone (complete):

- Messaging boundary introduced (no coupling to `RabbitMQ` in domain logic)
    - Added `EventPublisher` abstraction
    - `RabbitMqEventPublisher` implements `EventPublisher` using `Spring AMQP`
- Event contract introduced
    - `OrderCreatedEvent` published after successful order creation
- `RabbitMQ` wiring added
    - `RabbitMqConfig` defines exchange / routing / queue binding (project-level messaging topology)
- Service integrates publishing
    - `OrderService` publishes `OrderCreatedEvent` after persistence
- Documentation upgraded
    - README now documents layered architecture and event-driven architecture
    - Includes both flowchart and sequence diagram for event flow
    - Event contract schema documented
- Verification
    - `mvn clean test` passes fully
    - `WebMvcTest`, `DataJpaTest`, and service unit tests remain green

---

# 3. Architecture Decisions (ADR Style)

## ADR-001: Layered Architecture
`Controller` -> `Service` -> `Repository` -> `JPA` -> `Database`  
Status: Implemented

## ADR-002: DTO Isolation
Entities are not exposed directly through API contracts.  
Status: Implemented

## ADR-003: API Versioning
URL-based versioning `/api/v1/...`  
Status: Implemented

## ADR-004: Profile-Based Test Isolation
H2 for tests, PostgreSQL for prod/docker.  
Status: Implemented

## ADR-005: Maven Wrapper Usage
Consistent build tool versioning across environments.  
Status: Implemented

## ADR-006: OpenAPI Integration and Verification
Swagger UI and OpenAPI JSON available at runtime.  
Status: Implemented and verified

## ADR-007: Structured Exception Handling
Centralized error mapping with stable DTOs and correct HTTP semantics.  
Status: Implemented and stabilized

## ADR-008: Dockerized Local Environment
Reproducible runtime via `Docker Compose`.  
Status: Implemented

## ADR-009: Coverage Enforcement Strategy
`JaCoCo` threshold enforcement in CI.  
Status: Implemented

## ADR-010: API Contract Stabilization
Stable pagination envelope and typed error contract.  
Status: Implemented

## ADR-011: OpenAPI Contract Specification
OpenAPI made explicit and consumer-friendly with concrete schemas.  
Status: Implemented

## ADR-012: Messaging Boundary via Port-Adapter
Publish events behind an abstraction, keep `RabbitMQ` as an adapter.  
Status: Implemented (`v0.8.0`)

---

# 4. Implemented Components

## Core Architecture
- Strict layered design
- Clear package separation aligned with responsibilities

## Domain Layer
- `Order` entity
- `OrderStatus` enum
- Persistence defaults (lifecycle handling)

## Service Layer
- Business logic encapsulated
- Transaction boundaries defined (`@Transactional`)
- Dirty checking update strategy

## Persistence Layer
- Spring Data JPA repository (`OrderRepository`)

## Web Layer
- Versioned endpoints (`/api/v1/orders`)
- Bean Validation integrated (`@Valid`)
- Pagination exposed via stable response envelope (`PageResponse` / `OrderPageResponse`)
- Typed error handling via centralized controller advice

## Messaging Layer (`v0.8.0`)
- `messaging/config`
    - `RabbitMqConfig` for exchange / queue / binding
- `messaging/event`
    - `OrderCreatedEvent` immutable event DTO
- `messaging/publisher`
    - `EventPublisher` interface
    - `RabbitMqEventPublisher` implementation

## Observability
- Spring Boot Actuator enabled and verified

## OpenAPI / Swagger
- Swagger UI and OpenAPI JSON exposed
- Endpoint-level annotations include responses and schema contracts
- Swagger UI shows correct API version

---

# 5. API Contract Summary

Base path:
- `/api/v1/orders`

Endpoints:
- `POST /api/v1/orders`
    - `201`: `OrderResponse`
    - `400`: `ValidationError`
- `GET /api/v1/orders/{id}`
    - `200`: `OrderResponse`
    - `404`: `ApiError`
- `GET /api/v1/orders?page=0&size=10`
    - `200`: `OrderPageResponse` (concrete schema for OpenAPI)
- `PATCH /api/v1/orders/{id}/status?status=CONFIRMED`
    - `200`: `OrderResponse`
    - `400`: `ApiError`
    - `404`: `ApiError`

Contracts:
- `OrderPageResponse` fields:
    - `content`, `page`, `size`, `totalElements`, `totalPages`
- `ApiError` fields:
    - `timestamp`, `status`, `error`, `message`, `path`
- `ValidationError` fields:
    - `timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`

---

# 6. Testing Strategy

Test layers:

## Web Layer Contract Tests
- `@WebMvcTest` + `MockMvc`
- Validates status codes, JSON contract shape, validation errors, enum mismatch behavior

## Repository Slice Tests
- `@DataJpaTest`
- H2 in-memory database
- Mapping and persistence verification

## Service Unit Tests
- Mockito-based unit tests
- NotFound scenarios
- Behavior validation for listing and status updates
- `v0.8.0` update: ensure event publisher is invoked for create flow (where applicable)

Current status:
- `mvn clean test` green

---

# 7. Code Quality and CI

## JaCoCo Coverage
- HTML report:
  `services/order-service/target/site/jacoco/index.html`
- XML for CI and Codecov
- Threshold enforcement enabled in CI

## Codecov
- Coverage uploaded through GitHub Actions
- Badge integrated in README

## CI Workflow
- Build -> Test -> Coverage report -> Coverage enforcement -> Codecov upload

Status: Operational

---

# 8. Local Runtime

Docker run:
```bash
docker compose up --build
```

Access:

Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

OpenAPI JSON: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

Health: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

Notes:

Root `/` is not mapped; endpoints are API and docs focused.

`RabbitMQ` runtime requires broker availability (Docker compose or local broker, depending on setup).

---

# 9. Versioning and Releases

Current target release: `v0.8.0`
Current code state: `RabbitMQ` event publishing introduced + documentation updated

Release rule:

1. Implement changes
2. Run tests
3. Commit
4. Tag
5. Push tag
6. Publish GitHub Release

Release tooling note:

`GitHub CLI` (`gh`) is not installed on current environment, so release creation must be done either:

- via GitHub web UI, or
- after installing `GitHub CLI` on the machine.

---

# 10. Planned Next Milestone

Milestone: Event Consumption and Reliability Hooks (`v0.9.0`)

Goal:
Move from "publish only" to "publish + consume" and introduce reliability patterns.

Planned scope:

- Add a consumer example (`@RabbitListener`) to prove end-to-end event-driven flow
- Introduce dead-letter queue handling (DLQ) and retry strategy (design-level or basic config)
- Add idempotency guardrails for consumer side (design notes or minimal implementation)
- Document delivery semantics and failure modes

---

# 11. Known Issues / Observations

1. `@MockBean` deprecation warnings under `Spring Boot 3.5.x` during tests (build still succeeds)
2. Ensure README version badge matches latest tag after tagging and releasing
3. `GitHub CLI` not available (`gh release create` fails) - release must be created via UI or install `gh`
4. Maven warning previously observed: duplicate `spring-boot-starter-amqp` dependency. Ensure `pom.xml` contains a single declaration.

---

# 12. Current Stability Assessment

Build: Passing
Tests: Passing
Coverage: Enforced
CI: Operational
Docker runtime: Verified
OpenAPI contract: Explicit, stable, and verified in Swagger UI
Architecture: Clean, extensible, event-ready

Project maturity level:
Portfolio-grade backend service with stable API contracts, enforced quality gates, and event-driven foundations via `RabbitMQ`.

---

END OF FILE
