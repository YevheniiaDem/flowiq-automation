# FlowIQ Traceability Matrix

| Field | Value |
|-------|-------|
| **Document ID** | QA-TRC-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |
| **Regenerate** | `mvn verify -Prequirements-traceability` |

---

## 1. Purpose

Maps FlowIQ business features to API endpoints, TestNG suites, and concrete test classes in `flowiq-automation`.

**Legend:** ✅ = covered · — = not applicable · ⚠️ = partial / gap

---

## 2. Feature → Test Traceability

| Feature ID | Feature | API Endpoints (primary) | API Smoke | API Regression | Contract | UI Smoke | UI Regression | E2E | Security |
|------------|---------|-------------------------|:---------:|:--------------:|:--------:|:--------:|:-------------:|:---:|:--------:|
| F-01 | Authentication | `POST /auth/login`, `POST /auth/logout`, `GET /auth/me` | ✅ `AuthSmokeApiTest` | ✅ `AuthRegressionTest` | ✅ `AuthContractTest` | ✅ `LoginSmokeTest` | — | ✅ `UserLoginE2ETest` | ✅ |
| F-02 | Registration | `POST /auth/register` | ✅ `AuthSmokeApiTest` | ✅ `AuthRegressionTest` | ✅ `AuthContractTest` | ✅ `RegisterSmokeTest` | — | ✅ `RegisterOnboardingE2ETest` | — |
| F-03 | Dashboard | `GET /dashboard/*` | ✅ `DashboardSmokeApiTest` | ✅ `DashboardRegressionTest` | ✅ `DashboardContractTest` | ✅ `DashboardSmokeTest` | — | ✅ (post-login) | ✅ |
| F-04 | Imports | `GET/POST /imports/*` | ✅ `ImportsSmokeApiTest` | ✅ `ImportsRegressionTest` | ✅ `ImportsContractTest` | ✅ `ImportsSmokeTest` | — | ✅ `ImportCsvE2ETest` | ✅ |
| F-05 | Transactions | `GET/POST/PUT/DELETE /transactions/*` | ✅ `TransactionsSmokeApiTest` | ✅ `TransactionsRegressionTest` | ✅ `TransactionsContractTest` | ✅ `TransactionsSmokeTest` | — | ✅ `CreateTransactionE2ETest` | ✅ |
| F-06 | Analytics | `GET /analytics/*` | ✅ `AnalyticsSmokeApiTest` | ✅ `AnalyticsRegressionTest` | ✅ `AnalyticsContractTest` | ✅ `AnalyticsSmokeTest` | ✅ `AnalyticsRegressionTest` | ✅ `CheckAnalyticsE2ETest`, `AnalyticsOverviewE2ETest` | ✅ |
| F-07 | Forecasts | `GET /forecasts/*` | ✅ `ForecastsSmokeApiTest` | ✅ `ForecastsRegressionTest` | ✅ `ForecastsContractTest` | ✅ `ForecastsSmokeTest` | — | ✅ `CheckForecastE2ETest` | ✅ |
| F-08 | AI Accountant | `GET /ai-accountant/*`, `POST /chat` | ✅ `AIAccountantSmokeApiTest` | ✅ `AIAccountantRegressionTest` | ✅ `AIAccountantContractTest` | ✅ `AIAccountantSmokeTest` | — | — | ✅ |
| F-09 | Tasks | `GET/POST/PUT/DELETE /tasks/*` | ✅ `TasksSmokeApiTest` | ✅ `TasksRegressionTest` | ✅ `TasksContractTest` | ✅ `TasksSmokeTest` | — | ✅ `CompleteTaskE2ETest` | ✅ |
| F-10 | Notifications | `GET/PUT/DELETE /notifications/*` | ✅ `NotificationsSmokeApiTest` | ✅ `NotificationsRegressionTest` | ✅ `NotificationsContractTest` | ✅ `NotificationsSmokeTest` | — | ✅ `ReadNotificationE2ETest` | ✅ |
| F-11 | Reports | `GET/POST /reports/*` | ✅ `ReportsSmokeApiTest` | ✅ `ReportsRegressionTest` | ✅ `ReportsContractTest` | ✅ `ReportsSmokeTest` | — | ✅ `GenerateReportE2ETest` | ✅ |
| F-12 | Settings | `GET/PUT /settings/*` | ✅ `ProfileSmokeApiTest` | ✅ `ProfileRegressionTest` | — | ✅ `SettingsSmokeTest` | ✅ `SettingsRegressionTest` | ✅ `SettingsProfileE2ETest` | — |
| F-13 | Profile | `GET/PUT /profile/*` | ✅ `ProfileSmokeApiTest` | ✅ `ProfileRegressionTest` | ✅ `ProfileContractTest` | ✅ (Settings tab) | ✅ `SettingsRegressionTest` | ✅ `SettingsProfileE2ETest` | ✅ |
| F-14 | Security | Auth boundaries | — | ✅ (auth negatives) | — | ✅ (security tab) | — | ⚠️ (tab only) | ✅ `SecuritySmokeApiTest` |
| F-15 | Help Center | `GET /business-guide/*` | ✅ `BusinessGuideSmokeApiTest` | ✅ `BusinessGuideRegressionTest` | ✅ `BusinessGuideContractTest` | ✅ `BusinessGuideSmokeTest` | ✅ (help in Settings) | — | ✅ |
| F-16 | Demo Workspace | UI banner | — | — | — | ✅ `OnboardingSmokeTest` | ✅ `OnboardingRegressionTest` | — | — |
| F-17 | Onboarding | UI / localStorage | — | — | — | ✅ `OnboardingSmokeTest` | ✅ `OnboardingRegressionTest` | ✅ `RegisterOnboardingE2ETest` | — |
| F-18 | Empty States | UI `*-empty-state` | — | — | — | ✅ Analytics/Transactions | ✅ `AnalyticsRegressionTest` | — | — |
| F-19 | Activation Checklist | UI `activation-checklist` | — | — | — | ✅ `OnboardingSmokeTest` | ✅ `OnboardingRegressionTest` | — | — |
| F-20 | What's New | UI modal | — | — | — | ✅ `OnboardingSmokeTest` | — | — | — |
| F-21 | Product Tour | Driver.js / Help | — | — | — | ✅ (help entry) | ✅ `OnboardingRegressionTest` | — | — |

---

## 3. Suite → Test Class Index

### API Smoke (`-Papi-smoke`)

| Class | Groups |
|-------|--------|
| `com.flowiq.api.auth.AuthSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.dashboard.DashboardSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.profile.ProfileSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.transactions.TransactionsSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.imports.ImportsSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.reports.ReportsSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.analytics.AnalyticsSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.notifications.NotificationsSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.tasks.TasksSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.forecasts.ForecastsSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.businessguide.BusinessGuideSmokeApiTest` | `smoke`, `api` |
| `com.flowiq.api.aiaccountant.AIAccountantSmokeApiTest` | `smoke`, `api` |

### API Regression (`-Papi-regression`)

| Package | Class |
|---------|-------|
| `api.regression.auth` | `AuthRegressionTest` |
| `api.regression.transactions` | `TransactionsRegressionTest` |
| `api.regression.dashboard` | `DashboardRegressionTest` |
| `api.regression.analytics` | `AnalyticsRegressionTest` |
| `api.regression.imports` | `ImportsRegressionTest` |
| `api.regression.reports` | `ReportsRegressionTest` |
| `api.regression.notifications` | `NotificationsRegressionTest` |
| `api.regression.tasks` | `TasksRegressionTest` |
| `api.regression.forecasts` | `ForecastsRegressionTest` |
| `api.regression.businessguide` | `BusinessGuideRegressionTest` |
| `api.regression.aiaccountant` | `AIAccountantRegressionTest` |
| `api.regression.profile` | `ProfileRegressionTest` |

### Contract (`-Pcontract`)

| Class | Schema domain |
|-------|---------------|
| `AuthContractTest` | auth (login, register, me) |
| `DashboardContractTest` | dashboard |
| `ProfileContractTest` | profile |
| `TransactionsContractTest` | transactions |
| `AnalyticsContractTest` | analytics |
| `ImportsContractTest` | imports |
| `ReportsContractTest` | reports |
| `TasksContractTest` | tasks |
| `NotificationsContractTest` | notifications |
| `ForecastsContractTest` | forecasts |
| `BusinessGuideContractTest` | businessguide |
| `AIAccountantContractTest` | aiaccountant |

### UI Smoke (`-Pui-smoke`)

`com.flowiq.ui.smoke.*` — 14 classes (see [SMOKE_STRATEGY.md](SMOKE_STRATEGY.md))

### UI Regression (`-Pui-regression`)

| Class |
|-------|
| `AnalyticsRegressionTest` |
| `SettingsRegressionTest` |
| `OnboardingRegressionTest` |

### E2E (`-Pe2e`)

| Class |
|-------|
| `UserLoginE2ETest` |
| `RegisterOnboardingE2ETest` |
| `CreateTransactionE2ETest` |
| `ImportCsvE2ETest` |
| `GenerateReportE2ETest` |
| `CompleteTaskE2ETest` |
| `ReadNotificationE2ETest` |
| `CheckForecastE2ETest` |
| `CheckAnalyticsE2ETest` |
| `AnalyticsOverviewE2ETest` |
| `SettingsProfileE2ETest` |

### Security (`-Psecurity`)

| Class |
|-------|
| `SecuritySmokeApiTest` |

---

## 4. CI Pipeline Traceability

| CI Job | Maven profiles | Features validated |
|--------|----------------|-------------------|
| PR `contract-tests` | `-Pcontract` | API schema stability |
| Nightly `smoke` | `api-smoke`, `ui-smoke` | All modules reachable |
| Nightly `api-regression` | `api-regression` | Business rules |
| Nightly `ui-regression` | `ui-smoke`, `ui-regression` | UI + onboarding |
| Nightly `contract` | `contract` | Schemas on ephemeral stack |
| Nightly `security` | `security` | 401 matrix |
| `api-smoke.yml` | `-Papi-smoke` | Stage/dev API |
| `ui-smoke.yml` | `-Pui-smoke` | Stage/dev UI |

---

## 5. Broken / Missing Traceability

| Feature | Gap | Action |
|---------|-----|--------|
| Security — password change | No E2E/API test | Add `POST /profile/change-password` tests |
| AI Accountant | No E2E chat journey | Accept smoke/regression or add stable E2E |
| Integrations | No feature yet | Add row when `/integrations` ships |
| Contract POST bodies | Create endpoints not contracted | Extend schemas per [CONTRACT-COVERAGE.md](../CONTRACT-COVERAGE.md) |

---

## 6. Framework Component Traceability

| Component | Traced features |
|-----------|-----------------|
| `ProfileService` | F-12, F-13 |
| `SettingsService` | F-12 |
| `RegisterPage` | F-02 |
| `SettingsPage` | F-12, F-13, F-14, F-15 |
| `AnalyticsPage` | F-06, F-18 |
| `OnboardingPage` | F-16–F-21 |
| `OnboardingUiHelper` | F-17–F-20 (overlay control) |
| `InfrastructureRetryAnalyzer` | All suites (infra reliability) |

---

## 7. References

- [REQUIREMENTS_COVERAGE_MATRIX.md](REQUIREMENTS_COVERAGE_MATRIX.md)
- [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md)
- [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md)
- Agent output: [traceability-matrix.md](../ai-reports/traceability-matrix.md)
