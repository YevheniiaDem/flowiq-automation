# FlowIQ Test Strategy

| Field | Value |
|-------|-------|
| **Document ID** | QA-STR-001 |
| **Version** | 1.0 |
| **Status** | Approved |
| **Product** | FlowIQ (SaaS financial management platform) |
| **Repository** | `flowiq-automation` |
| **Last updated** | 2026-06-28 |
| **Owner** | QA Engineering |
| **Related repos** | `flowiq-backend`, `flowiq-frontend` |

---

## 1. Purpose

This document defines the overall quality strategy for FlowIQ. It establishes how testing supports product delivery across API, UI, contract, security, and end-to-end layers, and how automation integrates with CI/CD in `flowiq-automation`.

## 2. Quality Objectives

| Objective | Target | Measurement |
|-----------|--------|-------------|
| Critical user flows automated | 100% of in-scope flows | [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md) |
| PR quality gate | Compile + contract pass | `.github/workflows/pr-validation.yml` |
| Nightly regression | Full ephemeral stack | `.github/workflows/nightly-regression.yml` |
| Contract stability | JSON Schema validation on live API | `-Pcontract` (19 tests) |
| Flaky test visibility | Classify infra vs business failures | `detect-flaky-tests` job, `FlakyTestInvestigator` |
| Release confidence | Smoke green + no P1 open defects | [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) |

## 3. Strategic Principles

1. **Automation first** — Repeatable checks run in CI; manual testing focuses on exploratory and new-feature validation.
2. **Test the system as deployed** — API, contract, smoke, and regression suites execute against live backend and frontend (local Docker, ephemeral CI stack, or `stage`/`dev`).
3. **Layered defense** — Unit tests in `flowiq-backend`; contract tests on PR; smoke on demand; full regression nightly.
4. **Fast feedback** — PR validation completes compile + contract in minutes; smoke suites parallelized (API: 4 threads, UI: 3 threads).
5. **No false confidence from retries** — `InfrastructureRetryAnalyzer` retries only connection, timeout, and 5xx failures (max 2 attempts). Assertion failures are never retried.
6. **Traceability** — Features map to TestNG groups, suite XML, and Allure reports. Regenerate via `-Prequirements-traceability`.

## 4. Test Pyramid (FlowIQ Implementation)

```
                    ┌─────────────┐
                    │  E2E (11)   │  Playwright, sequential, critical journeys
                    ├─────────────┤
                    │ UI Smoke/   │  14 smoke + 3 regression classes
                    │ Regression  │
                    ├─────────────┤
                    │ API Smoke   │  12 modules, parallel methods
                    │ + Regression│  12 modules, ~269 executions
                    ├─────────────┤
                    │ Contract    │  19 JSON Schema validations
                    ├─────────────┤
                    │ Unit        │  flowiq-backend + framework unit tests
                    └─────────────┘
```

## 5. Scope of Automation

All 21 user-facing flows are covered by at least one automated layer:

Authentication, Registration, Dashboard, Imports, Transactions, Analytics, Forecasts, AI Accountant, Tasks, Notifications, Reports, Settings, Profile, Security, Help Center (Business Guide), Demo Workspace, Onboarding, Empty States, Activation Checklist, What's New, Product Tour.

See [TEST_SCOPE.md](TEST_SCOPE.md) and [REQUIREMENTS_COVERAGE_MATRIX.md](REQUIREMENTS_COVERAGE_MATRIX.md).

## 6. Environments & Execution Model

| Environment | Profile | Use case |
|-------------|---------|----------|
| Local Docker | `-Plocal` / `-Pdocker` | Developer validation |
| Ephemeral CI | `-Pci` | Nightly regression (`docker-compose.ci.yml`) |
| Stage | `-Pstage` | Manual smoke (`api-smoke.yml`, `ui-smoke.yml`) |
| Dev | `-Pdev` | Early integration smoke |

Configuration: `EnvironmentConfig` + `src/main/resources/environments/*.properties`.

## 7. Tooling Stack

| Layer | Technology |
|-------|------------|
| Test runner | TestNG 7.10 |
| API | Rest Assured 5.5, service layer (`com.flowiq.services.*`) |
| UI / E2E | Playwright 1.49 (Chromium default; Firefox/WebKit cross-browser) |
| Assertions | AssertJ, custom `RegressionAssertions`, `UiAssertions` |
| Reporting | Allure 2.29 (GitHub Pages on nightly) |
| Data | DataFaker, `TestDataFactory`, `RandomDataGenerator` |
| Schema | JSON Schema via Rest Assured `json-schema-validator` |
| CI | GitHub Actions, Docker Compose ephemeral stack |
| Quality agents | Test gap, flaky, traceability, release risk (`com.flowiq.agents.*`) |

## 8. Roles & Responsibilities

| Role | Responsibility |
|------|----------------|
| QA Engineering | Suite design, page objects, regression maintenance, coverage reports |
| Developers | Unit tests (backend), fix failures from PR contract gate |
| DevOps / Platform | CI runners, secrets, ephemeral stack, artifact retention |
| Product | Acceptance criteria, release sign-off |

## 9. Risk-Based Prioritization

High-risk areas receive deeper regression and E2E coverage:

- **Transactions** — CRUD, isolation, pagination (largest regression module)
- **Auth / Security** — Session, 401 matrix, registration
- **Reports** — 18 generate format combinations
- **AI Accountant** — Monitored for flakiness; smoke only for chat

Risk analysis: [RISK_ANALYSIS.md](RISK_ANALYSIS.md). Regression selection: `-Prisk-based-regression`.

## 10. Continuous Improvement

| Activity | Frequency | Mechanism |
|----------|-----------|-----------|
| Flaky test detection | Nightly | `detect-flaky-tests` job |
| Coverage gap analysis | Per release / on demand | `-Ptest-gap-analysis` |
| API change detection | PR / release | `-Papi-change-detection` |
| Traceability refresh | After major feature add | `-Prequirements-traceability` |
| Documentation sync | Per sprint | Update `docs/qa/` and `docs/automation/` |

## 11. References

- [TEST_PLAN.md](TEST_PLAN.md)
- [AUTOMATION_STRATEGY.md](AUTOMATION_STRATEGY.md)
- [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md)
- [CI-CD.md](../CI-CD.md)
- [API-REGRESSION-COVERAGE.md](../API-REGRESSION-COVERAGE.md)
- [CONTRACT-COVERAGE.md](../CONTRACT-COVERAGE.md)
