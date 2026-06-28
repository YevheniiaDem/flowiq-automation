# FlowIQ Release Checklist

| Field | Value |
|-------|-------|
| **Document ID** | QA-REL-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Pre-production verification checklist for FlowIQ releases. All items must be completed or explicitly waived before production deployment.

**Release candidate:** Tag or commit SHA: `________________`  
**Target date:** `________________`  
**Sign-off:** QA Lead / Engineering Lead / Product Owner

---

## 2. Code & Build

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1.1 | Release branch / tag identified | ☐ | Git tag/SHA |
| 1.2 | `flowiq-automation` compiles on release SHA | ☐ | `mvn clean compile test-compile` |
| 1.3 | `flowiq-backend` unit tests pass | ☐ | CI or local surefire |
| 1.4 | No unmerged critical PRs for release scope | ☐ | PR list |
| 1.5 | Changelog / release notes drafted | ☐ | Link |

---

## 3. Automated Testing

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 2.1 | Nightly regression green on release SHA | ☐ | GHA run URL |
| 2.2 | Smoke (API + UI) — 100% pass | ☐ | Allure / surefire |
| 2.3 | API regression — 100% pass | ☐ | `api-regression` job |
| 2.4 | UI regression — 100% pass | ☐ | `ui-regression` job |
| 2.5 | Contract tests — 100% pass | ☐ | 19/19 |
| 2.6 | Security suite — 100% pass | ☐ | `-Psecurity` |
| 2.7 | E2E suite executed | ☐ | `mvn test -Pe2e -Pci` or `-Plocal` |
| 2.8 | Failed-only (non-flaky) count = 0 | ☐ | `detect-flaky-tests` output |
| 2.9 | Allure report reviewed | ☐ | GitHub Pages URL |

---

## 4. Quality Gates

| # | Item | Status | Notes |
|---|------|--------|-------|
| 3.1 | Zero open **P1** defects | ☐ | |
| 3.2 | P2 defects reviewed and accepted | ☐ | List waivers |
| 3.3 | Known gaps documented | ☐ | [REQUIREMENTS_COVERAGE_MATRIX.md](REQUIREMENTS_COVERAGE_MATRIX.md) |
| 3.4 | Flaky tests triaged | ☐ | [FLAKY_TEST_DETECTION.md](../automation/FLAKY_TEST_DETECTION.md) |
| 3.5 | API change report reviewed (if API release) | ☐ | `-Papi-change-detection` |
| 3.6 | Release risk assessment | ☐ | `-Prelease-risk-assessment` optional |

---

## 5. Environment & Configuration

| # | Item | Status |
|---|------|--------|
| 4.1 | Stage smoke passed (`api-smoke` + `ui-smoke`) | ☐ |
| 4.2 | Production config reviewed (URLs, secrets, feature flags) | ☐ |
| 4.3 | Database migrations applied / rollback plan documented | ☐ |
| 4.4 | Demo user / seed data compatible with smoke tests | ☐ |

---

## 6. Cross-Functional

| # | Item | Owner | Status |
|---|------|-------|--------|
| 5.1 | Product acceptance criteria met | PO | ☐ |
| 5.2 | Security review (if auth/payment changes) | Security | ☐ |
| 5.3 | Documentation updated (user + API) | Tech writing | ☐ |
| 5.4 | Monitoring / alerts configured | Ops | ☐ |
| 5.5 | Rollback procedure documented | Eng | ☐ |

---

## 7. Post-Release

| # | Item | Timing |
|---|------|--------|
| 6.1 | Production smoke (manual or scheduled) | Within 1h |
| 6.2 | Monitor error rates / health endpoints | 24h |
| 6.3 | Close release defect filter / tag | After verification |
| 6.4 | Retrospective if P1 incident | Within 1 week |

---

## 8. Quick Commands

```bash
# Full pre-release local validation (stack required)
mvn test -Papi-smoke -Plocal
mvn test -Pui-smoke -Plocal
mvn test -Papi-regression -Plocal
mvn test -Pcontract -Plocal
mvn test -Psecurity -Plocal
mvn test -Pe2e -Plocal

# Trigger nightly on specific refs
# GitHub Actions → Nightly Regression → workflow_dispatch
#   automation_ref, backend_ref, frontend_ref, test_suite: full
```

---

## 9. Waiver Template

| Defect ID | Severity | Reason for waiver | Approver | Date |
|-----------|----------|-------------------|----------|------|
| | | | | |

---

## 10. Sign-Off

| Role | Name | Signature | Date |
|------|------|-----------|------|
| QA Lead | | | |
| Engineering Lead | | | |
| Product Owner | | | |

---

## 11. References

- [ENTRY_EXIT_CRITERIA.md](ENTRY_EXIT_CRITERIA.md)
- [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md)
- [DEFECT_WORKFLOW.md](DEFECT_WORKFLOW.md)
