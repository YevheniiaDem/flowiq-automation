# FlowIQ Regression Strategy

| Field | Value |
|-------|-------|
| **Document ID** | QA-REG-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Objective

Detect functional regressions in FlowIQ API and UI after code changes while balancing execution time and signal quality.

## 2. Regression Suites

| Suite | Profile | TestNG suite | Parallelism | Est. duration |
|-------|---------|--------------|-------------|---------------|
| API regression | `-Papi-regression` | `regression-api-suite.xml` | 4 methods | 12–18 min |
| UI regression | `-Pui-regression` | `ui-regression-suite.xml` | 3 methods | 6–10 min |
| Combined | `-Pregression` | `regression-suite.xml` | Per child | 20–30 min |

**Note:** Nightly `ui-regression` job runs **both** `ui-smoke` and `ui-regression` for broader UI signal.

## 3. API Regression Modules

| Module | Test class | ~Executions | Focus |
|--------|------------|-------------|-------|
| Auth | `AuthRegressionTest` | 28 | Login, register, me, logout, negatives |
| Transactions | `TransactionsRegressionTest` | 39 | CRUD, search, filter, pagination, isolation |
| Dashboard | `DashboardRegressionTest` | 15 | Stats, charts, health, unauthorized |
| Analytics | `AnalyticsRegressionTest` | 15 | Overview, trends, FOP |
| Imports | `ImportsRegressionTest` | 14 | Upload, list, invalid files |
| Reports | `ReportsRegressionTest` | 37 | Generate 18 combos, preview, download |
| Notifications | `NotificationsRegressionTest` | 25 | Read state, summary, delete |
| Tasks | `TasksRegressionTest` | 35 | CRUD, grouped, today/upcoming |
| Forecasts | `ForecastsRegressionTest` | 19 | Revenue, expense, tax, FOP limits |
| Business Guide | `BusinessGuideRegressionTest` | 28 | Articles, search, categories |
| AI Accountant | `AIAccountantRegressionTest` | 20 | Health, chat, recommendations |
| Profile | `ProfileRegressionTest` | — | GET/PUT profile, settings |

**Total:** ~269 executions across 12 classes.

Architecture:

- Base: `BaseRegressionApiTest`
- Assertions: `RegressionAssertions`
- Data: `RegressionDataProviders`
- Services only — no Rest Assured in test bodies

Full inventory: [API-REGRESSION-COVERAGE.md](../API-REGRESSION-COVERAGE.md).

## 4. UI Regression Modules

| Class | Focus |
|-------|-------|
| `AnalyticsRegressionTest` | Tab navigation, empty state vs data state |
| `SettingsRegressionTest` | Tab persistence after reload, profile/security tabs |
| `OnboardingRegressionTest` | Activation checklist, demo banner, product tour entry |

## 5. When Regression Runs

| Trigger | Suites |
|---------|--------|
| Nightly cron (03:00 UTC) | API + UI regression (parallel jobs) |
| Manual `workflow_dispatch` | Select `api`, `ui`, or `full` |
| Pre-release | API + UI + E2E recommended |
| Post-hotfix | Affected module + smoke |

## 6. Selection Strategy

### 6.1 Full regression (default nightly)

All API regression modules + UI smoke + UI regression when `SELECTED_SUITE=full`.

### 6.2 Partial regression (risk-based)

```bash
mvn verify -Prisk-based-regression
```

Agent output recommends:

| Recommendation | When |
|----------------|------|
| `FULL_REGRESSION` | High-risk release, many API changes |
| `PARTIAL_REGRESSION` | Scoped feature change |
| `SMOKE_ONLY` | Low-risk config/docs change |

### 6.3 Module-targeted (developer)

```bash
mvn test -Papi-regression -Plocal -Dtest=ReportsRegressionTest
mvn test -Pui-regression -Plocal -Dtest=SettingsRegressionTest
```

## 7. Parallel Execution & Isolation

| Layer | Mechanism |
|-------|-----------|
| API | Stateless HTTP; shared demo user; unique data where needed |
| UI | ThreadLocal `Playwright` sessions per TestNG thread |
| Retries | Infra only via `InfrastructureRetryTransformer` |

## 8. Failure Handling

| Failure type | Action |
|--------------|--------|
| Single module failure | File defect; re-run module locally |
| Widespread API 5xx | Infra incident; check stack diagnostics |
| Flaky | `detect-flaky-tests`; do not mute without fix plan |
| Contract + regression mismatch | Schema or API bug — fix backend or schema |

## 9. Maintenance

| Activity | Owner | Frequency |
|----------|-------|-----------|
| Add regression for new endpoint | QA + Dev | Per feature |
| Update data providers | QA | When API pagination/filter changes |
| Prune obsolete tests | QA | Quarterly (`-Ptest-maintenance`) |
| Review duration | QA Lead | Monthly |

## 10. References

- [SMOKE_STRATEGY.md](SMOKE_STRATEGY.md)
- [RISK_ANALYSIS.md](RISK_ANALYSIS.md)
- [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md)
