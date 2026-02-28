# OrderFlow

OrderFlow is a production-oriented backend system designed to demonstrate clean architecture, domain-driven thinking, and scalable microservice patterns using Spring Boot and PostgreSQL.

OrderFlow models a simplified order lifecycle system where orders are created, validated, persisted, and prepared for asynchronous processing via domain events.

This project focuses on engineering quality, clarity of design, and real-world backend principles rather than surface complexity.

---

## Project Goals

OrderFlow is built to showcase:

- Clean layered architecture
- Clear separation of concerns
- Domain-driven design principles
- RESTful API best practices
- Validation and structured error handling
- Transaction management
- Observability and metrics
- Messaging-driven architecture (planned)
- Incremental, milestone-based development

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web (REST)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Micrometer + Actuator
- RabbitMQ (planned)
- OpenAPI (planned)
- GitHub Actions (planned)

---

## Architecture Overview

### Project Structure

```
orderflow/
└── services/
    └── order-service/
        ├── controller/
        ├── service/
        ├── repository/
        ├── entity/
        ├── dto/
        ├── exception/
        └── OrderServiceApplication.java
```

### Layered Architecture

```
Controller (HTTP layer)
        ↓
Service (business logic)
        ↓
Repository (data abstraction)
        ↓
JPA / Hibernate (ORM)
        ↓
PostgreSQL
```

### Layer Responsibilities

- Controller  
  Handles HTTP transport logic only.

- Service  
  Contains business rules and transaction boundaries.

- Repository  
  Abstracts data access operations.

- Entity  
  Represents the persistence model.

- DTO  
  Defines the API contract model.

This strict separation ensures maintainability, testability, and scalability.

---

## Current Scope

The current implementation includes:

- Order creation
- Order retrieval by ID
- Paginated order listing
- Order status transitions
- PostgreSQL persistence
- Global exception handling
- Basic observability via Actuator

RabbitMQ and multi-service orchestration are planned in upcoming milestones.

---

## Running Locally

### Requirements

- Java 17
- PostgreSQL
- Maven (or IntelliJ built-in Maven)

---

### Database Setup

Create the database and user:

```sql
CREATE USER "order" WITH PASSWORD 'order';
CREATE DATABASE orderdb OWNER "order";
GRANT ALL PRIVILEGES ON DATABASE orderdb TO "order";
```

---

### Application Configuration

`application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: order
    password: order
```

---

### Start the Service

Run `OrderServiceApplication` from your IDE.

The service runs on:

```
http://localhost:8081
```

Health check endpoint:

```
http://localhost:8081/actuator/health
```

---

## API Endpoints

### Create Order

```
POST /api/orders
```

Request:

```json
{
  "customerId": "c1",
  "productId": 101,
  "quantity": 2
}
```

Response:

```json
{
  "id": 1,
  "customerId": "c1",
  "productId": 101,
  "quantity": 2,
  "status": "PENDING",
  "createdAt": "2026-01-01T10:00:00"
}
```

---

### Get Order By ID

```
GET /api/orders/{id}
```

---

### List Orders (Paginated)

```
GET /api/orders?page=0&size=10
```

---

### Update Order Status

```
PATCH /api/orders/{id}/status?status=CONFIRMED
```

Available statuses:

- `PENDING`
- `CONFIRMED`
- `CANCELLED`

---

## Observability

OrderFlow includes production-ready monitoring foundations:

- Spring Boot Actuator
- Health endpoint
- Metrics endpoint (expandable)
- Structured JSON error responses

Future expansion may include:

- Prometheus integration
- Distributed tracing
- Centralized logging

---

## Key Design Decisions

- Entities are separated from DTOs to decouple persistence from API contracts.
- Business logic is confined to the service layer.
- Controllers contain no domain logic.
- Transactions are defined at the service layer.
- Pagination is enforced to avoid unbounded result sets.
- Conventional Commits are used to maintain a clean history.

## Roadmap

See `ROADMAP.md` for detailed milestone planning.

Upcoming milestones include:

- OpenAPI / Swagger documentation
- RabbitMQ integration (OrderCreated event)
- Inventory microservice
- Integration testing
- Reliability patterns (Outbox, idempotency)
- CI/CD pipeline with GitHub Actions

---

## Engineering Practices

- Conventional Commits
- Vertical slice development
- DTO separation from entities
- Transactional service methods
- Global exception handling
- Pagination for scalable APIs
- Clean commit history

---

## Why OrderFlow?

OrderFlow simulates real-world backend engineering:

- Clear architectural boundaries
- Messaging-oriented design evolution
- Domain modeling discipline
- Incremental system growth
- Production-oriented thinking

The goal is correctness, clarity, and engineering maturity.

---

## Status

Active development.  
Built as a portfolio-grade backend system.
