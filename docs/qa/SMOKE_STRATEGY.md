# FlowIQ Smoke Strategy

| Field | Value |
|-------|-------|
| **Document ID** | QA-SMK-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Smoke tests provide rapid confidence that each FlowIQ module is deployable and reachable. They gate nightly pipelines and support on-demand validation against `stage` and `dev`.

## 2. Smoke Philosophy

| Principle | Implementation |
|-----------|----------------|
| **Fast** | API ~4–6 min, UI ~8–12 min (parallel) |
| **Shallow** | Happy path + page load; deep rules in regression |
| **Broad** | Every in-scope module has at least one smoke test |
| **Stable** | Prefer `data-testid`; dismiss onboarding overlays by default |
| **CI-friendly** | Runnable against ephemeral stack without external secrets |

## 3. Suite Structure

| Suite | Profile | Suite XML | Threads |
|-------|---------|-----------|---------|
| API smoke | `-Papi-smoke` | `api-smoke-suite.xml` | 4 |
| UI smoke | `-Pui-smoke` | `ui-smoke-suite.xml` | 3 |
| Combined | `-Psmoke` | `smoke-suite.xml` | Sequential |

TestNG groups: `smoke` + `api` (API); `ui-smoke` (UI).

## 4. API Smoke Inventory

| Class | Module | Typical checks |
|-------|--------|----------------|
| `AuthSmokeApiTest` | Auth | Login, register, me |
| `DashboardSmokeApiTest` | Dashboard | Stats, health |
| `ProfileSmokeApiTest` | Profile / Settings | GET profile, sessions |
| `TransactionsSmokeApiTest` | Transactions | List, summary |
| `ImportsSmokeApiTest` | Imports | List jobs |
| `ReportsSmokeApiTest` | Reports | List, preview |
| `AnalyticsSmokeApiTest` | Analytics | Overview |
| `NotificationsSmokeApiTest` | Notifications | List, summary |
| `TasksSmokeApiTest` | Tasks | List |
| `ForecastsSmokeApiTest` | Forecasts | Summary |
| `BusinessGuideSmokeApiTest` | Help Center | Articles list |
| `AIAccountantSmokeApiTest` | AI Accountant | Health |

## 5. UI Smoke Inventory

| Class | Module |
|-------|--------|
| `LoginSmokeTest` | Authentication |
| `RegisterSmokeTest` | Registration |
| `DashboardSmokeTest` | Dashboard |
| `TransactionsSmokeTest` | Transactions |
| `ImportsSmokeTest` | Imports |
| `ReportsSmokeTest` | Reports |
| `AnalyticsSmokeTest` | Analytics |
| `ForecastsSmokeTest` | Forecasts |
| `AIAccountantSmokeTest` | AI Accountant |
| `TasksSmokeTest` | Tasks |
| `NotificationsSmokeTest` | Notifications |
| `BusinessGuideSmokeTest` | Help Center |
| `SettingsSmokeTest` | Settings / Profile / Security |
| `OnboardingSmokeTest` | Onboarding, What's New, Activation |

Package: `com.flowiq.ui.smoke`.

## 6. CI Integration

| Pipeline | Smoke execution |
|----------|-----------------|
| Nightly `smoke` job | `api-smoke` then `ui-smoke` on ephemeral stack |
| `api-smoke.yml` | `mvn test -Papi-smoke -Denv={stage\|dev}` |
| `ui-smoke.yml` | `mvn test -Pui-smoke -Denv={stage\|dev}` |
| PR validation | **No** full smoke — contract only for speed |

Credentials:

- CI ephemeral: `demo@flowiq.ai` / `demo123`
- Stage/dev: `TEST_USER_EMAIL`, `TEST_USER_PASSWORD` secrets

## 7. Pass Criteria

| Criterion | Threshold |
|-----------|-----------|
| All smoke tests pass | 100% |
| No infra-only retry exhaustion | Max 2 retries per test |
| Artifacts uploaded | Surefire + Allure (+ Playwright on UI failure) |

## 8. Failure Response

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| All API smoke fail | Backend down | Check `/api/health`, compose logs |
| All UI smoke fail | Frontend down | Check `base.url` |
| Single module fail | Feature regression | Run module regression; file defect |
| Intermittent UI | Overlay / timing | Check flaky report; onboarding helper |

## 9. Smoke vs Regression

| Aspect | Smoke | Regression |
|--------|-------|------------|
| Depth | 1–3 tests per module | 14–39 per API module |
| Negatives | Minimal | Extensive |
| Duration | Minutes | Tens of minutes |
| PR gate | No | Contract only |
| Nightly | Yes | Yes |

## 10. Local Commands

```bash
# API smoke (local stack)
mvn test -Papi-smoke -Plocal

# UI smoke
mvn test -Pui-smoke -Plocal

# Headed debug
mvn test -Pui-headed -Plocal

# Stage (requires secrets)
mvn test -Papi-smoke -Pstage
```

## 11. References

- [REGRESSION_STRATEGY.md](REGRESSION_STRATEGY.md)
- [UI-SMOKE-STABILITY.md](../UI-SMOKE-STABILITY.md)
- [CI-CD.md](../CI-CD.md)
