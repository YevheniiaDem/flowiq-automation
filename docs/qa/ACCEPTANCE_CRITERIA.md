# FlowIQ Acceptance Criteria

| Field | Value |
|-------|-------|
| **Document ID** | QA-ACC-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Defines product-level acceptance criteria for FlowIQ features, mapped to automated verification where implemented.

## 2. Global Acceptance Criteria

| ID | Criterion | Verification |
|----|-----------|--------------|
| AC-G-01 | User can register, login, and logout securely | `AuthRegressionTest`, `UserLoginE2ETest`, `RegisterOnboardingE2ETest` |
| AC-G-02 | Authenticated user sees dashboard with financial summary | `DashboardSmokeApiTest`, `DashboardSmokeTest` |
| AC-G-03 | Unauthorized API access returns 401 | `SecuritySmokeApiTest` |
| AC-G-04 | API responses conform to published JSON schemas | `-Pcontract` (19 tests) |
| AC-G-05 | Critical journeys complete without manual intervention | `-Pe2e` (11 flows) |
| AC-G-06 | Nightly regression passes on release candidate | [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) |

## 3. Feature Acceptance Criteria

### F-01 Authentication

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-AUTH-01 | Valid credentials | User logs in | 200 + session token; redirected to dashboard | ✅ |
| AC-AUTH-02 | Invalid credentials | User logs in | 401/400; error shown | ✅ |
| AC-AUTH-03 | Active session | User calls `/auth/me` | User profile returned | ✅ |
| AC-AUTH-04 | Active session | User logs out | Session invalidated | ✅ |

### F-02 Registration

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-REG-01 | New email | User registers | Account created; can login | ✅ |
| AC-REG-02 | Existing email | User registers | Duplicate rejected | ✅ |
| AC-REG-03 | New user (UI) | Completes registration | Onboarding shown | ✅ E2E |

### F-03 Dashboard

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-DASH-01 | Logged-in user | Opens dashboard | KPIs and charts load | ✅ |
| AC-DASH-02 | No token | Calls dashboard API | 401 | ✅ |

### F-04 Imports

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-IMP-01 | Valid CSV | User uploads | Import job created | ✅ E2E |
| AC-IMP-02 | Invalid file | User uploads | Error returned | ✅ regression |
| AC-IMP-03 | Jobs exist | User lists imports | Paginated list | ✅ contract |

### F-05 Transactions

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-TXN-01 | Logged-in user | Creates transaction | Persisted and listed | ✅ E2E |
| AC-TXN-02 | User A | Accesses User B transaction | Denied | ✅ regression |
| AC-TXN-03 | Filters applied | Lists transactions | Correct subset | ✅ regression |

### F-06 Analytics

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-ANL-01 | User with data | Opens analytics | Overview metrics shown | ✅ |
| AC-ANL-02 | User without data | Opens analytics | Empty state with CTA | ✅ UI regression |
| AC-ANL-03 | FOP user | Views FOP insights | Insights endpoint 200 | ✅ |

### F-07 Forecasts

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-FCT-01 | Logged-in user | Views forecast summary | Summary schema valid | ✅ contract |
| AC-FCT-02 | User | Navigates forecasts UI | Page loads | ✅ smoke + E2E |

### F-08 AI Accountant

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-AI-01 | Service healthy | Health check | 200 + schema | ✅ |
| AC-AI-02 | User | Opens AI page | UI loads | ✅ smoke |
| AC-AI-03 | User | Sends chat message | Response received | ⚠️ regression (flaky-prone) |

### F-09 Tasks

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-TSK-01 | User | Creates task | Task in list | ✅ regression |
| AC-TSK-02 | Open task | Marks complete | Status updated | ✅ E2E |

### F-10 Notifications

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-NOT-01 | Unread notifications | User marks read | Count decreases | ✅ E2E |
| AC-NOT-02 | User | Views summary | Schema valid | ✅ contract |

### F-11 Reports

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-RPT-01 | User | Previews report | Preview schema valid | ✅ |
| AC-RPT-02 | User | Generates PDF/Excel/CSV | Job completes | ✅ regression + E2E |

### F-12 Settings

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-SET-01 | User | Opens settings | All tabs navigable | ✅ smoke + regression |
| AC-SET-02 | User | Updates profile | Changes persisted | ✅ E2E |
| AC-SET-03 | User | Opens security tab | Password form visible | ✅ smoke |

### F-13 Profile

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-PRF-01 | User | GET profile | Schema valid | ✅ contract |
| AC-PRF-02 | User | PUT profile | Updated fields returned | ✅ regression |

### F-14 Security

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-SEC-01 | No token | Access protected endpoints | 401 | ✅ security suite |
| AC-SEC-02 | User | Changes password | Sessions revoked | ❌ **Gap P1** |

### F-15 Help Center

| ID | Given | When | Then | Automated |
|----|-------|------|------|-----------|
| AC-HLP-01 | User | Lists articles | Paginated articles | ✅ |
| AC-HLP-02 | User | Opens article | Content displayed | ✅ regression |

### F-16–F-21 Onboarding & UX

| ID | Criterion | Automated |
|----|-----------|-----------|
| AC-ONB-01 | Welcome / onboarding overlays can be dismissed | ✅ |
| AC-ONB-02 | Activation checklist visible for new users | ✅ |
| AC-ONB-03 | What's New modal dismissible | ✅ smoke |
| AC-ONB-04 | Product tour launchable from Help | ✅ UI regression |
| AC-ONB-05 | Demo workspace banner shown when applicable | ✅ |
| AC-ONB-06 | Empty states guide user to next action | ✅ |

## 4. Non-Functional Acceptance (Limited)

| ID | Criterion | Status |
|----|-----------|--------|
| AC-NF-01 | API responds within 30s timeout | Enforced by config |
| AC-NF-02 | UI operable in Chromium headless CI | ✅ nightly |
| AC-NF-03 | Firefox/WebKit core smoke | Manual / cross-browser profile |

## 5. Definition of Done (Feature)

A feature is **accepted** when:

1. All applicable AC-* IDs above are ✅ or waived by PO.
2. Smoke tests exist for the module.
3. Regression tests cover happy path + primary negatives.
4. Contract test added if new public API endpoint.
5. Traceability matrix row added ([TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md)).

## 6. References

- [REQUIREMENTS_COVERAGE_MATRIX.md](REQUIREMENTS_COVERAGE_MATRIX.md)
- [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md)
- [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md)
