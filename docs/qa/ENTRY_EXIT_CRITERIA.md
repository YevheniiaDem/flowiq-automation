# FlowIQ Entry and Exit Criteria

| Field | Value |
|-------|-------|
| **Document ID** | QA-EEC-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Defines when testing may start (entry) and when a build or release is considered acceptable (exit).

## 2. Entry Criteria

### 2.1 Pull request testing

| # | Criterion | Verification |
|---|-----------|--------------|
| E-PR-01 | Automation code compiles | `mvn clean compile test-compile` |
| E-PR-02 | Backend branch available for contract job | Checkout `flowiq-backend` with matching branch |
| E-PR-03 | PostgreSQL service healthy | GHA service container |
| E-PR-04 | Backend health endpoint reachable | `wait-for-backend.sh` → `GET /api/health` |
| E-PR-05 | No blocking infrastructure outage | CI runner available |

### 2.2 Nightly regression

| # | Criterion | Verification |
|---|-----------|--------------|
| E-NR-01 | Ephemeral stack built | `build-environment` job success |
| E-NR-02 | Docker images exported or cached | `build-ci-images` action |
| E-NR-03 | Frontend on `:3000`, backend on `:8080` | `ensure-ci-stack` |
| E-NR-04 | Test credentials available | `demo@flowiq.ai` in CI env |
| E-NR-05 | Playwright browsers installed (UI jobs) | `setup-ci-test-runner` with `install-playwright: true` |

### 2.3 Manual smoke (stage/dev)

| # | Criterion | Verification |
|---|-----------|--------------|
| E-SM-01 | `TEST_USER_EMAIL` and `TEST_USER_PASSWORD` secrets set | `validate-test-secrets` action |
| E-SM-02 | Target environment deployed and healthy | Manual / ops confirmation |
| E-SM-03 | GitHub Environment (`stage` or `dev`) configured | Workflow environment gate |

### 2.4 E2E / local testing

| # | Criterion | Verification |
|---|-----------|--------------|
| E-E2E-01 | Full stack running (Docker Compose or local dev) | `base.url` and `api.url` reachable |
| E-E2E-02 | Demo user seeded | `test.user.email` in `local.properties` |
| E-E2E-03 | Playwright browsers installed | `mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI` (if needed) |

### 2.5 Test development entry

| # | Criterion |
|---|-----------|
| E-DEV-01 | Feature merged or feature flag enabled in target env |
| E-DEV-02 | API endpoint documented or discoverable in backend |
| E-DEV-03 | `data-testid` added for new UI (per `TestIds` convention) |

## 3. Exit Criteria

### 3.1 Pull request merge

| # | Criterion | Threshold |
|---|-----------|-----------|
| X-PR-01 | Compile job | 100% pass |
| X-PR-02 | Backend unit tests | 100% pass |
| X-PR-03 | Contract tests | 100% pass (19 tests) |
| X-PR-04 | No new P1/P2 defects without waiver | QA approval |

### 3.2 Nightly regression

| # | Criterion | Threshold |
|---|-----------|-----------|
| X-NR-01 | Smoke (API + UI) | 100% pass |
| X-NR-02 | API regression | 100% pass |
| X-NR-03 | UI regression (smoke + regression) | 100% pass |
| X-NR-04 | Contract | 100% pass |
| X-NR-05 | Security | 100% pass |
| X-NR-06 | Non-flaky failures | Zero unresolved failed-only count |
| X-NR-07 | Allure report published | Warning acceptable; artifact required |

Flaky tests (`detect-flaky-tests`) do not block pipeline by default (`continue-on-error: true`) but must be triaged within 2 sprints.

### 3.3 Release to production

| # | Criterion | Reference |
|---|-----------|-----------|
| X-REL-01 | Nightly green on release commit or tag | [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) |
| X-REL-02 | E2E suite executed | `-Pe2e` pass |
| X-REL-03 | No open P1 defects | Defect tracker |
| X-REL-04 | P2 defects reviewed and accepted | Product + QA sign-off |
| X-REL-05 | Contract + API regression pass on release candidate | CI or manual |
| X-REL-06 | Release risk assessment reviewed | `-Prelease-risk-assessment` optional |
| X-REL-07 | Acceptance criteria met | [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md) |

### 3.4 Sprint / feature completion

| # | Criterion |
|---|-----------|
| X-SPR-01 | New feature has smoke coverage minimum |
| X-SPR-02 | Traceability matrix updated for new requirements |
| X-SPR-03 | Known gaps documented in coverage matrix |

## 4. Suspension Criteria

Stop testing when:

- CI stack repeatedly fails health check (> 3 consecutive nights) — escalate to platform
- Data corruption on shared demo tenant — rotate credentials / reseed
- External dependency outage (registry, GitHub Actions)

## 5. Waiver Process

1. Document failure scope and business impact.
2. QA Lead + Engineering Lead approval.
3. Create defect with target fix version.
4. Do not waive contract failures on PR without executive approval.

## 6. References

- [TEST_PLAN.md](TEST_PLAN.md)
- [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md)
- [DEFECT_WORKFLOW.md](DEFECT_WORKFLOW.md)
