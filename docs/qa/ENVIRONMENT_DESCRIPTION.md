# FlowIQ Environment Description

| Field | Value |
|-------|-------|
| **Document ID** | QA-ENV-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Overview

FlowIQ tests run against configurable environments loaded by Owner `EnvironmentConfig`:

```java
@Sources({
    "classpath:environments/local.properties",
    "classpath:environments/${env}.properties",
    "system:properties",
    "system:env"
})
```

Activate via Maven: `-Plocal`, `-Pci`, `-Pstage`, `-Pdev`, `-Pdocker`.

## 2. Environment Catalog

| Environment | Property file | `base.url` | `api.url` | Primary use |
|-------------|---------------|------------|-----------|-------------|
| **local** | `local.properties` | `http://localhost:3000` | `http://localhost:8080/api` | Developer workstation |
| **docker** | `docker.properties` | Docker Compose URLs | Same stack | Local containerized |
| **ci** | `ci.properties` | `http://localhost:3000` | `http://localhost:8080/api` | Ephemeral nightly stack |
| **stage** | `stage.properties` | Stage frontend URL | Stage API URL | Pre-production smoke |
| **dev** | `dev.properties` | Dev frontend URL | Dev API URL | Early integration |
| **local-headed** | `local-headed.properties` | Local | Local | Debug UI (`-Pui-headed`) |

## 3. Local / Docker Stack

Typical developer setup:

| Service | Port | Notes |
|---------|------|-------|
| flowiq-frontend | 3000 | React app under test |
| flowiq-backend | 8080 | Spring Boot API |
| PostgreSQL | 5432 | `flowiq` / `flowiq123` (local.properties) |

Start via project Docker documentation: [DOCKER.md](../DOCKER.md).

### CI ephemeral stack

| Component | File | Notes |
|-----------|------|-------|
| Compose | `docker-compose.ci.yml` | Per-run project name `flowiq-ci-{run_id}` |
| Images | GHCR pre-built backend + frontend | `build-ci-images` action |
| Optional profiles | `mailhog`, `minio`, `redis` | Nightly workflow input |
| Teardown | `ci-teardown-local.sh` | Unless `CI_SHARED_STACK=true` |

## 4. Configuration Parameters

| Key | Default (local/ci) | Description |
|-----|-------------------|-------------|
| `env` | `local` | Environment name |
| `base.url` | `http://localhost:3000` | Frontend root |
| `api.url` | `http://localhost:8080/api` | API base (includes `/api`) |
| `api.timeout` | `30000` | Rest Assured timeout (ms) |
| `ui.timeout` | `30000` | Playwright default timeout (ms) |
| `browser` | `chromium` | `chromium`, `firefox`, `webkit`, `chrome` |
| `headless` | `true` | Playwright headless mode |
| `slow.mo` | `0` | Slow motion ms (headed debug: 200) |
| `test.user.email` | `demo@flowiq.ai` | Default automation user |
| `test.user.password` | `demo123` | Default automation password |
| `db.url` | `jdbc:postgresql://localhost:5432/flowiq` | Integration DB tests |
| `db.username` | `flowiq` | DB user |
| `db.password` | `flowiq123` | DB password |

### Override via environment variables

Owner maps `system:env` — use uppercase with dots replaced:

```bash
export TEST_USER_EMAIL=user@example.com
export TEST_USER_PASSWORD=secret
mvn test -Papi-smoke -Pstage
```

## 5. GitHub Actions Environments

| Workflow | Environment | Secrets |
|----------|-------------|---------|
| `api-smoke.yml` | `stage` or `dev` (input) | `TEST_USER_EMAIL`, `TEST_USER_PASSWORD` |
| `ui-smoke.yml` | `stage` or `dev` | Same |
| `nightly-regression.yml` | Ephemeral (no external env) | Inline `demo@flowiq.ai` |
| `pr-validation.yml` | Ephemeral PostgreSQL + backend JAR | `GH_PAT` optional for private backend |

Repository variables:

| Variable | Default |
|----------|---------|
| `BACKEND_REPOSITORY` | `YevheniiaDem/flowiq-backend` |
| `FRONTEND_REPOSITORY` | `YevheniiaDem/flowiq-frontend` |
| `CI_RUNNER_LABELS` | `ubuntu-latest` |
| `CI_SHARED_STACK` | `false` |

## 6. Browser Matrix

| Browser | Profile | Suite |
|---------|---------|-------|
| Chromium (default) | All UI/E2E profiles | All |
| Firefox | `-Pcross-browser-firefox` | `cross-browser-suite.xml` |
| WebKit | `-Pcross-browser-webkit` | `cross-browser-suite.xml` |
| Chrome headed | `-Pui-headed`, `-Pe2e-headed` | smoke / e2e |

## 7. Test Data in Environments

| Environment | User strategy |
|-------------|---------------|
| local/ci | Shared `demo@flowiq.ai` with demo workspace data |
| stage/dev | Secrets-configured dedicated test user |
| Registration tests | `TestDataFactory.randomRegisterRequest()` — unique per run |

See [TEST_DATA_STRATEGY.md](TEST_DATA_STRATEGY.md).

## 8. Health Checks

| Check | Endpoint / command |
|-------|-------------------|
| API ready | `GET {api.url}/health` |
| Frontend ready | HTTP 200 on `base.url` |
| CI wait script | `.github/scripts/wait-for-backend.sh` |

## 9. Artifact & Report Locations

| Artifact | Retention (nightly) |
|----------|-------------------|
| Surefire XML | 30 days |
| Allure results | 30 days |
| Playwright trace/video | On failure, 30 days |
| Docker diagnostics | 14 days |
| Allure GitHub Pages | 90-day history |

## 10. References

- [CI-CD.md](../CI-CD.md)
- [CI_INFRASTRUCTURE.md](../automation/CI_INFRASTRUCTURE.md)
- [DOCKER.md](../DOCKER.md)
