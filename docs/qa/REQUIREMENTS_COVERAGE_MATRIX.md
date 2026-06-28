# FlowIQ Requirements Coverage Matrix

| Field | Value |
|-------|-------|
| **Document ID** | QA-RCM-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Summarizes requirements coverage across test levels, identifies gaps, and quantifies automation maturity per FlowIQ feature.

**Coverage formula (per feature):**

```
Coverage % = (weighted layers covered / layers in scope) × 100

Layers (weight 1 each): API Smoke, API Regression, Contract, UI Smoke, UI Regression, E2E, Security (if applicable)
UI-only features: UI Smoke, UI Regression, E2E only (max 3 layers)
```

---

## 2. Executive Summary

| Metric | Value |
|--------|-------|
| Features in scope | 21 |
| Features with ≥1 automated layer | **21 (100%)** |
| Features with full API stack (smoke + regression + contract) | 10 |
| Features with E2E journey | 11 |
| Known P1 gaps | 2 (password change, avatar upload) |
| Overall weighted coverage | **~87%** |

---

## 3. Requirements Coverage Table

| Req ID | Requirement | API Smk | API Reg | Contract | UI Smk | UI Reg | E2E | Sec | **Coverage** | Status |
|--------|-------------|:-------:|:-------:|:--------:|:------:|:------:|:---:|:---:|:------------:|--------|
| REQ-01 | User login/logout | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | **86%** | Met |
| REQ-02 | User registration | ✅ | ✅ | ✅ | ✅ | — | ✅ | — | **83%** | Met |
| REQ-03 | Dashboard KPIs | ✅ | ✅ | ✅ | ✅ | — | ⚠️ | ✅ | **83%** | Met |
| REQ-04 | CSV import | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | **100%** | Met |
| REQ-05 | Transaction management | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | **100%** | Met |
| REQ-06 | Analytics & insights | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **100%** | Met |
| REQ-07 | Financial forecasts | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | **100%** | Met |
| REQ-08 | AI Accountant | ✅ | ✅ | ✅ | ✅ | — | — | ✅ | **71%** | Partial |
| REQ-09 | Task management | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | **100%** | Met |
| REQ-10 | Notifications | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | **100%** | Met |
| REQ-11 | Report generation | ✅ | ✅ | ✅ | ✅ | — | ✅ | ✅ | **100%** | Met |
| REQ-12 | Application settings | ✅ | ✅ | — | ✅ | ✅ | ✅ | — | **71%** | Met |
| REQ-13 | User profile | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **100%** | Met |
| REQ-14 | Security & auth boundaries | — | ⚠️ | — | ⚠️ | — | ⚠️ | ✅ | **25%** | **Gap** |
| REQ-15 | Help Center / Business Guide | ✅ | ✅ | ✅ | ✅ | ⚠️ | — | ✅ | **71%** | Met |
| REQ-16 | Demo workspace | — | — | — | ✅ | ✅ | — | — | **67%** | Met (UI) |
| REQ-17 | User onboarding | — | — | — | ✅ | ✅ | ✅ | — | **100%** | Met (UI) |
| REQ-18 | Empty state UX | — | — | — | ✅ | ✅ | — | — | **67%** | Met (UI) |
| REQ-19 | Activation checklist | — | — | — | ✅ | ✅ | — | — | **67%** | Met (UI) |
| REQ-20 | What's New modal | — | — | — | ✅ | — | — | — | **33%** | Met (UI) |
| REQ-21 | Product tour | — | — | — | ⚠️ | ✅ | — | — | **50%** | Partial |

---

## 4. Coverage by Test Level

| Test level | Requirements covered | Count |
|------------|---------------------|-------|
| API Smoke | REQ-01–13, 15 | 13 / 21 |
| API Regression | REQ-01–13, 15 | 13 / 21 |
| Contract | REQ-01–11, 13, 15 | 12 / 21 |
| UI Smoke | REQ-01–21 | **21 / 21** |
| UI Regression | REQ-06, 12–13, 15–21 | 9 / 21 |
| E2E | REQ-01–07, 09–13, 17 | 11 / 21 |
| Security | REQ-01, 03–11, 13, 15 | 11 / 21 |

---

## 5. Gap Analysis

### P1 — Release risk

| Req | Gap | Recommendation | Target suite |
|-----|-----|----------------|--------------|
| REQ-14 | Password change not tested end-to-end | API test + E2E with session revoke | `ProfileRegressionTest`, new E2E |
| REQ-14 | Avatar upload missing | Multipart API + UI test | profile module |

### P2 — Quality improvement

| Req | Gap | Recommendation |
|-----|-----|----------------|
| REQ-08 | No E2E for AI chat | Add when chat stability improves |
| REQ-12 | No settings contract | Schema for notification preferences |
| REQ-21 | Product tour step assertions | Driver.js step validation (fragile) |
| REQ-15 | Help article E2E | Optional read-article journey |

### P3 — Nice to have

| Req | Gap | Recommendation |
|-----|-----|----------------|
| REQ-18 | Empty state with zero-data tenant | Dedicated test user |
| REQ-03 | Explicit dashboard E2E class | Extend `UserLoginE2ETest` or add dedicated class |
| Cross-cutting | Nightly cross-browser | GHA matrix job |

---

## 6. Module Maturity Ratings

| Rating | Criteria | Features |
|--------|----------|----------|
| **A — Mature** | Smoke + regression + contract + UI + E2E | Transactions, Reports, Notifications, Tasks, Analytics |
| **B — Strong** | Smoke + regression + contract + UI | Dashboard, Forecasts, Imports, Profile, Auth |
| **C — Adequate** | Smoke + partial deeper layers | AI Accountant, Settings, Help, Onboarding UX |
| **D — Needs work** | Known P1 gaps | Security (password change) |

---

## 7. PR vs Release Coverage

| Gate | Layers enforced | Requirements |
|------|-----------------|--------------|
| PR merge | Contract + compile | REQ with public API changes must update schema |
| Nightly | Smoke + regression + contract + security | All REQ-01–15 API features |
| Release | Above + E2E + checklist | REQ-01–13 minimum E2E |

---

## 8. Maintenance

| Event | Update action |
|-------|---------------|
| New feature shipped | Add row to §3; update [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md) |
| New test class | Recalculate coverage % |
| Sprint end | Run `-Ptest-gap-analysis`; sync gaps |
| Major release | Run `-Prequirements-traceability` |

```bash
mvn verify -Prequirements-traceability
mvn verify -Ptest-gap-analysis
```

---

## 9. Related Metrics (Automation Repo)

| Metric | Source |
|--------|--------|
| API regression executions | ~269 ([API-REGRESSION-COVERAGE.md](../API-REGRESSION-COVERAGE.md)) |
| Contract tests | 19 |
| UI smoke classes | 14 |
| E2E classes | 11 |
| User flows automated | 21/21 ([AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md)) |

---

## 10. References

- [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md)
- [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md)
- [TEST_SCOPE.md](TEST_SCOPE.md)
- [RISK_ANALYSIS.md](RISK_ANALYSIS.md)
