# Feature Matrix

> WIP: AI-generated, not challenged yet.

Scope: platform capabilities provided by the blueprint (not client business behavior).

Path legend:
- `common/*` and `features/*` shorthand mean `api/src/main/java/com/theodo/springblueprint/...`
- `testhelpers/*` and `transverse/*` shorthand mean `api/src/test/java/com/theodo/springblueprint/...`

## Architecture

| Capability | Why it matters | Evidence |
|---|---|---|
| Hexagonal package boundaries | keeps business logic independent from frameworks | `transverse/architecture/HexagonalArchitectureRulesUnitTests.java` |
| Domain use-case auto scan | removes manual bean wiring drift for domain use cases | `common/api/beans/DomainBeanConfiguration.java` |
| Port-first I/O design | adapters stay swappable and testable | `common/domain/ports/*`, `features/*/domain/ports/*` |
| REPR endpoint model | explicit endpoint ownership and transport isolation | `features/*/api/endpoints/*`, `transverse/architecture/api/*RulesUnitTests.java` |
| Use case shape guard (`handle`) | consistent use-case public API | `transverse/architecture/domain/UseCaseRulesUnitTests.java` |
| Time/UUID/random guardrails | avoids hidden non-determinism in domain code | `transverse/architecture/DesignRulesUnitTests.java`, `transverse/architecture/CodingRulesUnitTests.java` |

## Security

| Capability | Why it matters | Evidence |
|---|---|---|
| Dual filter chains (`/public/` + secured) | explicit public vs protected boundary | `common/api/security/WebSecurityConfiguration.java` |
| Method-level authorization requirement | protected routes require explicit auth intent | `common/api/security/WebSecurityConfiguration.java`, `transverse/architecture/api/ControllerConventionRulesUnitTests.java` |
| Stateless JWT resource server | avoids server-side session coupling | `common/api/security/StandaloneJwtAuthentication.java` |
| Request cache disable on auth failure | avoids unexpected saved-request behavior in API flows | `common/api/security/WebSecurityConfiguration.java` |
| Auth ProblemDetail entry point | consistent 401 payload contract | `common/api/security/AuthenticationProblemEntryPoint.java` |
| Actuator basic-auth filter | management plane is separately protected | `common/api/security/SimpleBasicAuthenticationFilter.java` |
| Management split-context security import | same actuator security wiring when management context is split | `api/src/main/resources/META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports` |

## Authentication

| Capability | Why it matters | Evidence |
|---|---|---|
| Token creation/rotation service | single authority for token lifecycle | `features/authentication/domain/services/RefreshTokenService.java` |
| Token-pair identity integrity checks | blocks access/refresh mismatch abuse | `features/authentication/domain/services/RefreshTokenService.java` |
| Cookie transport helper | consistent browser transport of auth tokens | `features/authentication/api/services/AuthenticationResponseEntity.java` |
| Scheduled expired-session purge | session-store hygiene over time | `features/authentication/api/schedules/cleanupsessions/CleanUpUserSessionsScheduledTask.java` |

## Data

| Capability | Why it matters | Evidence |
|---|---|---|
| JPA entities and repositories | explicit relational mapping layer | `common/infra/database/entities/*`, `common/infra/database/jparepositories/*` |
| Adapter exception translation | domain sees domain exceptions instead of raw infra exceptions | `common/infra/adapters/UserRepository.java`, `common/infra/adapters/UserSessionRepository.java` |
| Liquibase migration stack | repeatable schema evolution | `api/src/main/resources/db/changelog-master.yaml`, `api/src/main/resources/db/changelog/*.sql` |
| Migration helper script | standardized migration workflow | `api/db` |
| Destructive SQL guard + whitelist | safer migration reviews | `.ci/scripts/maven_springboot/check_destructive_queries.sh`, `api/src/main/resources/db/changelog-whitelist.yml` |
| Schema tools runner | catches model/schema drift | `common/infra/database/SchemaToolsRunner.java`, `common/infra/database/DatabaseMigrationIntegrationTests.java` |
| Soft-delete flush hardening | avoids deferred flush conflicts with partial unique indexes | `common/infra/configurations/HibernateEventListenersRegistrarBeanPostProcessor.java`, `common/infra/database/softdelete/SoftDeletableDbEntitiesIntegrationTests.java` |
| Time precision alignment | prevents nanosecond/microsecond mismatch issues | `common/infra/adapters/TimeProvider.java` |

## Observability

| Capability | Why it matters | Evidence |
|---|---|---|
| Event-driven logging listener | centralized domain/system event logging | `common/api/LoggingEventListener.java` |
| Unhandled exception event publishing | consistent failure signal path | `common/api/exceptionhandling/base/BaseResponseEntityExceptionHandler.java` |
| SQL comments + formatting | fast query source attribution | `common/infra/database/logging/*`, `common/infra/configurations/DataSourceProxyConfig.java` |

## Errors

| Capability | Why it matters | Evidence |
|---|---|---|
| Layered exception handlers | stable error contract by concern | `common/api/exceptionhandling/*`, `features/*/api/exceptionhandlers/*` |
| Legacy `/error` bridge | unified fallback path for legacy error entrypoint | `common/api/exceptionhandling/GlobalExceptionController.java` |
| Exception handler structure rules | prevents untested custom exception payloads | `transverse/architecture/ExceptionHandlerRulesUnitTests.java` |

## Serialization and Validation

| Capability | Why it matters | Evidence |
|---|---|---|
| Strict object mapper defaults | fail fast on malformed/ambiguous payloads | `common/api/beans/ObjectMapperConfiguration.java` |
| Immutable collection OpenAPI converter | correct schema generation for Eclipse immutable types | `common/api/beans/ImmutableArraysModelConverter.java` |
| `@Parsable` validation helper | reusable parseability validation patterns | `common/api/annotations/Parsable.java`, `common/utils/ParsableChecker.java` |
| Eclipse `@NotEmpty` validator integration | validation compatibility for Eclipse Collections | `common/api/validators/NotEmptyValidatorForEclipseRichIterable.java`, `api/src/main/resources/META-INF/services/jakarta.validation.ConstraintValidator` |

## Build and Quality

| Capability | Why it matters | Evidence |
|---|---|---|
| Maven wrapper + strict Maven config | reproducible local/CI behavior | `api/mvnw`, `api/.mvn/maven.config`, `api/.mvn/jvm.config` |
| Build-time extension | visibility into build phase timings | `api/.mvn/extensions.xml` |
| Reproducible artifact timestamping | deterministic build outputs | `api/pom.xml` (`project.build.outputTimestamp`, `git-commit-id-maven-plugin`) |
| Spotless formatting gate | consistent code formatting | `api/pom.xml`, `api/formatter.xml` |
| PMD + custom rules | static enforcement of coding/test conventions | `api/pmd/*`, `api/src/main/java/com/theodo/pmd/customrules/EnforceActPatternInTestsRule.java` |
| JaCoCo thresholds | coverage floor protection | `api/pom.xml` |
| PIT thresholds | mutation-quality floor | `api/pom.xml` |
| Checker nullness profile + stubs | compile-time nullability safety | `api/pom.xml`, `api/src/main/stubs/*` |
| Maven Enforcer rules | dependency graph integrity | `api/pom.xml` |
| Surefire partitioning + reporter | fail-fast feedback with readable test output | `api/pom.xml` |

## Supply Chain

| Capability | Why it matters | Evidence |
|---|---|---|
| OWASP dependency-check + suppressions | CVE visibility with explicit risk acceptance | `api/pom.xml`, `api/dependency-check-suppressions.xml` |
| Pinned GitHub Action SHAs | supply-chain tampering risk reduction | `.github/workflows/*` |
| Pinned image digests | deterministic container provenance | `docker-compose.dependencies.yml`, workflow files |
| Renovate governance policy | controlled dependency update cadence | `.ci/renovate/renovate.json5` |

## Testing

| Capability | Why it matters | Evidence |
|---|---|---|
| Test naming/category architecture rules | predictable test execution model | `transverse/architecture/TestsNamingConventionRulesUnitTests.java` |
| Query-count extension | query regression/N+1 detection | `testhelpers/junitextensions/querycount/QueryCountingExtension.java` |
| Testcontainers DB extension | realistic DB behavior in tests | `testhelpers/junitextensions/SetupTestDatabaseExtension.java` |
| Compose/Testcontainers DB image parity | avoids version drift between local and test DBs | `testhelpers/junitextensions/SetupTestDatabaseExtension.java`, `docker-compose.dependencies.yml` |
| Parent nested Spring extension | stable nested abstract JPA tests | `testhelpers/junitextensions/ParentNestedSpringExtension.java`, `testhelpers/annotations/ParentNestedDataJpaTest.java` |
| Test-only DB type switch | explicit control of test-only entities/repositories | `testhelpers/annotations/IncludeTestOnlyDbTypes.java`, `testhelpers/configurations/IgnoreTestOnlyDbTypesConfiguration.java` |
| Base app test without DB auto-mocks | avoids missing-bean startup failures in no-DB app tests | `testhelpers/baseclasses/BaseApplicationTestsWithoutDb.java` |
| Base web per-call scope + stale-bean checks | prevents cross-test bean reuse leaks | `testhelpers/baseclasses/BaseWebMvcIntegrationTests.java`, `testhelpers/utils/ClearableProxiedThreadScope.java` |
| Scheduled-task immediate scheduler harness | deterministic tests for `@Scheduled` paths | `testhelpers/baseclasses/BaseScheduledTaskIntegrationTests.java` |
| `@UnitTest` instance resolver | lightweight constructor DI for unit tests | `testhelpers/junitextensions/InstanceParameterResolver.java`, `testhelpers/utils/Instance.java` |
| Fail-fast extension | faster stop on cascading failures | `testhelpers/junitextensions/FailFastExtension.java` |
| Flow-test helpers | scenario-level behavior verification | `transverse/flowtests/helpers/*` |
| Randomized class/method ordering with seed logs | reveals hidden test coupling and supports reproduction | `api/src/test/resources/junit-platform.properties`, `api/src/test/resources/junit-logging.properties` |

## CI/CD

| Capability | Why it matters | Evidence |
|---|---|---|
| Reusable workflow composition (`all*`) | consistent CI/CD topology across repos | `.github/workflows/all.yml`, `.github/workflows/all_*.yml` |
| Path-based dynamic CI enablement | skips unaffected heavy jobs safely | `.github/workflows/all_configure.yml`, `.github/actions/check-changes/action.yml` |
| Layered CI jobs (compile/tests/mutation/security/startup) | pre-merge defense-in-depth | `.github/workflows/_ci_maven_springboot.yml` |
| Blueprint-only regression digest checks | protects template test/report behavior over time | `.github/workflows/blueprint-tests.yml`, `.ci/scripts/blueprint/maven_springboot/*` |
| Workflow end-marker required status | stable branch-protection status key | `.github/workflows/all.yml`, `.github/default_branch_ruleset.json` |
| Scheduled dependency updates and scans | continuous dependency hygiene | `.github/workflows/scheduled-dependencies-update.yml`, `.github/workflows/scheduled-dependency-security-check.yml`, `.ci/renovate/renovate.json5` |

## DevOps and Runtime

| Capability | Why it matters | Evidence |
|---|---|---|
| Hardened Docker image + healthcheck | safer runtime defaults and operability | `api/Dockerfile` |
| Container health verification action | catches healthcheck regressions in CI | `.github/actions/verify-container-image-health/action.yml` |
| Compose local dependencies + PgAdmin | reproducible local DB stack | `docker-compose.yml`, `docker-compose.dependencies.yml` |
| Startup smoke script | quick startup regression detection | `.ci/scripts/maven_springboot/localdev_startup_smoke.sh` |
| DevTools-triggered localdev property injection | local ergonomics without production leakage | `common/api/LocalDevPropertySourceInjecter.java`, `api/src/main/resources/application-localdev.yml` |

## Governance

| Capability | Why it matters | Evidence |
|---|---|---|
| Branch ruleset | enforces review + merge policy + required status checks | `.github/default_branch_ruleset.json` |
| PR/issue templates | structured collaboration inputs | `.github/pull_request_template.md`, `.github/ISSUE_TEMPLATE/*` |
| Contribution contract | shared contribution expectations | `CONTRIBUTING.md` |

## Out of Scope

- client-specific business behavior and product rules

## Navigation

- ⬅️ Previous: [9 - CI/CD and Governance](09-ci-cd-and-governance.md)
