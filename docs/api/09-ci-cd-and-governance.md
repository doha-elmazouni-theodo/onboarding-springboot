# CI/CD and Governance

This document describes the GitHub-based delivery pipeline and repository governance model used by the blueprint.

## 1. Workflow Topology

Primary workflow graph:

```text
all.yml
  |
  +--> all_configure.yml   (builds runtime CI config from repo changes)
  |
  +--> all_cicd.yml
         |
         +--> all_ci.yml   (quality gates)
         |
         +--> all_cd.yml   (deployment placeholder)
         |
         +--> blueprint-tests.yml (blueprint repo only)
```

Why this layout exists:
- shared CI/CD shape across repositories
- centralized change-detection and runtime config generation
- clear separation of CI quality gates and CD concerns

## 2. Dynamic Configuration and Change Detection

`all_configure.yml`:
- runs case-sensitivity path check
- checks watched paths with `.github/actions/check-changes`
- emits JSON config consumed by downstream workflows

Effect:
- backend pipeline can be skipped on PRs that do not touch backend-relevant paths

Main files:
- `.github/workflows/all_configure.yml`
- `.github/actions/check-changes/action.yml`
- `.ci/scripts/common/detect-case-sensitivity-issues-in-paths.sh`

## 3. CI Contract (`_ci_maven_springboot.yml`)

Main jobs:
- `compile`
  - destructive SQL guard
  - compile/package/static checks
  - publishes compilation artifacts for downstream jobs
- `tests`
  - runs `verify` with selected skip flags for already-covered static steps
  - equivalent intent to local `./mvnw spotless:apply verify`, but split across CI jobs for speed
  - publishes reports on failure/exposure
- `mutation-tests`
  - runs PIT mutation checks
- `dependency-security-check`
  - PR-only incremental dependency vulnerability check
- `compose-health-and-startup`
  - validates container health contract
  - runs startup smoke script

Workflow file:
- `.github/workflows/_ci_maven_springboot.yml`

## 4. Composite Actions and Script Inventory

### 4.1 Composite Actions (`.github/actions`)

- `setup-java`
- `check-changes`
- `download-dependency-check-database`
- `incremental-dependency-check`
- `verify-container-image-health`

### 4.2 CI/CD Scripts (`.ci/scripts`)

- `.ci/scripts/common/detect-case-sensitivity-issues-in-paths.sh`
- `.ci/scripts/maven_springboot/check_destructive_queries.sh`
- `.ci/scripts/maven_springboot/incremental-dependency-check.sh`
- `.ci/scripts/maven_springboot/localdev_startup_smoke.sh`
- `.ci/scripts/blueprint/maven_springboot/generate_jacoco_digest.py`
- `.ci/scripts/blueprint/maven_springboot/generate_surefire_digest.py`
- `.ci/scripts/blueprint/maven_springboot/generate_pitest_digest.py`

## 5. Scheduled Automation

### 5.1 Dependency Update Automation

Workflow:
- `.github/workflows/scheduled-dependencies-update.yml`

Schedules (UTC, weekdays):
- `08:50` cron: PR creation + updates
- `09:00` to `17:00` hourly cron: PR updates/rebases only (no new PR creation)

How it is implemented:
- workflow checks `github.event.schedule`
- passes Renovate schedule to allow or deny PR creation for that run

Renovate policy file:
- `.ci/renovate/renovate.json5`

### 5.2 Daily Vulnerability Scan

Workflow:
- `.github/workflows/scheduled-dependency-security-check.yml`

Runs OWASP dependency-check on all dependencies (not only new ones) to catch newly disclosed CVEs.

## 6. Blueprint-Only Regression Workflow

`blueprint-tests.yml` is intended only for the blueprint repository itself.

Why:
- it compares report digests against baseline runs to protect template behavior
- this is template-maintenance logic, not client-project delivery logic

Current safety condition:
- `IS_BLUEPRINT` is defined only in the blueprint repository
- in `.github/workflows/all_cicd.yml`, job runs only if
  `vars.IS_BLUEPRINT == 'true'`

For client projects:
- remove or keep disabled this workflow path
- keep standard CI quality gates (`all_ci.yml` and `_ci_maven_springboot.yml`)

## 7. Branch Governance

Branch ruleset file:
- `.github/default_branch_ruleset.json`

Main enforced policies:
- pull request required
- minimum one approval
- code-owner review required
- resolved review threads required
- linear history required
- merge methods limited to `squash` and `rebase`
- required status check: `Workflow end marker`

## 8. Required Secrets and External Tokens

From repository setup perspective:
- NVD API key required for dependency-check workflows (`NVD_API_KEY`)
- Renovate token required for scheduled dependency updates (`RENOVATE_GITHUB_TOKEN`)

## 9. Collaboration Artifacts

Repository governance assets:
- PR template: `.github/pull_request_template.md`
- issue templates: `.github/ISSUE_TEMPLATE/*`
- contribution guide: `CONTRIBUTING.md`

## 10. Checklist

### ✅ Do

- run `./mvnw spotless:apply verify` locally before PR
- keep PR scope focused and validation notes explicit
- treat dependency suppressions as reviewed exceptions

### ❌ Do Not

- bypass CI findings by weakening guards without architecture discussion
- run blueprint-only digest workflows in client project CI by default
- merge dependency updates without compatibility/security review

## Navigation

- ⬅️ Previous: [8 - Security, Observability, and Error Handling](08-security-observability-and-error-handling.md)
- ➡️ Next: [10 - Feature Matrix](10-feature-matrix.md)
