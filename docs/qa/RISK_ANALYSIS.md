# FlowIQ Risk Analysis

| Field | Value |
|-------|-------|
| **Document ID** | QA-RSK-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Identifies quality risks for FlowIQ, assesses likelihood and impact, and maps mitigations to existing automation and process controls.

## 2. Risk Assessment Matrix

| ID | Risk | Likelihood | Impact | Score | Mitigation |
|----|------|------------|--------|-------|------------|
| R-01 | Auth/session regression exposes data | Medium | Critical | **High** | Auth regression, security suite, contract `/auth/me`, user isolation tests |
| R-02 | Transaction data loss or corruption | Low | Critical | **High** | Transactions regression (CRUD, isolation), E2E create flow |
| R-03 | API breaking change ships undetected | Medium | High | **High** | PR contract gate, `-Papi-change-detection`, JSON schemas |
| R-04 | AI Accountant chat flakiness masks real failures | High | Medium | **Medium** | Flaky detection; smoke only for chat; traces on failure |
| R-05 | Onboarding overlays block UI tests | Medium | Medium | **Medium** | `OnboardingUiHelper.dismissOverlays()`; `BaseOnboardingUiSmokeTest` |
| R-06 | Parallel UI tests share state | Low | Medium | **Medium** | ThreadLocal Playwright sessions; unique register emails |
| R-07 | Ephemeral CI stack instability | Medium | High | **High** | Docker diagnostics artifacts; infra-only retries; stack teardown |
| R-08 | Demo user mutation breaks shared tests | Medium | Low | **Low** | Profile tests restore data; register E2E for destructive paths |
| R-09 | Report generation wrong figures | Low | High | **Medium** | 18 format combinations in regression; E2E generate |
| R-10 | Import pipeline fails silently | Medium | High | **Medium** | Imports regression + E2E CSV; contract list schema |
| R-11 | Cross-browser layout breakage | Medium | Medium | **Medium** | `-Pcross-browser-firefox/webkit`; weekly manual spot check |
| R-12 | Password change not tested | High | High | **High** | **Gap** — P1: add API + E2E (documented) |
| R-13 | Notification preferences drift | Medium | Low | **Low** | **Gap** — P2: PUT contract + UI toggle |
| R-14 | Locale-specific selectors break UI | Medium | Medium | **Medium** | Prefer `data-testid` (`TestIds`); tab index for settings |
| R-15 | Nightly duration creep blocks feedback | Medium | Medium | **Medium** | Parallel suites; parallel GHA jobs; smoke subset on demand |

**Scoring:** High = immediate action; Medium = monitor + plan; Low = accept with controls.

## 3. Technical Risk Detail

### 3.1 Authentication & authorization (R-01)

| Control | Implementation |
|---------|----------------|
| Negative API tests | `AuthRegressionTest`, all modules' unauthorized cases |
| Security smoke | `SecuritySmokeApiTest` — 401 without token |
| Session validation | `GET /auth/me` smoke, contract, regression |

### 3.2 Financial data integrity (R-02, R-09)

| Control | Implementation |
|---------|----------------|
| Transaction CRUD | `TransactionsRegressionTest` (~39 executions) |
| User isolation | Regression negative: access other user's transaction |
| Forecasts / analytics | Dedicated regression modules |
| Reports | Parameterized generate tests |

### 3.3 CI / infrastructure (R-07)

| Control | Implementation |
|---------|----------------|
| Health wait | `wait-for-backend.sh`, compose healthchecks |
| Retries | `InfrastructureRetryAnalyzer` (max 2, infra only) |
| Diagnostics | `upload-docker-diagnostics`, compressed logs |
| Ephemeral project | `COMPOSE_PROJECT_NAME=flowiq-ci-${{ github.run_id }}` |

### 3.4 Test reliability (R-04, R-05, R-06)

| Control | Implementation |
|---------|----------------|
| Flaky job | `detect-flaky-tests` with 90-day history |
| Onboarding | localStorage keys in `OnboardingUiHelper` |
| UI stability guide | [UI-SMOKE-STABILITY.md](../UI-SMOKE-STABILITY.md) |

## 4. Coverage Gaps as Risks

| Gap | Risk ID | Priority |
|-----|---------|----------|
| Password change E2E | R-12 | P1 |
| Avatar upload | — | P1 |
| Product tour steps | R-14 | P2 |
| Integrations page | — | P2 |
| Zero-data tenant | R-08 | P3 |

## 5. Risk Response Strategies

| Strategy | When used |
|----------|-----------|
| **Mitigate** | Add automation (primary approach) |
| **Monitor** | Flaky reports, nightly trends |
| **Accept** | Exploratory-only areas with PO sign-off |
| **Transfer** | External pen test for security compliance |

## 6. Risk-Based Regression

Automated agent recommends suite depth:

```bash
mvn verify -Prisk-based-regression
```

Output: `docs/ai-reports/` — recommends `FULL_REGRESSION`, `PARTIAL_REGRESSION`, or `SMOKE_ONLY`.

## 7. Review Cadence

| Activity | Frequency |
|----------|-----------|
| Update this register | Each major release |
| Review flaky report | Weekly |
| Re-score after incidents | Within 48h of P1 |

## 8. References

- [REGRESSION_STRATEGY.md](REGRESSION_STRATEGY.md)
- [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md)
- [FLAKY_TEST_DETECTION.md](../automation/FLAKY_TEST_DETECTION.md)
