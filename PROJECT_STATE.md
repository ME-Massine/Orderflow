# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: Spring Boot 3, PostgreSQL, JPA, Actuator, OpenAPI  
Language: Java 17

Repository layout: `mono-repo (services/order-service)`

---

# 1. Project Vision

OrderFlow is a production-grade backend service designed to demonstrate:

- Clean layered architecture
- Domain-driven structure
- REST API best practices
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

Milestone: Coverage Enforcement + Code Quality Automation (v0.5.0)

Status: Stable, Versioned, Tested, Measurable

Scope of milestone (now complete):

- JaCoCo coverage instrumentation
- XML + HTML coverage report generation
- Enforced minimum coverage thresholds:
    - Line coverage ≥ 75%
    - Branch coverage ≥ 60%
- CI failure if coverage drops below threshold
- Codecov integration
- Auto-updating coverage badge in README
- CI artifact upload of JaCoCo report
- `pom` version aligned with tag (0.5.0)

Release tags:
- `v0.1.0` baseline
- `v0.2.0` integration tests
- `v0.3.0` service unit tests
- `v0.4.0` dockerized local run
- `v0.5.0` coverage enforcement + Codecov

---

# 3. Architecture Decisions (ADR Style)

## ADR-001: Layered Architecture
`Controller` → `Service` → `Repository` → `JPA` → `Database`  
Status: Implemented

## ADR-002: DTO Isolation
Entities separated from API contract models.  
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
Status: Implemented and Verified
Swagger UI available at /swagger-ui/index.html
OpenAPI JSON exposed at /v3/api-docs

## ADR-007: Structured Exception Handling
Centralized error mapping with correct HTTP semantics.  
Status: Implemented

## ADR-008: Dockerized Local Environment
Reproducible runtime via `docker-compose`.  
Status: Implemented (v0.4.0)

## ADR-009: Coverage Enforcement Strategy
Enforce coverage thresholds at build level using JaCoCo and fail CI if violated.

Reason:
- Prevent regression
- Maintain quality baseline
- Provide recruiter-visible quality signal

Status: Implemented (v0.5.0)

---

# 4. Completed Components

## Core Architecture
- Clean layered design
- Strict separation of concerns

## Domain Layer
- Order entity
- `OrderStatus` enum
- `@PrePersist` lifecycle defaults

## Service Layer
- Business logic encapsulated
- Transaction boundaries defined
- Dirty checking update strategy

## Persistence Layer
- Spring Data JPA repository

## Web Layer
- Versioned REST endpoints
- Pagination enforced
- Validation integrated

## Observability
- Actuator endpoints
- Metrics exposure

## Testing Layers

### Web Layer Contract Tests
- `@WebMvcTest` + `MockMvc`
- Validation and error mapping verification

### Repository Slice Tests
- `@DataJpaTest`
- H2 in-memory DB
- JPA lifecycle verification

### Service Unit Tests
- Mockito-based pure unit tests
- Business logic validation
- NotFound scenarios
- Pageable verification
- Dirty checking behavior validation

---

# 5. Code Quality Infrastructure

## JaCoCo Coverage

- Generates HTML report:
  `services/order-service/target/site/jacoco/index.html`
- Generates XML report for CI + Codecov
- Enforces coverage thresholds at build time
- Fails build if thresholds not met

## Codecov Integration

- Coverage uploaded automatically via GitHub Actions
- Coverage badge in README updates per commit
- Provides PR-level coverage visibility
- Enables long-term quality tracking

CI behavior:
- Tests must pass
- Coverage must meet threshold
- Coverage report uploaded as artifact

---

# 6. Local Runtime

Run via Docker:
```bash
docker compose up --build
```

Base URL:
- http://localhost:8081  (no root "/" mapping; API endpoints only)

API Documentation:
- http://localhost:8081/swagger-ui/index.html
- http://localhost:8081/v3/api-docs

Observability:
- http://localhost:8081/actuator/health
- http://localhost:8081/actuator/metrics

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

Current version: `0.5.0`  
Current tag: `v0.5.0`

Release rule:
1. Implement feature
2. Bump version
3. Commit
4. Tag
5. Push
6. Publish release

Documentation-only changes do not trigger new version tags.

---

# 9. Pending Tasks

## High Priority
- Add README architecture diagram
- Improve pagination response standardization
- Refine error contract (include request path)

## Medium Priority
- Structured request logging (correlation ID)
- Prometheus metrics export
- RabbitMQ event publishing (OrderCreatedEvent)

## Long-Term
- Introduce second service (product-service)
- Inter-service communication
- Reliability patterns (Outbox, idempotency)

---

# 10. Known Issues

1. `@MockBean` deprecation warning (Spring Boot 3.5.x)
2. `PageImpl` JSON serialization stability warning
3. RabbitMQ dependency present but not yet used

---

# 11. Current Stability Assessment

Build: Passing  
Tests: Web + Repository + Service implemented  
Coverage: Enforced (≥75% line, ≥60% branch)  
CI: Fully automated  
Coverage Badge: Live via Codecov  
Docker Runtime: Functional  
Architecture: Clean, extensible, professionally structured

Project maturity level:  
Professional Backend System with Enforced Quality Baseline

---

END OF FILE
