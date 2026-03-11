# Testing Platform

Testing is treated as an architecture concern in this blueprint.

The goal is to keep feedback fast, behavior coverage high, and regressions visible at the boundary where they happen.

## 1. Strategy

Core strategy:
- exhaustive domain unit tests
- mutation testing on domain-heavy scope
- fakes preferred over repeated mocks
- contract tests for port behavior
- focused integration tests on boundaries
- isolated tests for cross-cutting concerns (security, exception handling, logging)
- full application/flow tests for multi-step scenarios

Why this mix works:
- domain behavior gets deep coverage at low runtime cost
- integration boundaries are verified where translation/mapping risks are highest
- architectural constraints stay testable and explicit

### 1.1 Which Test Should I Add?

Use this quick map when implementing changes:

| Change type | Minimum test layer | Notes |
|---|---|---|
| new domain branch/invariant | unit tests on use case/value object | primary behavior specification |
| new adapter/repository behavior | integration tests + `@AssertQueryCount` when query-sensitive | validate translation + SQL footprint |
| new adapter implementing an existing port | contract tests (`*PortContractTests`) + adapter integration tests | enforces adapter substitutability; if contract fails, adapter cannot be swapped safely |
| port behavior contract change | update abstract contract tests, then run all adapter implementations against it | one contract change can break multiple adapters; treat as cross-cutting change |
| endpoint mapping/security change | web integration tests (`BaseWebMvcIntegrationTests`) | assert HTTP contract and auth requirements |
| exception-to-ProblemDetail mapping change | exception handler integration tests | assert status and payload shape |
| scheduled-task logic/wiring change | scheduled-task integration tests (`BaseScheduledTaskIntegrationTests`) | validate property-driven schedule wiring |
| multi-step end-to-end behavior | application/flow tests | keep count small, scenario-driven |

Contract-test implication:
- contract tests define domain expectations shared by all implementations
- do not add implementation-specific assertions in abstract contract classes
- keep implementation-specific checks in adapter integration tests

## 2. Test Categories and Naming

Naming conventions:
- `*UnitTests`
- `*IntegrationTests`
- `*ApplicationTests`
- `*ContractTests` (abstract contract definitions)

Execution order (`maven-surefire-plugin`):
1. architecture tests (`transverse/architecture/**`)
2. unit tests (`*UnitTests`)
3. integration/application remainder

Why:
- fail fast on structural violations
- keep fast-feedback loops before slower suites

Rule enforcement sources:
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/TestsNamingConventionRulesUnitTests.java`

## 3. Unit Test Scope

Unit tests should primarily target:
- use cases
- value objects with validation logic

Prefer testing behavior through public domain contracts, not internal implementation details.

Unit tests are the main executable specification of business behavior.

## 4. Fakes Over Mocks

Use fakes when a dependency is reused in many tests.

Why:
- better readability and less setup noise
- behavior-oriented tests instead of interaction-scripted tests
- fewer fragile tests tied to call order/internal steps

Important rule:
- do not add methods to production ports only to simplify tests
- if extra inspection helpers are needed, put them in fake adapters

## 5. Contract Tests for Ports

Pattern:
1. abstract `*PortContractTests` defines required behavior from domain perspective
2. each adapter test class extends that contract
3. same behavior spec runs against fake and real adapters

Benefit:
- one specification, many implementations
- adapters stay interchangeable from domain point of view

## 6. What "Integration Test" Means Here

Integration tests validate boundaries, not just “Spring context loaded”.

Common integration targets:
- API ↔ Domain: request mapping, response mapping, security annotations
- Domain ↔ Infra: adapter behavior, persistence semantics, exception translation
- Cross-cutting runtime behavior: auth entry points, exception payloads, SQL logging, OpenAPI conversion

## 7. Test Infrastructure Building Blocks

### 7.1 Custom Annotations

| Annotation | Purpose | Why it exists |
|---|---|---|
| `@UnitTest` | standard marker for unit test classes | enables naming/category rules and local conventions |
| `@IntegrationTest` | marks tests that are integration by behavior but not by standard Spring slice annotation | prevents category ambiguity |
| `@SetupDatabase` | enables Testcontainers DB setup + required DB-test config imports | keeps DB integration tests realistic and deterministic |
| `@ParentNestedDataJpaTest` | JPA slice support for nested test classes inherited from abstract parents | avoids brittle nested test bootstrap behavior |
| `@AssertQueryCount` | declarative query-count expectations | catches N+1 and query drift |
| `@IncludeTestOnlyDbTypes` | includes test-only entities/repositories in test context | allows dedicated infra behavior tests without leaking those types into normal runs |

### 7.2 Base Classes

| Base class | What it does | Why it exists |
|---|---|---|
| `AbstractApplicationTests` | shared base for full application tests | removes repeated bootstrapping code |
| `BaseApplicationTestsWithDb` | full context with Testcontainers DB, restarted per test method | strong isolation for stateful full-context scenarios |
| `BaseApplicationTestsWithoutDb` | full context without datasource auto-config, auto-registers repository/entity-manager mocks | avoids startup failures caused by missing repository/entity-manager beans; these mocks are inert placeholders and should not be used for behavior assertions |
| `BaseWebMvcIntegrationTests` | standard web integration harness with auth/test helpers | keeps endpoint integration tests consistent |
| `BaseScheduledTaskIntegrationTests` | scheduler harness executing scheduled tasks immediately | deterministic scheduled-task tests |
| `BaseExceptionHandlerIntegrationTests` | shared exception-handler payload contract tests | consistent error response verification across handlers |

### 7.3 Web MVC Isolation Internals

`BaseWebMvcIntegrationTests` uses a custom per-call scope (`ClearableProxiedThreadScope`) for application beans.

Mechanism:
- beans are scoped to one test-method call lifecycle
- after each test method, scope is cleared
- existing proxies are marked stale
- stale proxy usage throws an explicit error

Why this exists:
- many fake adapters are stateful by design (they store in-memory data used by assertions)
- default Spring test bean scope is singleton, which would reuse the same fake instance across test methods
- `ClearableProxiedThreadScope` forces a fresh fake instance per test method and prevents state leakage between tests
- stale-proxy detection fails fast if a test tries to use a bean from a previous test-method lifecycle

Relevant code:
- `api/src/test/java/com/theodo/springblueprint/testhelpers/baseclasses/BaseWebMvcIntegrationTests.java`
- `api/src/test/java/com/theodo/springblueprint/testhelpers/utils/ClearableProxiedThreadScope.java`

### 7.4 `@UnitTest` Constructor Injection Internals

`InstanceParameterResolver` builds a lightweight dependency bag from test constructor parameter types.

Why this exists:
- provides simple constructor-based dependency injection for unit tests
- avoids spinning a full Spring context
- resets resolver state after each test method to avoid leaking state between tests

Relevant code:
- `api/src/test/java/com/theodo/springblueprint/testhelpers/junitextensions/InstanceParameterResolver.java`
- `api/src/test/java/com/theodo/springblueprint/testhelpers/utils/Instance.java`

### 7.5 Scheduled Task Harness and `application.yml`

`BaseScheduledTaskIntegrationTests` loads `application.yml` properties and uses an immediate one-time scheduler.

Why loading `application.yml` matters:
- many `@Scheduled` expressions reference properties
- tests need the same property resolution path as runtime to validate real schedule wiring

Relevant code:
- `api/src/test/java/com/theodo/springblueprint/testhelpers/baseclasses/BaseScheduledTaskIntegrationTests.java`

## 8. Query Count Guardrails

Purpose:
- prevent N+1 regressions
- document expected SQL footprint per tested SUT method

Mechanism:
- class-level `@AssertQueryCount`
- per-case `@Expected(count, sutActMethod, testMethod)`
- every test method on the annotated class needs a matching `@Expected` entry
- `QueryCountingExtension` checks actual query count

Example:

```java
@SetupDatabase
@Import({ UserRepository.class })
@ParentNestedDataJpaTest
@AssertQueryCount(
    sutClass = UserRepository.class,
    value = {
        @Expected(count = 1, sutActMethod = "create", testMethod = "creating_a_new_user_succeeds"),
        @Expected(count = 1, sutActMethod = "create", testMethod = "creating_a_new_user_returns_the_created_user")
    }
)
class UserRepositoryIntegrationTests {

    @Test
    void creating_a_new_user_succeeds() {
        userRepository.create(newUser);
        // assertions...
    }

    @Test
    void creating_a_new_user_returns_the_created_user() {
        User created = userRepository.create(newUser);
        // assertions...
    }
}
```

## 9. Act Pattern (PMD-Enforced)

Custom rule: `EnforceActPatternInTestsRule`.

Enforced constraints:
- exactly one `// Act` marker per supported test method
- banned phase comments: `Given`, `When`, `Then`, `Arrange`, `Assert`
- statement after `// Act` is mandatory and must trigger behavior
- Act statement must not be an assertion
- at least one statement must follow the Act statement
- spacing around Act is enforced for readability

Rule source:
- `api/src/main/java/com/theodo/pmd/customrules/EnforceActPatternInTestsRule.java`

## 10. Exception Testing Model

Two required levels:

1. Behavior-level exception tests:
- unit and contract tests verify when exceptions are thrown

2. API payload-level exception tests:
- exception handler integration tests verify HTTP status + ProblemDetail payload

Structure enforcement:
- `ExceptionHandlerRulesUnitTests` ensures handler test classes define required exception-provider methods

## 11. Determinism and Anti-Flake Defaults

`api/src/test/resources/junit-platform.properties`:
- parallel execution disabled by default
- randomized class and method order
- optional fixed seed overrides for failure reproduction

`api/src/test/resources/junit-logging.properties`:
- logs random seeds used by JUnit random orderers

Why:
- random order reveals hidden state coupling
- logged seeds make failures reproducible

## 12. Test Checklist

### ✅ Do

- treat domain tests as primary behavior specification
- invest in readable test helpers
- prefer fakes for repeated dependency setup
- use AssertJ for assertions
- keep endpoint integration tests focused on mapping/security contracts

### ❌ Do Not

- change production visibility only to make tests pass
- duplicate full business-branch coverage at endpoint layer
- skip query-count assertions on DB-sensitive adapter tests
- annotate tests with `@WithMockUser`; `TestsNamingConventionRulesUnitTests` forbids it. Use `getAccessTokenCookie(...)` helpers from `BaseWebMvcIntegrationTests` so tests exercise the real template authentication flow

## Navigation

- ⬅️ Previous: [3 - Project Structure and Conventions](03-project-structure-and-conventions.md)
- ➡️ Next: [5 - Build Toolchain and Quality Gates](05-build-toolchain-and-quality-gates.md)
