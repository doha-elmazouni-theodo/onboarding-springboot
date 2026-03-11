# Architecture Overview

This document defines architecture contracts for the blueprint and where they are enforced.

## 1. Architecture Style

The template follows hexagonal architecture:
- domain code defines business behavior and contracts
- adapters implement I/O details
- dependencies point toward domain contracts

This is a rule-driven architecture, not a suggestion-only architecture.

![Hexagonal architecture schema](resources/hexagonal_architecture.png)

## 2. Layer Model

| Layer | Package | Responsibility | Owns I/O? |
|---|---|---|---|
| Feature API | `features/*/api` | HTTP endpoints, scheduled tasks, feature-level API concerns | yes |
| Feature Domain | `features/*/domain` | use cases, entities, value objects, domain services, feature ports | no |
| Common API | `common/api` | Spring wiring, security config, exception handling, serialization config | yes |
| Common Domain | `common/domain` | shared domain abstractions (ports, events, base exceptions, value objects) | no |
| Common Infra | `common/infra` | adapter implementations, JPA entities/repositories, infra config | yes |
| Common Utils | `common/utils` | generic reusable utilities | no |

## 3. Dependency Direction Rules

Rules:
- feature domain code depends on domain/common utils plus a narrow allowlist of Java, Jakarta, Checker Framework, Eclipse Collections, and Lombok types.
- subdomains do not depend on each other.
- adapters depend on ports, not the reverse.
- domain code must not perform I/O directly.

Enforcement:
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/HexagonalArchitectureRulesUnitTests.java`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/DesignRulesUnitTests.java`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/CodingRulesUnitTests.java`

## 4. Domain vs Adapter Responsibilities

Domain code should contain:
- business decisions
- business invariants
- orchestration between domain concepts and ports

Adapter code should contain:
- protocol/framework translation (HTTP, JPA, JWT, etc.)
- serialization/deserialization
- persistence mapping

A quick check:
- if logic could change when business rules change, it belongs in domain
- if logic could change when framework/protocol changes, it belongs in adapters

## 5. Use Case Contract

Use case classes live under `..domain.usecases..` and end with `UseCase`.

Enforced shape:
- exactly one method named `handle`
- `handle` takes at most one argument

Enforcement source:
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/domain/UseCaseRulesUnitTests.java`

Team convention (stricter than current static check):
- keep `handle` as the only public method in a use case class

This avoids multi-purpose use case classes and keeps one operation per class.

## 6. REPR Endpoint Contract

REPR = Request, Endpoint, Response.

Endpoint conventions:
- one endpoint class per route handler
- request and response transport models are endpoint-local
- request and response models are not shared across endpoints

Enforced naming and placement:
- endpoint classes: suffix `Endpoint`, package `..api.endpoints.(*)`
- endpoint request/response classes: suffix `EndpointRequest` / `EndpointResponse`
- no class-level `@RequestMapping` on controllers

Enforcement sources:
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/api/NamingConventionRulesUnitTests.java`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/api/ControllerConventionRulesUnitTests.java`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/api/RequestResponseConventionRulesUnitTests.java`

### 6.1 Authorization Annotation Rule

`ControllerConventionRulesUnitTests` enforces:
- mapped controller methods must declare `@PreAuthorize` unless either:
  - every declared mapping path contains `/public/`, or
  - the method is declared in an `ErrorController` implementation.

So the rule is path-based, not class visibility-based.

## 7. Collections and Immutability Contract

Default policy:
- use Eclipse Collections interfaces in fields and non-private signatures
- default to immutable collections unless mutability is required
- use local factories `Immutable` and `Mutable`

Allowed exceptions:
- endpoint request/response fields can use Java collection types
- JPA entities can use Java collection types

Enforcement sources:
- PMD rules in `api/pmd/custom/design-rules.xml`
- request/response type checks in `RequestResponseConventionRulesUnitTests`

For nullability implications of collection element types, see:
- [Nullability and Checker Framework](06-nullability-and-checker-framework.md)

## 8. Bean Wiring Strategy

Domain wiring:
- `common/api/beans/DomainBeanConfiguration.java` auto-registers `..domain.usecases..*UseCase` classes
- use cases stay framework-agnostic and unannotated by Spring stereotypes

Infra wiring:
- technical beans are configured in infra/common config classes
- adapters and framework boundaries remain outside domain packages

### 8.1 Fast Self-Review Before PR

Why this exists:
- catches architecture drift before CI/review, where fixes cost more time
- keeps feature work aligned with enforced rules and avoids late rework

Use this quick pass before pushing:
- domain changes do not import framework/web/persistence concerns (`org.springframework.*`, `jakarta.persistence.*`)
- each mapped endpoint method has explicit `@PreAuthorize` unless route path contains `/public/`
- each use case class keeps one `handle(...)` operation as public entrypoint
- endpoint request/response classes stay local to one endpoint package
- `./mvnw spotless:apply verify` succeeds locally

If architecture checks fail, inspect failing rule classes under:
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/api/`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/domain/`

## 9. Architecture Checklist

### ✅ Do

- keep business behavior in domain use cases and domain services
- inject ports into domain code
- keep endpoint transport models local to each endpoint
- keep non-domain concerns in adapters/configuration

### ❌ Do Not

- call JPA repositories directly from domain use cases
- share request/response models across endpoints
- put business decision branches in controllers/adapters
- bypass architecture rules by weakening tests or rule sets

## Navigation

- ⬅️ Previous: [1 - Onboarding Guide](01-onboarding-guide.md)
- ➡️ Next: [3 - Project Structure and Conventions](03-project-structure-and-conventions.md)
