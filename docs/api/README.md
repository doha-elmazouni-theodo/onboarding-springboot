# Developer Guide

This blueprint is intentionally opinionated.

It favors explicit rules, automated enforcement, and repeatable patterns over team-by-team interpretation. The goal is simple: any engineer should be able to modify a project safely without guessing local conventions.

## Who Should Read This

- engineers building or maintaining projects generated from this blueprint
- maintainers extending platform rules/tooling
- reviewers validating rule compliance and architecture consistency

## Core Principles

- ✅ business logic in domain code, not adapters
- ✅ tests are architecture, not final-stage checks
- ✅ rules enforced by tooling (ArchUnit, PMD, Maven plugins)
- ❌ no "one-off" bypass of a rule without changing the rule and documenting rationale

Non-negotiable examples:
- one use case class exposes one `handle(...)` operation ([Architecture Overview](02-architecture-overview.md#5-use-case-contract))
- mapped controller methods must carry explicit `@PreAuthorize` unless on `/public/` paths ([Architecture Overview](02-architecture-overview.md#61-authorization-annotation-rule))
- tests must not use `@WithMockUser`; use real auth helpers instead ([Testing Platform](04-testing-platform.md#12-test-checklist))

## Biggest Mistakes To Avoid

1. business logic in adapters/controllers instead of domain use cases/services
2. weak test helper design, causing unreadable and expensive tests

Bootstrap essentials (prerequisites, local start, must-know commands): [Root README](../../README.md)

## Path Legend

Unless explicitly marked otherwise:
- `features/*` and `common/*` paths mean `api/src/main/java/com/theodo/springblueprint/...`
- `testhelpers/*` and `transverse/*` paths mean `api/src/test/java/com/theodo/springblueprint/...`

## Reference Map

### Start Here

1. [Onboarding Guide](01-onboarding-guide.md)
2. [Architecture Overview](02-architecture-overview.md)
3. [Project Structure and Conventions](03-project-structure-and-conventions.md)

### Engineering and Platform Reference

4. [Testing Platform](04-testing-platform.md)
5. [Build Toolchain and Quality Gates](05-build-toolchain-and-quality-gates.md)
6. [Nullability and Checker Framework](06-nullability-and-checker-framework.md)
7. [Data Persistence and Migrations](07-data-persistence-and-migrations.md)
8. [Security, Observability, and Error Handling](08-security-observability-and-error-handling.md)
9. [CI/CD and Governance](09-ci-cd-and-governance.md)

### Full Capability Index

10. [Feature Matrix](10-feature-matrix.md)

## Rare But Important Mechanisms

Read these even if you do not edit them weekly.

| Mechanism | What it does | Where to learn it |
|---|---|---|
| Web MVC per-test bean lifecycle (`ClearableProxiedThreadScope`) | prevents state leakage between integration test methods | [Testing Platform](04-testing-platform.md) |
| Query-count guardrails (`@AssertQueryCount`) | blocks N+1/query drift regressions | [Testing Platform](04-testing-platform.md#8-query-count-guardrails) |
| Management split-context security import | keeps actuator security consistent on separate management context | [Security, Observability, and Error Handling](08-security-observability-and-error-handling.md#61-management-split-context-handling) |
| Soft-delete flush hardening listener | avoids partial unique-index conflicts after soft delete + recreate | [Data Persistence and Migrations](07-data-persistence-and-migrations.md#6-soft-delete-hardening) |
| Blueprint-only 100% threshold profile | enforces stricter quality gates in template repository | [Build Toolchain and Quality Gates](05-build-toolchain-and-quality-gates.md#2-coverage-and-mutation-thresholds) |
| Checker Framework stubs (`*.astub`) | stabilizes nullness analysis for external APIs | [Nullability and Checker Framework](06-nullability-and-checker-framework.md) |
| DevTools local config injection | injects `application-localdev.yml` only in local dev runs | [Onboarding Guide](01-onboarding-guide.md) |

## Navigation

- ➡️ Next: [1 - Onboarding Guide](01-onboarding-guide.md)
