# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: Spring Boot 3, PostgreSQL, JPA, Actuator, OpenAPI  
Language: Java 17

Repository layout: mono-repo (`services/order-service`)

---

# 1. Project Vision

OrderFlow is a production-oriented backend service designed to demonstrate:

- Clean layered architecture
- REST API best practices and contract stability
- Validation and structured error handling
- Transaction management
- Observability (Actuator + Metrics)
- Versioned APIs
- CI pipeline integration
- Test isolation via profile-based configuration
- Reproducible local environment via Docker
- Measurable and enforced code quality

This project reflects real-world backend engineering maturity and portfolio readiness.

---

# 2. Current Milestone

Milestone: API Contract Stabilization (v0.6.0)

Status: Stable, Versioned, Tested, Contract-hardened

Scope of milestone (complete):

- Standardized pagination response via `PageResponse<T>` envelope
    - Prevents leaking Spring internal paging types to API consumers
    - Provides stable pagination metadata: `content`, `page`, `size`, `totalElements`, `totalPages`
- Refined error contract for stable client consumption
    - Introduced `ApiError` DTO including request `path`
    - Introduced `ValidationError` DTO including `fieldErrors` and request `path`
    - Centralized error responses via `GlobalExceptionHandler`
- Service contract improvement
    - Added `listOrders(Pageable)` to align controller paging with Spring pageable binding
- Test updates
    - Updated `@WebMvcTest` assertions to validate the new pagination envelope and typed error responses
    - All tests remain green with JaCoCo reporting

Release tags:

- `v0.1.0` Order Service MVP
- `v0.2.0` Integration Testing Layer
- `v0.3.0` Service Layer Unit Tests
- `v0.4.0` Dockerized local run
- `v0.5.0` Coverage enforcement + Codecov
- `v0.6.0` API contract stabilization (pagination + typed errors)

---

# 3. Architecture Decisions (ADR Style)

## ADR-001: Layered Architecture
`Controller` -> `Service` -> `Repository` -> `JPA` -> `Database`  
Status: Implemented

## ADR-002: DTO Isolation
Entities are separated from API contract models.  
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

## ADR-006: OpenAPI Integration
Status: Implemented and verified  
Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)  
OpenAPI JSON: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

## ADR-007: Structured Exception Handling
Centralized error mapping with correct HTTP semantics.  
Status: Implemented and stabilized (`v0.6.0` typed contracts)

## ADR-008: Dockerized Local Environment
Reproducible runtime via `Docker Compose`.  
Status: Implemented (`v0.4.0`)

## ADR-009: Coverage Enforcement Strategy
Enforce coverage thresholds at build level using `JaCoCo` and fail CI if violated.  
Status: Implemented (`v0.5.0`)

## ADR-010: API Contract Stabilization
Expose stable pagination and error response shapes independent from Spring internals.  
Status: Implemented (`v0.6.0`)

---

# 4. Completed Components

## Core Architecture
- Clean layered design
- Separation of concerns enforced by package structure

## Domain Layer
- `Order` entity
- `OrderStatus` enum
- `@PrePersist` lifecycle defaults

## Service Layer
- Business logic encapsulated
- Transaction boundaries defined (`@Transactional`)
- Dirty checking update strategy
- Pageable-based listing supported (`listOrders(Pageable)`)

## Persistence Layer
- Spring Data JPA repository (`OrderRepository`)

## Web Layer
- Versioned REST endpoints (`/api/v1/orders`)
- Validation integrated (`@Valid`)
- Pagination supported and stabilized via `PageResponse<T>`
- Typed error contracts returned from centralized handler

## Observability
- Actuator endpoints enabled
- Health endpoint verified

## Testing Layers

### Web Layer Contract Tests
- `@WebMvcTest` + `MockMvc`
- Validates controller contracts, pagination envelope, and error mapping

### Repository Slice Tests
- `@DataJpaTest`
- H2 in-memory database
- JPA mapping and lifecycle verification

### Service Unit Tests
- Mockito-based pure unit tests
- Business logic validation
- NotFound scenarios
- Dirty checking behavior validation

---

# 5. Code Quality Infrastructure

## JaCoCo Coverage

- Generates HTML report:
  `services/order-service/target/site/jacoco/index.html`
- Generates XML report for CI + Codecov
- Enforces coverage thresholds at build time
- Fails build if thresholds not met

Coverage thresholds:
- Line coverage `>= 75%`
- Branch coverage `>= 60%`

## Codecov Integration

- Coverage uploaded automatically via GitHub Actions
- Coverage badge updates per commit
- PR-level coverage visibility

CI behavior:
- Tests must pass
- Coverage must meet thresholds
- Coverage report uploaded as artifact

---

# 6. Local Runtime

Run via Docker:
```bash
docker compose up --build
```

Base URL:

[http://localhost:8081](http://localhost:8081)
(no root "/" mapping; API endpoints only)

API documentation:

[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

[http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

Observability:

[http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

[http://localhost:8081/actuator/metrics](http://localhost:8081/actuator/metrics)

Docker provisions PostgreSQL automatically.

---

# 7. CI Pipeline

GitHub Actions workflow:

- Build
- Test
- JaCoCo coverage generation
- Coverage threshold enforcement
- Codecov upload
- Coverage artifact upload

Status: Fully operational

---

# 8. Versioning Discipline

Current version: `0.6.0`
Current tag: `v0.6.0`

Release rule:
1. Implement feature
2. Bump version
3. Commit
4. Tag
5. Push
6. Publish GitHub Release

Documentation-only changes do not require new version tags (unless they affect the release narrative).

---

# 9. Pending Tasks

## High Priority
- Add README architecture diagram
- Add explicit OpenAPI annotations for standardized response models (optional polish)
- Add a compact API contract section in README showing `PageResponse<T>` and `ApiError` schemas (portfolio polish)

## Medium Priority
- Structured request logging with correlation ID
- Prometheus metrics export
- RabbitMQ event publishing (`OrderCreatedEvent`) and clean ADR for messaging

## Long-Term
- Introduce second service (product-service)
- Inter-service communication patterns
- Reliability patterns (Outbox, idempotency)

---

# 10. Known Issues

1. `@MockBean` deprecation warning (Spring Boot 3.5.x) in tests
2. `RabbitMQ` dependency present but not yet used (planned milestone)
3. Ensure README version badge is not stale after releases

---

# 11. Current Stability Assessment

Build: Passing
Tests: Web + Repository + Service implemented and passing
Coverage: Enforced (`>=75%` line, `>=60%` branch)
CI: Fully automated
Coverage Badge: Live via Codecov
Docker Runtime: Functional
API Contract: Stabilized (pagination envelope + typed error contracts)
Architecture: Clean, extensible, professionally structured

Project maturity level:
Professional backend service with enforced quality baseline and stable external API contracts

---

END OF FILE
