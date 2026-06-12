# CI/CD Pipelines

Production-ready GitHub Actions workflows for **flowiq-automation** and **flowiq-backend** integration.

## Overview

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| [PR Validation](#1-pr-validation) | PR / push to `main`, `develop` | Fast quality gate |
| [API Smoke](#2-api-smoke) | `workflow_dispatch` | Live API smoke on demand |
| [UI Smoke](#3-ui-smoke) | `workflow_dispatch` | Live UI smoke on demand |
| [Nightly Regression](#4-nightly-regression) | Daily cron + manual | Full API regression + contracts |

```
.github/
├── workflows/
│   ├── pr-validation.yml
│   ├── api-smoke.yml
│   ├── ui-smoke.yml
│   └── nightly-regression.yml
├── actions/
│   ├── setup-java-maven/
│   ├── validate-test-secrets/
│   └── generate-allure-report/
└── scripts/
    ├── start-backend-ci.sh
    ├── stop-backend-ci.sh
    └── wait-for-backend.sh
```

## Prerequisites

### Repository variables

Configure under **Settings → Secrets and variables → Actions → Variables**:

| Variable | Default | Description |
|----------|---------|-------------|
| `BACKEND_REPOSITORY` | `YevheniiaDem/flowiq-backend` | GitHub `owner/repo` for backend checkout |

### Secrets

Configure under **Settings → Secrets and variables → Actions → Secrets**:

| Secret | Required for | Description |
|--------|--------------|-------------|
| `TEST_USER_EMAIL` | Smoke, nightly | Test user email (`stage` / `dev` environments) |
| `TEST_USER_PASSWORD` | Smoke, nightly | Test user password |
| `GH_PAT` | PR validation (optional) | Personal access token to checkout **private** `flowiq-backend` across repos |

For public backend repository, `GITHUB_TOKEN` is sufficient.

### GitHub Environments

Manual and nightly workflows use GitHub **Environments** (`stage`, `dev`):

1. Create environments: **Settings → Environments**
2. Add environment-specific secrets if they differ from repository secrets
3. Optional: required reviewers for `stage` / `dev` deployments

## 1. PR Validation

**File:** `.github/workflows/pr-validation.yml`  
**Triggers:** `pull_request`, `push` to `main` / `develop`

### Jobs

| Job | Runs | Maven command |
|-----|------|---------------|
| `compile` | flowiq-automation | `mvn clean compile test-compile` |
| `unit-tests` | flowiq-backend | `mvn test -Dtest=com.flowiq.unit.**` |
| `contract-tests` | automation + backend + PostgreSQL | `mvn test -Pcontract -Denv=local` |
| `pr-validation` | Gate | Fails if any required job failed |

`compile` and `unit-tests` run **in parallel**. `contract-tests` waits for `compile`, then:

1. Starts PostgreSQL 15 service container
2. Builds and starts `flowiq-backend` JAR against local DB
3. Waits for `GET /api/health`
4. Runs 15 JSON Schema contract tests against `http://localhost:8080/api`

### Artifacts (7 days)

- `pr-unit-surefire-reports`, `pr-unit-jacoco-report`
- `pr-contract-surefire-reports`, `pr-contract-allure-results`, `pr-contract-backend-log`

### Branch mapping

`BACKEND_REF` uses the PR head branch when available, so backend changes in the same branch name are tested together.

---

## 2. API Smoke

**File:** `.github/workflows/api-smoke.yml`  
**Trigger:** `workflow_dispatch` only

### Inputs

| Input | Options | Default |
|-------|---------|---------|
| `environment` | `stage`, `dev` | `stage` |

### Steps

1. Validate `TEST_USER_*` secrets
2. Maven cache + compile
3. `mvn test -Papi-smoke -Denv={environment}`
4. Generate **Allure HTML report** (`mvn allure:report`)

### Artifacts (14 days)

- `api-smoke-{env}-{run}-surefire`
- `api-smoke-{env}-{run}-allure-results`
- `api-smoke-{env}-{run}-allure-report` ← downloadable HTML
- `api-smoke-logs` (7 days)

### Local equivalent

```bash
mvn test -Papi-smoke -Denv=stage
mvn allure:serve
```

---

## 3. UI Smoke

**File:** `.github/workflows/ui-smoke.yml`  
**Trigger:** `workflow_dispatch` only

### Inputs

Same as API Smoke (`stage` / `dev`).

### Steps

1. Validate secrets
2. Maven cache + **Playwright browser cache**
3. Install Chromium (`playwright install --with-deps chromium`)
4. `mvn test -Pui-smoke -Denv={environment}`
5. Allure report generation

### Artifacts (14 days)

| Artifact | Path |
|----------|------|
| Traces | `target/traces` |
| Screenshots | `target/screenshots`, `target/test-output` |
| Videos | `target/videos`, `test-results` |
| Allure results + HTML report | `target/allure-results`, `target/site/allure-maven-plugin` |

### Local equivalent

```bash
mvn test -Pui-smoke -Denv=stage
```

---

## 4. Nightly Regression

**File:** `.github/workflows/nightly-regression.yml`  
**Triggers:**

- **Cron:** `0 3 * * *` (03:00 UTC daily)
- **Manual:** `workflow_dispatch` with `stage` / `dev`

### Steps

1. Validate secrets
2. `mvn test -Papi-regression -Denv={environment}` (~270 API regression tests)
3. `mvn test -Pcontract -Denv={environment}` (15 contract tests)
4. `mvn allure:report` — combined Allure report from both runs

### Artifacts (30 days)

- `nightly-surefire-{env}-{run}`
- `nightly-regression-{env}-{run}-allure-results`
- `nightly-regression-{env}-{run}-allure-report`
- `nightly-logs` (14 days)

### Local equivalent

```bash
mvn test -Papi-regression -Denv=stage
mvn test -Pcontract -Denv=stage
mvn allure:report
```

---

## Shared Infrastructure

### Maven caching

All workflows use `actions/setup-java@v4` with `cache: maven` (or composite action `setup-java-maven`).

UI Smoke additionally caches Playwright browsers under `~/.cache/ms-playwright`.

### Environment variables (workflow level)

```yaml
JAVA_VERSION: '17'
MAVEN_OPTS: '--batch-mode --fail-at-end -Dstyle.color=always'
```

Live-test workflows pass secrets to Maven via Owner/env resolution:

```properties
# environments/stage.properties
test.user.email=${TEST_USER_EMAIL}
test.user.password=${TEST_USER_PASSWORD}
```

### Concurrency

| Workflow | Policy |
|----------|--------|
| PR Validation | Cancel in-progress on new push |
| Smoke / Nightly | Do not cancel (preserve running test data) |

### Permissions

All workflows use minimal `contents: read` permission.

---

## Maven Profiles Reference

| Profile | Suite file | Use case |
|---------|------------|----------|
| `contract` | `contract-suite.xml` | JSON Schema contracts |
| `api-smoke` | `api-smoke-suite.xml` | API smoke |
| `ui-smoke` | `ui-smoke-suite.xml` | UI smoke |
| `api-regression` | `regression-api-suite.xml` | API regression |

Backend unit tests (separate repo):

```bash
cd flowiq-backend
mvn test -Dtest=com.flowiq.unit.**
```

---

## Troubleshooting

### PR Validation — backend checkout fails

- Ensure `BACKEND_REPOSITORY` variable is correct
- For private backend: add `GH_PAT` with `repo` scope
- Ensure branch `BACKEND_REF` exists in backend repo (falls back to PR branch name)

### Contract tests — backend won't start

- Download `pr-contract-backend-log` artifact
- Verify PostgreSQL service health in job logs
- Local repro: `docker compose up -d` in backend, then `mvn test -Pcontract -Denv=local`

### Smoke / Nightly — authentication errors

- Verify `TEST_USER_EMAIL` / `TEST_USER_PASSWORD` in repository or environment secrets
- Confirm credentials match `environments/{env}.properties` target API

### Allure report empty

- Ensure tests ran with AspectJ agent (configured in `pom.xml` surefire `argLine`)
- Download `*-allure-results` artifact first; HTML report depends on results

---

## Migration from legacy `ci.yml`

The old `CI` workflow (`mvn test -Dgroups=unit`) is replaced by **PR Validation**, which:

- Compiles automation framework
- Runs **backend** JUnit unit tests (`com.flowiq.unit`)
- Runs **contract** tests against a real backend in CI

Configure branch protection to require **PR Validation / PR Validation gate** status check.
