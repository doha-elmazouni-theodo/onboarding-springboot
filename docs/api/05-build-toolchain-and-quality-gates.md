# Build Toolchain and Quality Gates

This blueprint uses quality gates to enforce architecture and coding contracts automatically.

## 1. Baseline

- Java target: 25 (`api/pom.xml`, `api/.tool-versions`)
- Maven wrapper pinned (`api/.mvn/wrapper/maven-wrapper.properties`)
- strict Maven defaults in `api/.mvn/maven.config` (`--strict-checksums`, `--show-version`, build-time logging)
- JVM module/export flags in `api/.mvn/jvm.config`
- Maven build-time extension enabled in `api/.mvn/extensions.xml`

## 2. Coverage and Mutation Thresholds

Baseline thresholds (from `api/pom.xml`):
- mutation: `95`
- domain coverage: `95`
- domain test strength: `98`
- total line coverage: `95`
- total branch coverage: `80`

Blueprint-only stricter profile:
- profile id: `blueprint`
- activation marker: existence of `src/main/java/com/theodo/springblueprint/Application.java`
- effect: raises thresholds to `100`

Why this matters for client projects:
- when creating a client project, package/class renaming usually removes this marker
- after that, baseline thresholds apply unless your project defines stricter custom thresholds

## 3. Command Intent

| Command | Use this when | Notes |
|---|---|---|
| `./mvnw spotless:apply verify` | default full local validation before PR | runs formatting + tests + static checks + coverage checks + PIT checks |
| `./mvnw org.pitest:pitest-maven:mutationCoverage -Dmaven.gitcommitid.skip=true` | isolated mutation debugging | useful when you only need PIT feedback |
| `./mvnw org.pitest:pitest-maven:mutationCoverage -Dmaven.gitcommitid.skip=true -DtargetClasses=...` | fast local mutation iteration on narrowed scope | optimization only; run full configured scope before final validation |
| `.ci/scripts/maven_springboot/localdev_startup_smoke.sh api` | diagnose startup regressions quickly | compile + start + wait for startup marker |

### 3.1 Failure Triage Order

When `./mvnw spotless:apply verify` fails, triage in this order:
1. architecture rule failures: check failing `*RulesUnitTests` under `transverse/architecture/**`
2. PMD failures: check `api/pmd/custom/*.xml` rule intent and fix code shape
3. nullability failures: align contracts/annotations (see [Nullability and Checker Framework](06-nullability-and-checker-framework.md))
4. integration/application failures: inspect boundary setup and fixtures
5. mutation failures: add tests that kill surviving mutants, then rerun full validation

Primary report locations:
- `api/target/reports/surefire`
- `api/target/reports/pmd`
- `api/target/reports/jacoco`
- `api/target/reports/pitest`

## 4. PMD Custom Packs and Why They Exist

PMD entrypoint:
- `api/pmd/pmd-rule-set.xml`

Custom packs:

| Pack | File | Guardrails | Why it exists |
|---|---|---|---|
| Controller rules | `api/pmd/custom/controller-rules.xml` | request DTOs require `@Valid`; ban `@PreAuthorize("permitAll()")` | keep validation explicit and avoid accidental public exposure patterns |
| Design rules | `api/pmd/custom/design-rules.xml` | enforce Eclipse Collections in fields and non-private signatures; enforce local collection factories | keep collection contracts consistent and explicit |
| Generic rules | `api/pmd/custom/generic-rules.xml` | disallow `@UuidGenerator`; disallow `content().json(...)` in `BaseWebMvcIntegrationTests`; constrain `@Builder` usage | prevent hidden randomness paths, enforce consistent JSON assertions, and enforce builder policy |
| JPA rules | `api/pmd/custom/jpa-rules.xml` | ban `nullable=` in JPA annotations | single-source nullability semantics through `@Nullable`/`@NotNull` + checker tooling |
| Test rules | `api/pmd/custom/test-rules.xml` | disallow JUnit assertions; enforce Act pattern rule | keep assertion style consistent and test structure readable |

### 4.1 Builder Policy

Policy intent:
- Lombok `lombok.Builder` is forbidden.
- Jilt `org.jilt.Builder` is the allowed builder annotation.
- Jilt builders must declare `factoryMethod = ...`.
- Lombok builder permits constructing objects with `null` values for non-null fields, which conflicts with the nullness-checker contract.

Technical detail:
- PMD resolves annotation names by simple name in this rule context, so it cannot reliably distinguish Lombok and Jilt in every case.
- `factoryMethod = ...` is used as the enforceable marker for allowed Jilt usage.

## 5. ArchUnit Rule Classes

Architecture rules location:
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/api/`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/domain/`
- `api/src/test/java/com/theodo/springblueprint/transverse/architecture/infra/`

### 5.1 `transverse/architecture/`

| Rule class | Scope | What it protects |
|---|---|---|
| `CodingRulesUnitTests` | coding conventions | logger modifiers, no `java.util.logging`, no direct `UUID.randomUUID()` outside adapter, validated configuration properties |
| `DesignRulesUnitTests` | design conventions | no direct wall-clock access in domain paths; no field injection |
| `ExceptionHandlerRulesUnitTests` | exception handler testing structure | ensures exception handler integration tests provide required exception cases |
| `HexagonalArchitectureRulesUnitTests` | package dependencies | domain dependency direction, no subdomain cross-coupling, ports over concrete adapters |
| `NamingConventionRulesUnitTests` | shared naming | event/exception naming consistency |
| `TestsNamingConventionRulesUnitTests` | test architecture | test suffixes/categories, `@Primary` in app test configs, auth helper policy |

### 5.2 `transverse/architecture/api/`

| Rule class | Scope | What it protects |
|---|---|---|
| `ControllerConventionRulesUnitTests` | controller mappings/auth | no class-level mapping; `@PreAuthorize` requirements for mapped methods |
| `NamingConventionRulesUnitTests` | API naming/placement | endpoint/request/response/schedule naming and package placement |
| `RequestResponseConventionRulesUnitTests` | transport model contracts | request/response allowed field types and required request constraints |

### 5.3 `transverse/architecture/domain/`

| Rule class | Scope | What it protects |
|---|---|---|
| `NamingConventionRulesUnitTests` | domain naming/placement | use case/port/event/domain exception placement and suffix contracts |
| `UseCaseRulesUnitTests` | use case method shape | one `handle` with max one argument |

### 5.4 `transverse/architecture/infra/`

| Rule class | Scope | What it protects |
|---|---|---|
| `JpaRulesUnitTests` | JPA conventions | DB entity/repository naming and structural consistency |

## 6. Test Execution Topology

Surefire executions:
1. architecture tests
2. unit tests
3. integration/application tests

Why:
- architectural breakages fail early
- faster local feedback during iteration

## 7. Nullability Checker and Stubs

Nullness profile:
- auto-enabled on JDK 9+ (`nullness-checker` profile)
- compiler runs with `-Werror`
- stub path configured via `-Astubs=${basedir}/src/main/stubs`

What stubs are:
- `.astub` files that provide nullability contracts for APIs lacking sufficient annotations

Why stubs are required:
- third-party libraries often omit full nullability metadata
- without stubs, checker output can be noisy or inaccurate
- with stubs, nullness analysis is more deterministic

Stub directory:
- `api/src/main/stubs`

For detailed nullability usage and troubleshooting:
- [Nullability and Checker Framework](06-nullability-and-checker-framework.md)

## 8. Reproducibility Guardrails

Reproducible build configuration in `api/pom.xml`:
- `project.build.outputTimestamp=${git.commit.time}`
- `git-commit-id-maven-plugin` configured for stable metadata output
- non-deterministic git properties excluded from artifact metadata

## 9. Checklist

### ✅ Do

- run `./mvnw spotless:apply verify` before opening PR
- keep PMD/ArchUnit rules aligned with documented conventions
- update docs when adding or changing custom rules

### ❌ Do Not

- lower thresholds to bypass regressions
- disable nullness checker as a permanent workaround
- weaken rule packs without documenting architecture rationale

## Navigation

- ⬅️ Previous: [4 - Testing Platform](04-testing-platform.md)
- ➡️ Next: [6 - Nullability and Checker Framework](06-nullability-and-checker-framework.md)
