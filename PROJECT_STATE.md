# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: `Spring Boot 3`, `PostgreSQL`, `JPA`, `Actuator`, `OpenAPI`  
Language: `Java 17`

Repository layout: `mono-repo` (`services/order-service`)

---

# 1. Project Vision

OrderFlow is a production-oriented backend system built to demonstrate:

- Clean layered architecture
- Versioned REST APIs with stable contracts
- Validation and structured error handling
- Transaction management and disciplined service boundaries
- Observability via `Actuator`
- CI pipeline enforcement and reproducible builds
- Test isolation using profile-based configuration
- Dockerized runtime for local reproducibility
- Portfolio-grade engineering maturity

---

# 2. Current Milestone

Milestone: OpenAPI Contract Specification (`v0.7.0`)

Status: Stable, Verified in Swagger UI, Tests Passing

Scope of milestone (complete):

- OpenAPI contract made explicit at the endpoint level
    - `@Operation` summaries and descriptions added
    - Response codes and schemas documented per endpoint
- Concrete pagination schema introduced for OpenAPI
    - Added `OrderPageResponse` so Swagger shows `content: OrderResponse[]` and stable pagination fields
    - List endpoint returns `OrderPageResponse` (not a generic wrapper type in the OpenAPI output)
- Typed error contracts represented in docs
    - `ApiError` for general errors
    - `ValidationError` for validation failures with `fieldErrors`
- Swagger UI version aligned with application version
    - OpenAPI config and `application.yml` updated so Swagger displays the correct service version
- Verification
    - Swagger UI confirms:
        - correct shapes for `OrderResponse`, `OrderPageResponse`, `ApiError`, `ValidationError`
        - correct enum values for `OrderStatus`
    - `mvn clean test` passes fully

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
Status: Implemented (`v0.7.0`)

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
- Pageable-based listing supported

## Persistence Layer
- Spring Data JPA repository (`OrderRepository`)

## Web Layer
- Versioned endpoints (`/api/v1/orders`)
- Bean Validation integrated (`@Valid`)
- Pagination exposed via stable response envelope (`OrderPageResponse`)
- Typed error handling via centralized controller advice

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
    - `200`: `OrderPageResponse`
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

Current status:
- `mvn clean test` green

---

# 7. Code Quality and CI

## JaCoCo Coverage
- Generates HTML report:
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

Note:

Root `/` is not mapped; endpoints are API and docs focused.

---

# 9. Versioning and Releases

Current target release: `v0.7.0`
Current code state: OpenAPI contracts documented and verified

Release rule:

1. Implement changes
2. Run tests
3. Commit
4. Tag
5. Push tag
6. Publish GitHub Release

Documentation-only changes do not require new version tags (unless they affect the release narrative).

---

# 10. Planned Next Milestone

Milestone: Event-Driven Foundations with `RabbitMQ` (`v0.8.0`)

Goal:

Introduce event contracts and a messaging boundary without breaking layered architecture

Planned package layout:

- `messaging/config` for `RabbitMQ` wiring
- `messaging/event` for event contracts (immutable DTOs)
- `messaging/publisher` for publish abstraction and `RabbitMQ` adapter

Initial event:

`OrderCreatedEvent` published after successful order creation

Notes for production-grade follow-ups:

- Outbox pattern for reliable publishing
- Idempotent consumers for safe duplicate delivery handling

---

# 11. Known Issues / Observations

1. `@MockBean` deprecation warning under `Spring Boot 3.5.x` during tests (build still succeeds)
2. Ensure README version badge matches latest tag after tagging and releasing

---

# 12. Current Stability Assessment

Build: Passing
Tests: Passing
Coverage: Enforced
CI: Operational
Docker runtime: Verified
OpenAPI contract: Explicit, stable, and verified in Swagger UI
Architecture: Clean and extensible

Project maturity level:
Portfolio-grade backend service with stable API contracts and enforced quality gates.

---

END OF FILE
