# FlowIQ Test Types

| Field | Value |
|-------|-------|
| **Document ID** | QA-TYP-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Test Type Catalog

| Test type | Purpose | FlowIQ implementation | CI trigger |
|-----------|---------|----------------------|------------|
| **Smoke** | Fast confidence that build is testable | API + UI smoke suites | Nightly, on-demand workflows |
| **Regression** | Detect functional regressions | API regression (~269), UI regression (3 classes) | Nightly |
| **Contract** | API schema stability | JSON Schema vs live responses | PR + nightly |
| **Functional (API)** | Business rules, CRUD, negative paths | `com.flowiq.api.regression.*` | Nightly |
| **Functional (UI)** | Pages, tabs, modals, empty states | `com.flowiq.ui.*` | Nightly |
| **E2E** | Critical user journeys | `com.flowiq.e2e.*` | Manual pre-release |
| **Security** | Auth boundaries, 401 matrix | `SecuritySmokeApiTest` | Nightly |
| **Integration** | DB + API together | `*IntegrationTest`, `*IntegrationDbTest` | On demand |
| **Unit** | Isolated logic | Backend + framework unit tests | PR |
| **Cross-browser** | Browser compatibility | `cross-browser-suite.xml` | Manual / weekly |
| **Exploratory** | New features, UX | Manual (not automated) | Sprint |

## 2. Smoke Tests

**Definition:** Minimal tests proving each module is reachable and core paths work.

| Layer | Classes | Duration (est.) |
|-------|---------|---------------|
| API | 12 (`AuthSmokeApiTest` … `AIAccountantSmokeApiTest`) | 4–6 min |
| UI | 14 (`LoginSmokeTest` … `OnboardingSmokeTest`) | 8–12 min |

Details: [SMOKE_STRATEGY.md](SMOKE_STRATEGY.md).

## 3. Regression Tests

**Definition:** Broad functional coverage including negatives, pagination, isolation, and business rules.

| Layer | Scope | Duration (est.) |
|-------|-------|-----------------|
| API | 12 modules, data providers | 12–18 min |
| UI | Analytics, Settings, Onboarding | 6–10 min |

Details: [REGRESSION_STRATEGY.md](REGRESSION_STRATEGY.md).

## 4. Contract Tests

**Definition:** Response body matches versioned JSON Schema; status codes and content-type validated.

| Domain | Tests | Schema path |
|--------|-------|-------------|
| Auth | login, register, me | `schemas/auth/` |
| Dashboard | stats | `schemas/dashboard/` |
| Profile | profile response | `schemas/profile/` |
| Transactions | page, summary | `schemas/transactions/` |
| Analytics | overview, FOP insights | `schemas/analytics/` |
| Imports | list | `schemas/imports/` |
| Reports | list, preview | `schemas/reports/` |
| Tasks | page, grouped | `schemas/tasks/` |
| Notifications | page, summary | `schemas/notifications/` |
| Forecasts | summary | `schemas/forecasts/` |
| Business Guide | articles page | `schemas/businessguide/` |
| AI Accountant | health | `schemas/aiaccountant/` |

Base class: `BaseContractTest`. Inventory: [CONTRACT-COVERAGE.md](../CONTRACT-COVERAGE.md).

## 5. End-to-End Tests

| Class | Journey |
|-------|---------|
| `UserLoginE2ETest` | Login → dashboard |
| `RegisterOnboardingE2ETest` | Register → onboarding |
| `CreateTransactionE2ETest` | Create income/expense |
| `ImportCsvE2ETest` | Upload CSV import |
| `GenerateReportE2ETest` | Generate and download report |
| `CompleteTaskE2ETest` | Complete task |
| `ReadNotificationE2ETest` | Mark notification read |
| `CheckForecastE2ETest` | View forecast summary |
| `CheckAnalyticsE2ETest` | Analytics navigation |
| `AnalyticsOverviewE2ETest` | Analytics overview content |
| `SettingsProfileE2ETest` | Settings tabs profile/security |

## 6. Security Tests

`SecuritySmokeApiTest` validates protected endpoints return 401 without token:

- Dashboard, transactions, profile, imports, reports, notifications, tasks, analytics, forecasts, AI accountant, business guide.

Auth regression adds invalid credentials, duplicate registration, session isolation.

## 7. Negative & Edge Case Testing

| Area | Examples | Location |
|------|----------|----------|
| Auth | Invalid email/password, duplicate register | `AuthRegressionTest` |
| Transactions | Invalid ID, empty payload, user isolation | `TransactionsRegressionTest` |
| Imports | Empty file, invalid format | `ImportsRegressionTest` |
| Reports | Invalid report type/format | `ReportsRegressionTest` |
| UI empty states | Analytics with no data | `AnalyticsRegressionTest` |

## 8. Non-Functional Types (Planned / Partial)

| Type | Status |
|------|--------|
| Performance | Timeouts only; no dedicated load suite |
| Accessibility | Not automated |
| Compatibility | Cross-browser profiles available |
| Reliability | Flaky detection nightly |

## 9. TestNG Groups Reference

| Group | Used by |
|-------|---------|
| `smoke`, `api` | API smoke |
| `ui-smoke` | UI smoke |
| `api-regression` | API regression |
| `ui-regression` | UI regression |
| `contract` | Contract |
| `e2e` | E2E |
| `security` | Security |
| `unit` | Unit |

## 10. References

- [TEST_LEVELS.md](TEST_LEVELS.md)
- [API-REGRESSION-COVERAGE.md](../API-REGRESSION-COVERAGE.md)
