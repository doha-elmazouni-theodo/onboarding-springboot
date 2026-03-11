# Data Persistence and Migrations

## 1. Persistence Stack

- PostgreSQL at runtime
- Spring Data JPA repositories
- Liquibase SQL changelog lifecycle

## 2. Persistence Boundaries

Where persistence code lives:
- DB entities: `common/infra/database/entities/*DbEntity`
- JPA repositories: `common/infra/database/jparepositories/*`
- domain-facing adapters: `common/infra/adapters/*`

Adapter responsibilities:
- map DB entities to domain models
- map domain models to DB entities
- translate infrastructure exceptions into domain exceptions

Boundary rule:
- domain code depends on ports, never directly on JPA repositories.

## 3. Migration Workflow

Migration entrypoints:
- master changelog: `api/src/main/resources/db/changelog-master.yaml`
- helper script: `api/db`

Commands:

```bash
cd api
./db applyMigrations
./db makeMigration <label>
```

`makeMigration` behavior:
- applies current migrations first
- generates diff-based changelog if schema drift exists
- appends generated file to changelog master
- no file is generated if no effective schema diff exists

## 4. Migration Safety Guard

CI checks SQL changelogs for destructive patterns:
- script: `.ci/scripts/maven_springboot/check_destructive_queries.sh`
- whitelist: `api/src/main/resources/db/changelog-whitelist.yml`

When destructive SQL is intentional:
- keep explicit whitelist entry with clear reason

## 5. Schema Drift Detection Tooling

`SchemaToolsRunner` supports:
- exporting mapped schema
- validating mapped schema against actual DB

Tooling/tests using it:
- `api/src/test/java/com/theodo/springblueprint/common/infra/database/DatabaseMigrationIntegrationTests.java`
  - verifies Liquibase diff is non-empty on fresh DB
  - verifies Liquibase diff is empty after applying migrations
  - verifies Hibernate validation passes on migrated schema

This gives early signal when ORM mapping and migration scripts diverge.

## 6. Soft-Delete Hardening

`HibernateEventListenersRegistrarBeanPostProcessor` registers a delete listener that flushes after soft-delete operations (outside cascade contexts).

Why:
- avoids delayed flush behavior that can violate partial unique indexes in the same transaction when creating replacements after soft delete.

Reference tests explaining the issue and expected behavior:
- `api/src/test/java/com/theodo/springblueprint/common/infra/database/softdelete/SoftDeletableDbEntitiesIntegrationTests.java`

## 7. Query Visibility and Performance Guardrails

SQL observability components:
- `DatabaseQueryLogger`
- `CommentingQueryTransformer`

Query footprint enforcement:
- `@AssertQueryCount` + `QueryCountingExtension`

Reference integration tests:
- `api/src/test/java/com/theodo/springblueprint/common/infra/adapters/UserRepositoryIntegrationTests.java`
- `api/src/test/java/com/theodo/springblueprint/common/infra/adapters/UserSessionRepositoryIntegrationTests.java`

For query-count mechanics, see:
- [Testing Platform](04-testing-platform.md#8-query-count-guardrails)

## 8. Timestamp Precision Guard

`TimeProvider` truncates instants to microsecond precision before persistence.

Why:
- Java `Instant` can carry finer precision than PostgreSQL timestamps
- truncation avoids round-trip equality surprises and drift-sensitive bugs

## 9. Local Inspection with PgAdmin

PgAdmin URL: <http://localhost:8008>

Recommended usage:
- inspect migration effects
- inspect row state after integration testing
- inspect session/token lifecycle side effects

## 10. Checklist

### ✅ Do

- keep persistence access behind domain ports
- keep adapter mapping explicit and test-covered
- manage schema changes through migrations
- keep query-count assertions on hot DB paths

### ❌ Do Not

- patch schema manually outside migration scripts
- expose JPA entities directly at API boundary
- leak JPA repository dependencies into domain classes

## Navigation

- ⬅️ Previous: [6 - Nullability and Checker Framework](06-nullability-and-checker-framework.md)
- ➡️ Next: [8 - Security, Observability, and Error Handling](08-security-observability-and-error-handling.md)
