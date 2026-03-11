# Project Structure and Conventions

## 1. Repository Layout

Application code root (Java packages under `com.theodo.springblueprint`):
- `features/`
  - one folder per subdomain
  - each subdomain has `api/` and `domain/`
- `common/`
  - shared modules used across subdomains

Current example subdomains in this blueprint:
- `features/users`
- `features/authentication`

High-level structure:

```text
features/
  <subdomain>/
    api/
    domain/
common/
  api/
  domain/
  infra/
  utils/
```

## 2. Feature Domain Structure

Typical domain folder content:

```text
domain/
  entities/
  events/
  exceptions/
  ports/
  properties/
  services/
  usecases/
    <usecase>/
      <UseCase>.java
      <Command|Query>.java
  valueobjects/
```

### 2.1 Use Cases

Rules:
- one use case class = one business operation
- one `handle(...)` method (enforced by ArchUnit)
- `handle` has at most one input argument
- use cases depend on ports and domain services, not on other use cases

Why:
- keeps behavior boundaries explicit
- avoids hidden orchestration coupling

### 2.2 Command and Query Types

Rules:
- command/query type carries only input required by one use case
- do not share command/query types across use cases

Why:
- prevents accidental cross-use-case coupling

### 2.3 Domain Services

Domain services contain domain logic shared by multiple use cases in the same subdomain.

Use a domain service when shared behavior is business behavior, not technical helper behavior.

### 2.4 Entities and Value Objects

- entities represent domain objects with identity
- value objects represent immutable domain values and invariants

Use value objects to avoid stringly-typed business code and to centralize validation.

### 2.5 Domain Exceptions

Prefer explicit exception types that embed structured context, not generic message-only exceptions.

### 2.6 Domain Events

Domain events communicate completed domain actions and decouple side effects.

Event conventions:
- event class names describe what happened (for example `UserLoggedInEvent`)
- events carry only relevant event data
- events should provide meaningful `toString()` for logs
- include source type when relevant for logging attribution

### 2.7 New Use Case Skeleton

When adding a new business operation, create at minimum:
- `features/<subdomain>/domain/usecases/<operation>/<Operation>UseCase.java`
- `features/<subdomain>/domain/usecases/<operation>/<Operation>Command.java` or `<Operation>Query.java`
- domain unit tests for the use case behavior

Checklist:
- one public `handle(...)` method only
- dependencies injected through ports/domain services
- no direct dependency on endpoint DTOs or JPA repositories

## 3. Feature API Structure

Typical API folder content:

```text
api/
  endpoints/
    <endpoint>/
      <Endpoint>.java
      <EndpointRequest>.java
      <EndpointResponse>.java
  schedules/
    <schedule>/
      <ScheduledTask>.java
  services/
```

### 3.1 Endpoints (REPR)

REPR = Request, Endpoint, Response.

Rules:
- one endpoint class handles one route
- endpoint code delegates business logic to use cases
- endpoint request/response models are endpoint-local and not reused

`EndpointRequest` rules:
- request fields should be constrained with validation annotations
- business invariants must still be validated in domain code
- avoid introducing coupling by reusing external custom types

`EndpointResponse` rules:
- response models are explicit API contracts
- keep them local to endpoint boundary

### 3.2 Scheduled Tasks

Rules:
- schedule classes should group methods with the same dependency set
- scheduled methods belong under `..api.schedules..`

Why:
- avoids bloated scheduler classes with unrelated dependencies

### 3.3 API Services

`features/<subdomain>/api/services` is for API-side shared logic (for example cookie response composition), not domain business logic.

### 3.4 New Endpoint Skeleton

When adding a new route, create at minimum:
- `features/<subdomain>/api/endpoints/<operation>/<Operation>Endpoint.java`
- `features/<subdomain>/api/endpoints/<operation>/<Operation>EndpointRequest.java` when request body/params exist
- `features/<subdomain>/api/endpoints/<operation>/<Operation>EndpointResponse.java` when explicit output mapping is needed
- endpoint integration tests focused on mapping/security contracts

Checklist:
- one endpoint class handles one route
- endpoint delegates behavior to use case
- endpoint transport models are not reused outside endpoint package

## 4. Common Modules

### 4.1 `common/api`

Contains cross-cutting API/framework concerns:
- dependency injection setup
- security configuration
- exception handling
- serialization and web config

### 4.2 `common/domain`

Contains shared domain abstractions:
- base event and exception types
- shared value objects and ports

It must not contain feature business entities.

### 4.3 `common/infra`

Contains shared infrastructure implementations:
- adapters implementing domain ports
- database entities and repositories
- infrastructure properties/configuration

### 4.4 `common/utils`

Contains generic utilities that are domain-agnostic.

Do not place domain helper logic in `common/utils`.

## 5. Dependency Rules Summary

- `features/*` may depend on standard Java + `common/domain` + `common/utils`
- `common/domain` may depend on standard Java + `common/utils`
- `common/utils` should stay dependency-light and generic
- domain code must not perform direct I/O
- I/O must be represented by ports and implemented by adapters

For enforcement details, see:
- [Architecture Overview](02-architecture-overview.md#3-dependency-direction-rules)

## 6. Checklist

### ✅ Do

- keep business behavior in domain use cases/services
- keep endpoint transport models local to endpoint boundaries
- use ports for every out-of-process interaction

### ❌ Do Not

- share transport/request models across endpoints “for DRY”
- expose JPA entities directly at API boundary
- place business branches in API services or adapters

## Navigation

- ⬅️ Previous: [2 - Architecture Overview](02-architecture-overview.md)
- ➡️ Next: [4 - Testing Platform](04-testing-platform.md)
