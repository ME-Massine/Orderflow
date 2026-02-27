# OrderFlow Roadmap

OrderFlow is a systems-heavy backend project focused on recruiter clarity: clean architecture, strong backend fundamentals, observability, messaging, and production-style practices.

## Goals
- Build a microservices-oriented backend with clear boundaries and strong domain modeling.
- Demonstrate real backend engineering: persistence, messaging, reliability patterns, testing, CI, and documentation.
- Ship incrementally using vertical slices and stable milestones.

## High-Level Architecture (target)
- order-service: owns Order lifecycle and emits domain events
- inventory-service: validates and reserves stock, reacts to events
- payment-service: handles payment intent, reacts to events
- notification-service (optional): sends email or webhook notifications

Infrastructure (dev-friendly):
- PostgreSQL for persistence
- RabbitMQ for async messaging (Kafka later is optional)
- OpenAPI for API documentation
- Micrometer + Actuator for metrics/health
- Centralized logging conventions (structured logs)

---

## Milestones

### v0.1 Foundation (DONE)
- [x] Repository initialized (monorepo structure)
- [x] order-service bootstrapped (Spring Boot, Web, JPA, Validation, Actuator)
- [x] PostgreSQL configured and connected
- [x] Basic Order API
    - [x] POST /api/orders
    - [x] GET /api/orders/{id}
    - [x] GET /api/orders
- [x] DTOs and validation
- [x] Global exception handling
- [x] HTTP request file for manual testing (requests.http)

Exit criteria:
- Service runs locally and persists orders to Postgres.
- Basic endpoints verified using requests.http.

---

### v0.2 API Hardening + Clean Contracts
- [ ] Pagination for listing orders (page, size)
- [ ] Status update endpoint (PATCH /api/orders/{id}/status)
- [ ] Filtering (example: by customerId)
- [ ] Input and error response consistency
- [ ] Add basic constraints and indexes in DB (documented)
- [ ] Add logging conventions for request tracing (correlation id)

Exit criteria:
- API supports scalable listing and a real domain operation (status transition).

---

### v0.3 Observability + Documentation
- [ ] OpenAPI/Swagger documentation (springdoc)
- [ ] Actuator endpoints configured for health/info/metrics
- [ ] Basic custom metrics (example: orders.created counter)
- [ ] Add service info endpoint metadata (build info, git info if available)
- [ ] Improve error payload format (stable schema)

Exit criteria:
- A recruiter can open Swagger UI, test endpoints, and see health/metrics.

---

### v0.4 Messaging (RabbitMQ) + Domain Events
- [ ] RabbitMQ setup documented (local install OR cloud broker)
- [ ] Define event contracts (JSON schema / versioning rules)
- [ ] Publish OrderCreated event after successful order creation
- [ ] Add consumer example (can be in same service initially)
- [ ] Add retry strategy and dead-letter design (documented)
- [ ] Add idempotency strategy for consumers (documented and partially implemented)

Exit criteria:
- Order creation emits an event and at least one consumer reacts reliably.

---

### v0.5 Second Service (Inventory) + Integration Flow
- [ ] inventory-service created (Spring Boot)
- [ ] Inventory model + persistence
- [ ] Consume OrderCreated and reserve stock
- [ ] Emit InventoryReserved or InventoryFailed event
- [ ] Add integration tests for message flow (Testcontainers optional; if not possible, use broker stub and contract tests)

Exit criteria:
- Multi-service async flow demonstrated end-to-end with persistence.

---

### v0.6 Reliability Patterns
- [ ] Outbox pattern (or transactional message publishing strategy)
- [ ] Idempotency keys for write endpoints
- [ ] Rate limiting or basic abuse protection (optional)
- [ ] Timeouts and safe retries (documented and configured)
- [ ] Database migration tooling (Flyway or Liquibase)

Exit criteria:
- Clear reliability story: no lost events, safe retries, stable migrations.

---

### v1.0 MVP Release
- [ ] End-to-end business scenario documented:
    - Create order -> reserve inventory -> payment intent (optional) -> final status
- [ ] CI pipeline (GitHub Actions):
    - build, test, checkstyle/spotless
- [ ] Final documentation:
    - architecture diagram
    - service responsibilities
    - how to run locally
    - API usage examples
    - event contracts

Exit criteria:
- Repo can be cloned and evaluated quickly with clear docs and solid engineering signals.

---

## Engineering Rules (commit and development)
- Use Conventional Commits with service scope:
    - feat(order-service): ...
    - fix(order-service): ...
    - test(order-service): ...
    - docs: ...
    - chore: ...
- Prefer vertical slices: ship one use-case end-to-end before starting another.
- Keep commits small and reviewable.
- Every milestone should have a tag: v0.1, v0.2, etc.

---

## Notes
- If Docker is unavailable locally, document alternative setups:
    - local PostgreSQL install (Windows service)
    - local RabbitMQ install (Erlang + RabbitMQ) or cloud broker
- Kafka can be added later as an extension once RabbitMQ flow is stable.