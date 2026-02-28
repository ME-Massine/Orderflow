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
- contextLoads test
- H2 in-memory test database
- Rabbit auto-config disabled in tests

## CI
- GitHub Actions workflow (ci.yml)
- Maven build + test execution
- Failing test issues resolved

## Versioning
- Initial project versioning fixed in pom
- Changelog started

---

# 5. Pending Tasks

High Priority:
- Add integration tests (MockMvc)
- Add repository test slice
- Improve exception handling (standard error response format)

Medium Priority:
- Add global exception handler contract model
- Add request/response logging strategy
- Add pagination metadata standardization

Infrastructure:
- Dockerize order-service
- Add docker-compose with PostgreSQL
- Add health endpoint readiness/liveness separation

Observability:
- Add metrics tagging
- Enable Prometheus export

Architecture:
- Prepare for RabbitMQ event publishing (future milestone)
- Introduce domain events pattern

Documentation:
- Add architecture diagram (Mermaid)
- Expand README with run instructions

---

# 6. Known Issues

1. Maven wrapper exists only inside `services/order-service`
   → Root wrapper recommended for mono-repo cleanliness.

2. No integration test coverage yet.
   → Only contextLoads exists.

3. RabbitMQ starter present but not used yet.
   → Will be used in future milestone.

4. No standardized API error response format yet.

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

Add Integration Testing Layer

Specifically:
- Add `@WebMvcTest` for OrderController
- Add MockMvc tests for:
    - POST /api/v1/orders
    - GET /api/v1/orders
    - PATCH /api/v1/orders/{id}/status
- Add repository test with `@DataJpaTest`

Why:
This transitions the project from "functional demo" to "production-grade backend discipline".

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
CI: Functional  
Database: PostgreSQL (prod) + H2 (test)  
Architecture: Clean and extensible

Project maturity level: Early Professional

---

END OF FILE