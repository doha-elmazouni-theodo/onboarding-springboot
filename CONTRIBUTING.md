# Contributing

Thanks for helping improve this Spring Boot template.

## Scope

- Keep changes focused.
- One concern per PR.
- Link each PR to an issue.

## Issue Labels

- Every issue must have exactly one type label: `bug`, `enhancement`, `documentation`, or `question`.
- Issue forms add the type label automatically.
- Issues created outside issue forms must be labeled manually before triage or implementation.

## First Contribution Path

1. Pick an open issue labeled `good first issue` or `status:ready-for-contrib`.
2. Comment on the issue to claim it.
3. Open a PR from your fork.

## Fork Workflow

1. Fork this repository.
2. Clone your fork locally.
3. Create a branch from `main`:
   - `feat/<short-topic>`
   - `fix/<short-topic>`
   - `docs/<short-topic>`
4. Push branch to your fork.
5. Open PR to `theodo-group/blueprint-springboot:main`.

## Local Setup

1. Read project setup in `README.md`.
2. Run canonical local validation before push:
   ```bash
   cd api
   ./mvnw spotless:apply verify
   ```

## Pull Request Rules

- Fill the PR template.
- Keep title clear and action-based.
- Add tests for behavior changes.
- Update docs if behavior/setup changes.
- Avoid unrelated refactors.

## Review Expectations

- Maintainers review based on correctness, tests, readability, and template fit.
- Change requests should be addressed in the same PR when possible.

## Need Help?

- Open a `question` issue with context and expected outcome.
- If blocked on an existing issue, comment there instead of creating duplicates.
