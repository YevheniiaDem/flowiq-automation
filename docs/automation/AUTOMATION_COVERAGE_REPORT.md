# FlowIQ Automation Coverage Report

**Project:** `flowiq-automation`  
**Audit date:** 2026-06-28  
**Scope:** All user-facing FlowIQ flows — API, UI, E2E, contract, security, smoke, regression

---

## 1. Executive Summary

| Metric | Before audit | After implementation |
|--------|--------------|----------------------|
| User flows with any automation | 14 / 21 | **21 / 21** |
| API smoke modules | 10 | **12** (+ Dashboard, Profile/Settings) |
| API regression modules | 11 | **12** (+ Profile/Settings) |
| Contract domains | 9 | **12** (+ Dashboard, Profile, Imports list) |
| UI smoke test classes | 11 | **16** |
| UI regression classes | 0 | **3** |
| E2E flow classes | 8 | **11** |
| Security suite | Placeholder | **Implemented** (`-Psecurity`) |
| Cross-browser profiles | None | **firefox / webkit** |
| Parallel execution | Sequential | **API/UI parallel methods** |
| Infra-only retries | API layer only | **TestNG + API (no business reruns)** |

---

## 2. Feature Coverage Matrix

| User flow | API Smoke | API Regression | Contract | UI Smoke | UI Regression | E2E | Notes |
|-----------|-----------|----------------|----------|----------|---------------|-----|-------|
| **Authentication** | ✅ | ✅ | ✅ login | ✅ LoginSmokeTest | — | ✅ UserLoginE2ETest | Login/logout/me |
| **Registration** | ✅ | ✅ | ✅ register | ✅ RegisterSmokeTest | — | ✅ RegisterOnboardingE2ETest | API + UI register |
| **Dashboard** | ✅ **new** | ✅ | ✅ **new** | ✅ DashboardSmokeTest | — | ✅ (post-login) | Added API smoke + contract |
| **Imports** | ✅ | ✅ | ✅ **new** | ✅ ImportsSmokeTest | — | ✅ ImportCsvE2ETest | Imports list contract |
| **Transactions** | ✅ | ✅ | ✅ | ✅ TransactionsSmokeTest | — | ✅ CreateTransactionE2ETest | Strongest module |
| **Analytics** | ✅ | ✅ | ✅ | ✅ **AnalyticsSmokeTest** | ✅ **new** | ✅ **AnalyticsOverviewE2ETest** | UI page object added |
| **Forecasts** | ✅ | ✅ | ✅ | ✅ ForecastsSmokeTest | — | ✅ CheckForecastE2ETest | |
| **AI Accountant** | ✅ | ✅ | ✅ health | ✅ AIAccountantSmokeTest | — | — | Chat flaky-prone |
| **Tasks** | ✅ | ✅ | ✅ | ✅ TasksSmokeTest | — | ✅ CompleteTaskE2ETest | |
| **Notifications** | ✅ | ✅ | ✅ | ✅ NotificationsSmokeTest | — | ✅ ReadNotificationE2ETest | |
| **Reports** | ✅ | ✅ | ✅ | ✅ ReportsSmokeTest | — | ✅ GenerateReportE2ETest | |
| **Settings** | ✅ **new** | ✅ **new** | — | ✅ **SettingsSmokeTest** | ✅ **new** | ✅ **SettingsProfileE2ETest** | Tabs + Help center |
| **Profile** | ✅ **new** | ✅ **new** | ✅ **new** | ✅ (Settings tab) | ✅ **new** | ✅ **SettingsProfileE2ETest** | `/api/profile` |
| **Security** | ✅ **SecuritySmokeApiTest** | ✅ (auth isolation) | — | ✅ (Settings security tab) | — | ✅ (password tab) | 401 matrix |
| **Help Center** | ✅ BusinessGuide | ✅ | ✅ | ✅ BusinessGuideSmokeTest | ✅ Help center guides | — | Mapped to Business Guide |
| **Demo Workspace** | — | — | — | ✅ **Onboarding/Settings** | ✅ **new** | — | `demo-workspace-banner` |
| **Onboarding** | — | — | — | ✅ **OnboardingSmokeTest** | ✅ **new** | ✅ Register flow | Welcome/tour hooks |
| **Empty States** | — | — | — | ✅ Analytics/Transactions | ✅ Analytics | — | `*-empty-state` testIds |
| **Activation Checklist** | — | — | — | ✅ **OnboardingSmokeTest** | ✅ **new** | — | `activation-checklist` |
| **What's New** | — | — | — | ✅ **OnboardingSmokeTest** | — | — | Modal dismiss test |
| **Product Tour** | — | — | — | ✅ Help center entry | ✅ **new** | — | Driver.js tour via Help |

**Legend:** ✅ covered · — not applicable or covered indirectly

---

## 3. Test Suite Architecture

| Suite | Maven profile | TestNG suite | Parallelism | Est. duration (local/docker) |
|-------|---------------|--------------|-------------|------------------------------|
| **Smoke (all)** | `-Psmoke` | `smoke-suite.xml` | Sequential | ~25–35 min |
| **API smoke** | `-Papi-smoke` | `api-smoke-suite.xml` | **4 threads** | ~4–6 min |
| **UI smoke** | `-Pui-smoke` | `ui-smoke-suite.xml` | **3 threads** | ~8–12 min |
| **API regression** | `-Papi-regression` | `regression-api-suite.xml` | **4 threads** | ~12–18 min |
| **UI regression** | `-Pui-regression` | `ui-regression-suite.xml` | **3 threads** | ~6–10 min |
| **Contract** | `-Pcontract` | `contract-suite.xml` | **3 threads** | ~3–5 min |
| **Security** | `-Psecurity` | `security-suite.xml` | Sequential | ~1–2 min |
| **E2E** | `-Pe2e` | `e2e-suite.xml` | Sequential | ~10–15 min |
| **Cross-browser** | `-Pcross-browser-firefox` / `-Pcross-browser-webkit` | `cross-browser-suite.xml` | Sequential | ~10–14 min each |

### Nightly CI mapping

| Job | Profiles executed |
|-----|-------------------|
| Smoke | `api-smoke` + `ui-smoke` |
| API Regression | `api-regression` |
| UI Regression | `ui-smoke` + `ui-regression` |
| Contract | `contract` |
| Security | `security` |

---

## 4. New Framework Components

| Component | Purpose |
|-----------|---------|
| `ProfileService` / `SettingsService` | API coverage for profile & notification settings |
| `RegisterPage`, `AnalyticsPage`, `SettingsPage`, `OnboardingPage` | UI page objects |
| `OnboardingUiHelper` | localStorage control for onboarding overlays |
| `InfrastructureRetryAnalyzer` | Retries **only** connection/timeout/5xx failures |
| `InfrastructureRetryTransformer` | Applies infra retry globally via TestNG |
| `BaseOnboardingUiSmokeTest` | Preserves onboarding state for activation tests |

---

## 5. Missing Scenarios (remaining gaps)

| Priority | Gap | Recommendation |
|----------|-----|----------------|
| P1 | **Password change E2E** | Add API test for `POST /profile/change-password` with session revoke |
| P1 | **Avatar upload** | API multipart test + UI upload in Profile tab |
| P2 | **Full Product Tour driver steps** | Assert each Driver.js step selector (fragile across locales) |
| P2 | **Integrations page** | Add when backend integrations ship (`/integrations`) |
| P2 | **Notification preferences mutation** | PUT `/settings/notifications` contract + UI toggle test |
| P3 | **Cross-browser in nightly matrix** | Add optional GHA matrix job for firefox/webkit |
| P3 | **Empty state with fresh tenant** | Dedicated test user with zero transactions per suite |

---

## 6. Flaky Risk Assessment

| Area | Risk | Mitigation |
|------|------|------------|
| **AI Accountant chat** | High | Historical flaky tracking; traces on failure; no video upload for passes |
| **Onboarding modals** | Medium | `OnboardingUiHelper.dismissOverlays()` in default UI auth; isolated base for onboarding tests |
| **Registration UI** | Medium | Unique email via `TestDataFactory.randomRegisterRequest()` |
| **Analytics empty vs data** | Low | Assert content **or** empty state — both valid |
| **Parallel UI smoke** | Medium | Isolated Playwright sessions per TestNG thread (ThreadLocal) |
| **Demo user profile mutation** | Low | Profile update test restores demo data; prefer random register for destructive tests |
| **Infrastructure retries** | Low | Max 2 retries; assertion failures never retried |

---

## 7. Execution Time Estimates (full nightly, parallel jobs)

| Job | Estimated wall time |
|-----|---------------------|
| Build environment | 8–15 min |
| Smoke (API + UI) | 10–18 min |
| API regression | 12–20 min |
| UI regression (smoke + regression) | 15–25 min |
| Contract | 3–6 min |
| Security | 1–3 min |
| Flaky detection + Allure publish | 5–10 min |
| **Total wall (parallel)** | **~25–40 min** |

Sequential local full run (`-Psmoke`, `-Papi-regression`, `-Pui-regression`, `-Pcontract`, `-Pe2e`): **~60–90 min**

---

## 8. Recommendations

1. **Run cross-browser weekly** — `mvn test -Pcross-browser-firefox -Plocal` and `-Pcross-browser-webkit`.
2. **Extend contract coverage** — add `/auth/me`, dashboard health, FOP profile schemas when OpenAPI stabilizes.
3. **Add password-change regression** — highest-risk security flow still untested end-to-end.
4. **Nightly cross-browser matrix** — optional GHA job after UI regression passes on Chromium.
5. **Keep onboarding tests isolated** — use `BaseOnboardingUiSmokeTest`; never dismiss overlays in those tests.
6. **Monitor flaky report** — AI Accountant and registration UI should appear first in `flaky-report.json`.

---

## 9. Quick Run Commands

```bash
# Smoke
mvn test -Papi-smoke -Plocal
mvn test -Pui-smoke -Plocal

# Regression
mvn test -Papi-regression -Plocal
mvn test -Pui-regression -Plocal

# Contract & security
mvn test -Pcontract -Plocal
mvn test -Psecurity -Plocal

# E2E
mvn test -Pe2e -Plocal

# Cross-browser
mvn test -Pcross-browser-firefox -Plocal
mvn test -Pcross-browser-webkit -Plocal

# CI ephemeral
mvn test -Papi-regression -Pci
```

---

## 10. Related Documentation

- [CI_INFRASTRUCTURE.md](CI_INFRASTRUCTURE.md) — nightly pipeline & artifacts
- [CI_DIAGNOSTICS.md](CI_DIAGNOSTICS.md) — failure diagnostics
- [FLAKY_TEST_DETECTION.md](FLAKY_TEST_DETECTION.md) — flaky classification
- [API-REGRESSION-COVERAGE.md](../API-REGRESSION-COVERAGE.md) — detailed API regression inventory
- [CONTRACT-COVERAGE.md](../CONTRACT-COVERAGE.md) — contract endpoint list
