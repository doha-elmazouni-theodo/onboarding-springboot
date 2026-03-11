# Security, Observability, and Error Handling

## 1. Security Posture

Core model:
- stateless security (`SessionCreationPolicy.STATELESS`)
- method-level authorization enabled
- explicit public-path convention using `/public/`

Filter chains (`WebSecurityConfiguration`):
- public chain: requests whose URI contains `/public/` are permitted
- secured chain: all other requests use JWT resource server authentication, with the bearer token resolved from the `accessToken` cookie
- request cache is disabled (`NullRequestCache`)

## 2. Authorization Contract

Controller rule (`ControllerConventionRulesUnitTests`):
- mapped controller methods must declare `@PreAuthorize`, except when:
  - every declared mapping path contains `/public/`, or
  - method is declared in an `ErrorController` implementation

This keeps authorization intent explicit on protected routes.

## 3. Browser Transport Defaults

CORS is centralized in `WebSecurityConfiguration`.

Current defaults:
- `allowCredentials=true`
- allowed origin pattern: `*`
- allowed headers: `*`
- allowed methods: `*`

These defaults are convenient for development and must be hardened for production.

### 3.1 Hardening CORS for Production

Current CORS defaults are defined in `WebSecurityConfiguration.allowAllOrigins()`.
This is a blueprint enhancement item for maintainers/contributors: harden CORS behavior in the template itself via a blueprint PR so future generated projects inherit secure defaults (tracked in [#504](https://github.com/theodo-group/blueprint-springboot/issues/504)).

For production, replace wildcard origins with values provided from environment-backed configuration (not hardcoded), for example:

```java
// In security configuration, inject Environment (or @Value) and map configured origins
String configuredOrigins = environment.getProperty("security.cors.allowed-origins", "");
config.setAllowedOrigins(Arrays.stream(configuredOrigins.split(","))
    .map(String::trim)
    .filter(origin -> !origin.isEmpty())
    .toList());
config.addAllowedHeader("*");
config.addAllowedMethod("*");
config.setAllowCredentials(true);
```

Example env/property value:
- `SECURITY_CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com`

## 4. JWT and Cookie Transport

`JwtTokenClaimsCodec` implements:
- domain token codec contract
- Spring `JwtDecoder` contract

Cookie transport is handled by `AuthenticationResponseEntity`.

Cookie properties:
- `HttpOnly`
- `SameSite=Strict`
- `Secure` controlled by `security.https`
- refresh/logout path scoping

Design choice:
- access-token cookie max-age is aligned with refresh-token validity to keep both cookies available during refresh-token integrity checks.

## 5. Token Lifecycle Integrity

`RefreshTokenService` validates:
- refresh token exists and is not expired
- access token and refresh token belong to the same user
- previous refresh token is invalidated during rotation

This blocks token-pair mismatch and stale-token replay patterns.

## 6. Actuator Protection

Actuator endpoints (`/api/actuatorz/**`) are protected by `SimpleBasicAuthenticationFilter`.

Credentials source:
- `management.server.user`
- `management.server.password`

### 6.1 Management Split-Context Handling

When management endpoints run on a separate management context/port, Spring Boot creates a separate application context for that management plane.

To ensure actuator security is still loaded in that separate context, the file below imports the security configuration explicitly:
- `api/src/main/resources/META-INF/spring/org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration.imports`

Without this import, actuator security wiring may differ between same-port and split-port deployments.

## 7. Error Contract

Error payload model:
- Spring `ProblemDetail`
- stable error code/title conventions (`errors.*`)

`stable` means one error case always maps to the same `errors.*` code over time.
Treat these codes as API contract keys for clients; changing a code is a contract change.

Exception handler layers:
- `SpringExceptionHandler`
- `AuthenticationExceptionHandler`
- feature-scoped handlers in `features/*/api/exceptionhandlers/*` (for example `UsersExceptionHandler`)
- `CatchAllExceptionHandler`
- `GlobalExceptionController` for legacy `/error` bridge behavior

Authentication failures are normalized through `AuthenticationProblemEntryPoint`.

### 7.1 Feature Handler Scope

Feature handlers should only map known feature/domain exceptions to HTTP status + stable `errors.*` codes via `getDefaultResponseEntity(...)`.

Do not put business logic, repository/service calls, or manual event publishing in handlers.

When adding a new feature exception, update the corresponding `*ExceptionHandlerIntegrationTests` `staticProvideExceptions()` mapping so contract coverage stays complete.

Minimal handler mapping example:

```java
// 🎯 Map one domain exception to one HTTP contract
@ExceptionHandler({ UsernameAlreadyExistsInRepositoryException.class })
@Nullable public ResponseEntity<Object> handleException(
    UsernameAlreadyExistsInRepositoryException ex,
    WebRequest request) {
    // ✅ Centralized ProblemDetail creation (status + error code)
    return getDefaultResponseEntity(
        ex,
        request,
        HttpStatus.BAD_REQUEST,
        // 🔐 Stable API error code (`errors.*`)
        UsersApiErrorCodes.USERNAME_ALREADY_EXISTS_ERROR
    );
}
```

Matching test mapping example:

```java
// 🧪 Keep this list exhaustive for custom exceptions in the package
private static Stream<Arguments> staticProvideExceptions() {
    return Stream.of(
        Arguments.of(
            // 🎯 Thrown exception
            new UsernameAlreadyExistsInRepositoryException(new Username(""), new DataIntegrityViolationException("")),
            // ✅ Expected HTTP status
            HttpStatus.BAD_REQUEST,
            // ✅ Expected ProblemDetail payload
            """
            {"type":"about:blank","title":"errors.username_already_exists","status":400,
            "detail":"errors.username_already_exists","instance":"/exception-handling/throw"}"""
        )
    );
}
```

## 8. Observability

### 8.1 Event-Driven Logging

- handled exceptions publish `UnhandledExceptionEvent`
- domain events are published through `EventPublisherPort`
- `LoggingEventListener` logs events with source attribution

### 8.2 SQL Observability

Datasource proxy instrumentation (`DataSourceProxyConfig`) provides:
- SQL formatting (`DatabaseQueryLogger`)
- caller comments (`CommentingQueryTransformer`)

This complements query-count assertions from [Testing Platform](04-testing-platform.md#8-query-count-guardrails).

### 8.3 OpenAPI and Serialization

- OpenAPI exposure controlled by `springdoc.api-docs.enabled`
- `ImmutableArraysModelConverter` maps Eclipse immutable collections correctly in schema generation
- `ObjectMapperConfiguration` keeps deserialization strict for early failure on malformed input

## 9. Production Hardening Checklist

Before production deployment:
- replace wildcard CORS origins with explicit trusted origins
- set strong JWT secret and correct issuer from secret manager
- set `security.https=true` behind TLS so cookies are marked `Secure`
- override actuator credentials from secure secret sources
- verify only intended endpoints use `/public/`
- disable or restrict OpenAPI docs exposure outside trusted environments

### 9.1 Example Production Overrides

Typical production override snippet:

```yaml
security:
  https: true
  jwt:
    issuer: your-production-issuer
    secret: ${JWT_SECRET_FROM_SECRET_MANAGER}

management:
  server:
    user: ${ACTUATOR_USER}
    password: ${ACTUATOR_PASSWORD}

springdoc:
  api-docs:
    enabled: false
```

## 10. Checklist

### ✅ Do

- keep auth intent explicit with `@PreAuthorize` on protected endpoints
- keep ProblemDetail payload shape stable
- keep token lifecycle behavior centralized in domain services
- use SQL comments + query-count assertions together

### ❌ Do Not

- use `@PreAuthorize("permitAll()")` as public-access shortcut
- return ad-hoc error payload formats
- scatter token/cookie behavior across unrelated endpoints
- keep development-grade CORS/security defaults in production

## Navigation

- ⬅️ Previous: [7 - Data Persistence and Migrations](07-data-persistence-and-migrations.md)
- ➡️ Next: [9 - CI/CD and Governance](09-ci-cd-and-governance.md)
