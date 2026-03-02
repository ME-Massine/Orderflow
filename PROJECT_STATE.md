# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: Spring Boot 3, PostgreSQL, JPA, Actuator, OpenAPI  
Language: Java 17

Repository layout: mono-repo (services/order-service)

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

This project is structured to reflect real-world backend engineering standards and be portfolio-ready.

---

# 2. Current Milestone

Milestone: Dockerized Local Run + Portfolio-Ready Setup (v0.4.0)

Status: Stable, Versioned, Tested, Reproducible

Scope of milestone (now complete):
- CRUD-style order management endpoints
- API versioning introduced (/api/v1/...)
- OpenAPI documentation integrated
- Test profile configured (H2)
- CI pipeline executing Maven tests
- Controller contract tests (MockMvc via @WebMvcTest)
- Repository slice tests (@DataJpaTest)
- Exception handling corrected for 400 vs 500 responses
- Service layer unit tests (Mockito, no Spring context)
- Dockerfile (multi-stage build) for order-service
- docker-compose (order-service + PostgreSQL)
- Docker profile configuration (application-docker.yml)
- pom version aligned with release tag (0.4.0)

Release tags:
- v0.1.0 baseline
- v0.2.0 integration tests
- v0.3.0 service unit tests
- v0.4.0 dockerized local run

---

# 3. Architecture Decisions (ADR Style)

## ADR-001: Layered Architecture

Decision:
Use a strict layered architecture:

Controller → Service → Repository → JPA → Database

Reason:
- Clear separation of concerns
- Testability
- Industry-standard backend design
- Easy migration to microservices

Status: Implemented

---

## ADR-002: DTO Isolation

Decision:
Use DTOs for request and response models instead of exposing entities directly.

Reason:
- Avoid leaking persistence model
- Better API contract control
- Validation layer separation

Status: Implemented

---

## ADR-003: API Versioning Strategy

Decision:
Use URL-based versioning: `/api/v1/...`

Reason:
- Explicit and clear version control
- Backward compatibility support
- Industry standard approach

Status: Implemented

---

## ADR-004: Profile-Based Test Isolation

Decision:
Use `@ActiveProfiles("test")` and `application-test.yml` with H2.

Reason:
- Prevent CI from connecting to PostgreSQL
- Ensure deterministic builds
- Isolate infrastructure dependencies

Status: Implemented

---

## ADR-005: Maven Wrapper Usage

Decision:
Use Maven Wrapper (`mvnw`) instead of requiring global Maven installation.

Reason:
- Version consistency across environments
- CI portability
- Zero external dependency requirement

Status: Implemented (wrapper located in order-service module)

---

## ADR-006: OpenAPI Integration

Decision:
Integrate `springdoc-openapi` with Swagger UI.

Reason:
- Self-documenting API
- Developer experience improvement
- Industry-standard API contract visibility

Status: Implemented

---

## ADR-007: Structured Exception Handling

Decision:
Use @RestControllerAdvice for centralized exception mapping.

Enhancements:
- MethodArgumentNotValidException → 400
- MissingServletRequestParameterException → 400
- MethodArgumentTypeMismatchException → 400
- NotFoundException → 404
- Generic Exception → 500

Reason:
- Correct HTTP semantics
- Predictable API error contract
- Professional-grade behavior

Status: Implemented and verified by tests

---

## ADR-008: Dockerized Local Environment

Decision:
Provide a docker-compose based local runtime (order-service + PostgreSQL) and a Dockerfile build.

Reason:
- One-command reproducibility for reviewers and recruiters
- Eliminates local environment drift
- Aligns dev/prod runtime expectations

Status: Implemented (v0.4.0)

---

# 4. Completed Components

## Core Structure
- Spring Boot 3 project setup
- Layered architecture folders
- Clean package organization

## Domain Layer
- Order entity
- OrderStatus enum
- JPA mappings including @PrePersist defaults

## Persistence Layer
- OrderRepository (Spring Data JPA)

## Service Layer
- OrderService with business logic
- Pagination mapping: Page<OrderResponse>
- Status update uses transactional dirty checking

## Web Layer
- OrderController
- Versioned endpoints (/api/v1/orders)
- GET list (paginated)
- GET by ID
- POST create
- PATCH update status

## Validation
- Jakarta Validation annotations
- Request DTO validation

## Observability
- Spring Boot Actuator enabled
- Health endpoint configured
- Rabbit health disabled for tests

## Documentation
- OpenAPI docs available
- Swagger UI available when enabled

---

# 5. Testing Strategy (Implemented)

## Web Layer Contract Tests
- @WebMvcTest + MockMvc
- HTTP contract validation
- JSON structure checks
- Validation failure scenarios
- Missing request param behavior
- Invalid enum behavior

## Repository Slice Tests
- @DataJpaTest
- H2 in-memory database
- Persistence verification
- @PrePersist behavior verification

## Service Unit Tests
- Mockito based tests (no Spring context)
- create(), getById(), list(), updateStatus()
- NotFound scenarios validated
- DTO mapping validated
- Pageable construction validated
- Update behavior validated without explicit save (dirty checking)

Build status: Passing  
All tests: Green

---

# 6. Local Runtime

## Docker (recommended for reviewers)
From repo root:
- docker compose up --build

Expected endpoints:
- Health: http://localhost:8081/actuator/health
- Swagger: http://localhost:8081/swagger-ui/index.html (if enabled)

Notes:
- docker-compose provisions PostgreSQL
- order-service uses docker profile and docker network hostnames

---

# 7. CI

- GitHub Actions workflow (ci.yml)
- Maven build + test execution
- Test profile isolation in place

Status: Functional

---

# 8. Versioning

Current version: 0.4.0  
Current tag: v0.4.0

Release discipline:
- Bump pom version before tagging
- Tags point to the version bump commit
- Documentation-only updates do not require a new tag

---

# 9. Pending Tasks

### High Priority (portfolio ROI)
- Add JaCoCo coverage reporting
- Enforce minimum coverage threshold in CI
- Add README Quickstart (Docker, endpoints, Swagger)

### Medium Priority
- Standardize pagination response shape (PageImpl warning)
- Improve error contract consistency and include request path
- Structured logging (request id / correlation id)

### Longer Term (systems depth)
- RabbitMQ event publishing (OrderCreatedEvent)
- Domain events pattern
- Docker Compose extras (metrics stack: Prometheus)
- Add a second service (product-service) to demonstrate inter-service contracts

---

# 10. Known Issues

1. @MockBean deprecation warning in Spring Boot 3.5.x
    - Functional, but should be migrated when Spring finalizes the replacement annotation.

2. PageImpl serialization warning
    - JSON shape not guaranteed stable.
    - Candidate fix: DTO based pagination wrapper or Spring HATEOAS PagedModel.

3. RabbitMQ starter present but not used yet
    - Health disabled in tests.
    - Messaging milestone planned later.

---

# 11. Next Immediate Step

Milestone proposal: Test Coverage Metrics (JaCoCo) (v0.5.0)

Goal:
- Generate coverage reports locally and in CI
- Fail CI if coverage drops below threshold
- Add coverage badge to README

Reason:
- Recruiter-visible quality signal
- Protects against regressions now that tests exist

---

# 12. Current Stability Assessment

Build: Passing  
API: Working  
Swagger/OpenAPI: Working  
Validation: Correct HTTP semantics  
Exception Handling: Correctly mapped  
Tests: Web + Repository + Service implemented  
CI: Functional  
Local Runtime: Dockerized reproducible setup  
Database: PostgreSQL (prod/docker) + H2 (tests)  
Architecture: Clean, extensible, professionally structured

Project maturity level: Professional Backend Baseline (Portfolio Ready)

---

END OF FILE