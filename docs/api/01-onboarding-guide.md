# Onboarding Guide

Practical entry point for first architecture trace after local bootstrap.

## 1. Start the Application

Prerequisites and bootstrap commands: [Root README](../../README.md).

```bash
cd api
./mvnw spring-boot:run
```

Wait until startup log shows application started successfully before continuing.

If startup fails immediately:
- verify Docker daemon is running
- verify port `8080` is free on your machine
- rerun `./mvnw spring-boot:run` after fixing the blocking issue

When started with DevTools (`./mvnw spring-boot:run` or IDE run), local-only config is injected from `application-localdev.yml`.

Local dev effects:
- Liquibase enabled on startup
- Spring Docker Compose integration enabled
- SpringDoc API docs enabled

## 2. Verify Local Access

- API base URL: <http://localhost:8080/api>
- Swagger UI: <http://localhost:8080/api/swagger-ui/index.html>
- PgAdmin: <http://localhost:8008>

## 3. Create First Admin User (Public Endpoint)

`GET /api/users` is protected with `@PreAuthorize("hasRole('ADMIN')")`, so create an admin user first.

```bash
export BLUEPRINT_USERNAME="newjoiner_admin_$(date +%s)"
export BLUEPRINT_PASSWORD="ChangeMe123"

curl -sS -X POST 'http://localhost:8080/api/auth/public/signup' \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"New Joiner\",\"username\":\"${BLUEPRINT_USERNAME}\",\"password\":\"${BLUEPRINT_PASSWORD}\",\"roles\":[\"ADMIN\",\"USER\"]}"
```

Expected: HTTP `201` with created user payload.

## 4. Login and Store Auth Cookies

```bash
curl -i -sS -c /tmp/blueprint.cookies -X POST 'http://localhost:8080/api/auth/public/login' \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${BLUEPRINT_USERNAME}\",\"password\":\"${BLUEPRINT_PASSWORD}\"}"
```

Expected:
- HTTP `200`
- `Set-Cookie` headers for `accessToken` and `refreshToken`

## 5. Call Protected Endpoint

```bash
curl -sS -b /tmp/blueprint.cookies 'http://localhost:8080/api/users'
```

Expected: HTTP `200` and users payload.

If HTTP `401` or `403`:
- rerun login step to refresh cookies
- ensure signup roles included `ADMIN`
- ensure request uses cookie jar with `-b /tmp/blueprint.cookies`

## 6. First Architecture Trace (`GET /api/users`)

Trace in this order:

1. endpoint: `features/users/api/endpoints/getusers/GetUsersEndpoint.java`
2. use case: `features/users/domain/usecases/getusers/GetUsersUseCase.java`
3. port: `features/users/domain/ports/UserRepositoryPort.java`
4. adapter: `common/infra/adapters/UserRepository.java`
5. response mapping: `features/users/api/endpoints/getusers/GetUsersEndpointResponse.java`

Verify during trace:
- business decisions stay in domain classes
- adapters do translation and I/O only

## 7. Daily Command Loop

During development, run this command before pushing:

```bash
cd api
./mvnw spotless:apply verify
```

Details on gates and failure interpretation:
- [Build Toolchain and Quality Gates](05-build-toolchain-and-quality-gates.md)

## 8. Query Count Guardrail Example

Purpose: prevent N+1 regressions and hidden query drift.

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

Full semantics:
- [Testing Platform](04-testing-platform.md#8-query-count-guardrails)

## 9. Common Pitfalls

- ❌ business logic moved into adapters because they are "near" endpoint/repository
- ❌ unreadable tests due to no shared helpers/fakes
- ❌ architecture rules treated as optional under delivery pressure

## Navigation

- ⬅️ Previous: [0 - Developer Guide](README.md)
- ➡️ Next: [2 - Architecture Overview](02-architecture-overview.md)
