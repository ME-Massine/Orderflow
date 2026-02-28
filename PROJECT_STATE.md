# PROJECT_STATE.md

Project: OrderFlow  
Type: Systems-focused backend engineering project  
Primary Stack: Spring Boot 3, PostgreSQL, JPA, Actuator, OpenAPI  
Language: Java 17

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

This project is structured to reflect real-world backend engineering standards.

---

# 2. Current Milestone

Milestone: Core Order Service Foundation Complete

Status: Stable and Versioned

Scope of milestone:
- CRUD-style order management endpoints
- API versioning introduced (/api/v1/...)
- OpenAPI documentation integrated
- Test profile configured
- CI pipeline executing Maven tests
- Project versioning initialized
- Controller integration tests (MockMvc)
- Repository slice tests (DataJpaTest)
- Exception handling corrected for 400 vs 500 responses

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

## ADR-007 – Structured Exception Handling

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

# 4. Completed Components

## Core Structure
- Spring Boot 3 project setup
- Layered architecture folders
- Clean package organization

## Domain Layer
- Order entity
- Status enum
- JPA mappings

## Persistence Layer
- OrderRepository (Spring Data JPA)

## Service Layer
- OrderService with business logic
- Status update handling
- Pagination support

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
- Spring Boot Actuator integrated

## Documentation
- Swagger UI functional
- /v3/api-docs endpoint working

## Testing

### Web Layer Integration Tests
- @WebMvcTest
- MockMvc HTTP contract validation
- JSON structure verification
- Validation failure scenarios
- Enum mismatch handling
- Missing parameter handling

### Repository Tests
- @DataJpaTest
- H2 in-memory database
- Entity persistence verification
- @PrePersist behavior verification

### Test Coverage Scope
- Controller contract
- Validation layer
- Exception handling
- Repository persistence

Build Status: Passing
All tests: Green

## CI
- GitHub Actions workflow (ci.yml)
- Maven build + test execution
- Failing test issues resolved

## Versioning
- Initial project versioning fixed in pom
- Changelog started

---

# 5. Pending Tasks

### High Priority:
- Service layer unit tests
- Coverage metrics (Jacoco)
- Pagination response standardization

### Medium Priority:
- Structured request logging
- Correlation IDs
- Improve error contract consistency

### Infrastructure:
- Dockerize order-service
- Add docker-compose with PostgreSQL
- Add health endpoint readiness/liveness separation

### Observability:
- Add metrics tagging
- Enable Prometheus export

### Architecture:
- Prepare for RabbitMQ event publishing (future milestone)
- Introduce domain events pattern

### Documentation:
- Add architecture diagram (Mermaid)
- Expand README with run instructions

---

# 6. Known Issues

1. @MockBean deprecation warning in Spring Boot 3.5.x
   → Functional but flagged for future migration.

2. PageImpl serialization warning
   → JSON shape not guaranteed stable.
   → Can be improved with DTO-based pagination model.

3. RabbitMQ starter present but not used yet.
   → Will be used in future milestone.

4. No service-layer unit tests yet.

5. No Docker configuration yet.

---

# 7. Technical Debt

- Exception handling not fully standardized
- No test coverage metrics
- No structured logging correlation IDs
- No request tracing integration

---

# 8. Next Immediate Step

Recommended next step:

Add Service Layer Unit Tests

Specifically:
- Test create logic behavior
- Test status transition rules
- Test NotFound scenarios
- Test pagination mapping

Why:
Controller and repository layers are validated.
Service logic is the last untested core business layer.

This elevates the project to a fully disciplined backend baseline.

---

# 9. Long-Term Roadmap

Phase 2:
- RabbitMQ event publishing
- OrderCreatedEvent
- Message contract design

Phase 3:
- Extract into microservice-ready structure
- Add product-service
- Introduce inter-service communication

Phase 4:
- Add caching (Redis)
- Add idempotency handling
- Add API rate limiting

---

# 10. Current Stability Assessment

Build: Passing  
Swagger: Working  
API Versioning: Working  
Validation: Correct HTTP semantics
Exception Handling: Correctly mapped
Integration Tests: Implemented
Repository Tests: Implemented
CI: Functional
Database: PostgreSQL (prod) + H2 (test)
Architecture: Clean, extensible, professionally structured

Project maturity level: Professional Backend Baseline

---

END OF FILE