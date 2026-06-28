# FlowIQ Test Plan

| Field | Value |
|-------|-------|
| **Document ID** | QA-PLN-001 |
| **Version** | 1.0 |
| **Status** | Active |
| **Last updated** | 2026-06-28 |
| **Owner** | QA Engineering |

---

## 1. Introduction

This test plan describes the activities, schedule, resources, and deliverables for validating FlowIQ across `flowiq-automation`, integrated with `flowiq-backend` and `flowiq-frontend`.

## 2. Test Items

| Component | Repository | Test artifact location |
|-----------|------------|------------------------|
| REST API | flowiq-backend | `com.flowiq.api.*`, `com.flowiq.contracts.*` |
| Web UI | flowiq-frontend | `com.flowiq.ui.*`, `com.flowiq.e2e.*` |
| Automation framework | flowiq-automation | `com.flowiq.pages.*`, `com.flowiq.services.*` |
| JSON contracts | flowiq-automation | `src/test/resources/schemas/` |

## 3. Features to Be Tested

| ID | Feature | Primary suites |
|----|---------|----------------|
| F-01 | Authentication | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-02 | Registration | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-03 | Dashboard | api-smoke, api-regression, contract, ui-smoke |
| F-04 | Imports | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-05 | Transactions | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-06 | Analytics | api-smoke, api-regression, contract, ui-smoke, ui-regression, e2e |
| F-07 | Forecasts | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-08 | AI Accountant | api-smoke, api-regression, contract, ui-smoke |
| F-09 | Tasks | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-10 | Notifications | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-11 | Reports | api-smoke, api-regression, contract, ui-smoke, e2e |
| F-12 | Settings | api-smoke, api-regression, ui-smoke, ui-regression, e2e |
| F-13 | Profile | api-smoke, api-regression, contract, ui-smoke, ui-regression, e2e |
| F-14 | Security | security, api-regression (auth isolation), ui-smoke (settings tab) |
| F-15 | Help Center / Business Guide | api-smoke, api-regression, contract, ui-smoke |
| F-16 | Demo Workspace | ui-smoke, ui-regression (onboarding) |
| F-17 | Onboarding | ui-smoke, ui-regression, e2e |
| F-18 | Empty States | ui-smoke, ui-regression |
| F-19 | Activation Checklist | ui-smoke, ui-regression |
| F-20 | What's New | ui-smoke |
| F-21 | Product Tour | ui-smoke, ui-regression |

## 4. Features Not to Be Tested (this plan)

| Item | Reason |
|------|--------|
| Third-party payment gateways | Out of scope; mocked or not integrated |
| Mobile native apps | Web-only automation scope |
| Performance / load testing | Separate performance plan (not in current repo) |
| Penetration testing | Covered by dedicated security assessment process |

## 5. Test Approach by Phase

### 5.1 Development (per PR)

| Activity | Command / workflow | Owner |
|----------|-------------------|-------|
| Compile automation | `mvn clean compile test-compile` | CI `pr-validation` |
| Backend unit tests | `mvn test -Dtest=com.flowiq.unit.**` | CI (backend checkout) |
| Contract tests | `mvn test -Pcontract -Denv=local` | CI against ephemeral PostgreSQL + backend JAR |

### 5.2 Integration (nightly)

| Job | Maven profiles | Parallelism |
|-----|----------------|-------------|
| Smoke | `api-smoke`, `ui-smoke` | API 4 threads, UI 3 threads |
| API regression | `api-regression` | 4 threads |
| UI regression | `ui-smoke`, `ui-regression` | 3 threads |
| Contract | `contract` | 3 threads |
| Security | `security` | Sequential |

Workflow: `.github/workflows/nightly-regression.yml` (cron `0 3 * * *`).

### 5.3 Pre-release (manual + automated)

| Activity | Profile |
|----------|---------|
| Full E2E | `-Pe2e -Plocal` or `-Pe2e -Pci` |
| Cross-browser spot check | `-Pcross-browser-firefox`, `-Pcross-browser-webkit` |
| Stage smoke | `mvn test -Papi-smoke -Pstage` (workflow_dispatch) |
| Release checklist | [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) |

## 6. Test Deliverables

| Deliverable | Location |
|-------------|----------|
| Surefire reports | `target/surefire-reports/` |
| Allure results | `target/allure-results/` |
| Nightly Allure (GitHub Pages) | Published by `publish-allure-report` job |
| Flaky report | `docs/ai-reports/flaky-tests-report.md` (agent output) |
| CI summary | GitHub Actions job summary |
| QA documentation | `docs/qa/` |
| Coverage audit | `docs/automation/AUTOMATION_COVERAGE_REPORT.md` |

## 7. Schedule

| Event | Timing | Suites |
|-------|--------|--------|
| PR validation | Every PR / push to `main`, `develop` | compile, unit, contract |
| Nightly regression | Daily 03:00 UTC | full (smoke, api, ui, contract, security) |
| On-demand API smoke | Manual | `api-smoke.yml` → `stage` or `dev` |
| On-demand UI smoke | Manual | `ui-smoke.yml` → `stage` or `dev` |
| Pre-release E2E | Before production deploy | `-Pe2e` |

## 8. Resources

| Resource | Specification |
|----------|---------------|
| JDK | 17 |
| Maven | 3.9+ |
| Browsers | Chromium (default), Firefox, WebKit via Playwright |
| CI runner | `ubuntu-latest` (configurable via `CI_RUNNER_LABELS`) |
| Test user (CI/local) | `demo@flowiq.ai` / env `TEST_USER_*` secrets for stage/dev |
| Database (contract PR) | PostgreSQL 15 service container |

## 9. Entry / Exit Criteria

See [ENTRY_EXIT_CRITERIA.md](ENTRY_EXIT_CRITERIA.md).

## 10. Suspension & Resumption

Testing is suspended when:

- Ephemeral CI stack fails health check (`GET /api/health`)
- Required secrets (`TEST_USER_EMAIL`, `TEST_USER_PASSWORD`) are missing for stage/dev runs
- P1 production incident blocks shared environments

Resume when stack is healthy, secrets are configured, and incident is mitigated.

## 11. Approvals

| Role | Name | Date |
|------|------|------|
| QA Lead | _TBD_ | |
| Engineering Lead | _TBD_ | |
| Product Owner | _TBD_ | |

## 12. References

- [TEST_STRATEGY.md](TEST_STRATEGY.md)
- [TEST_SCOPE.md](TEST_SCOPE.md)
- [REGRESSION_STRATEGY.md](REGRESSION_STRATEGY.md)
- [SMOKE_STRATEGY.md](SMOKE_STRATEGY.md)
