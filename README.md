# Theodo Spring Boot Blueprint

Opinionated Spring Boot template used to bootstrap new client backend projects.

## Prerequisites

- Java 25+
- Docker Compose v2.20+
- Unix shell (macOS/Linux/Git Bash)
- Docker daemon running locally

## Local Start

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

Local URLs:
- API base URL: <http://localhost:8080/api>
- Swagger UI: <http://localhost:8080/api/swagger-ui/index.html>
- PgAdmin: <http://localhost:8008>

## Must-Know Commands

Run from `api/` unless stated otherwise.

| Command | Why you need it |
|---|---|
| `./mvnw spotless:apply verify` | canonical local validation before opening/updating PR |
| `./db applyMigrations` | apply Liquibase migrations manually |
| `./db makeMigration <label>` | generate migration from ORM/schema diff and append to changelog master |
| `docker compose port db 5432` (from repo root) | show mapped local DB port |

## Start Here

- Developer guide: [Developer Guide](docs/api/README.md)
- Contribution workflow: [CONTRIBUTING.md](CONTRIBUTING.md)
