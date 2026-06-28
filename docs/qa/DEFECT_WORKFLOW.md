# FlowIQ Defect Workflow

| Field | Value |
|-------|-------|
| **Document ID** | QA-DEF-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Defines how defects are reported, classified, tracked, and verified for FlowIQ, integrating automated test failures with issue tracking (Jira / GitHub Issues).

## 2. Defect Sources

| Source | Detection | Evidence |
|--------|-----------|----------|
| PR validation | Contract / compile failure | Surefire report, PR check |
| Nightly regression | Failed test method | Allure, `nightly-{run}-*` artifacts |
| Flaky classifier | Intermittent pass/fail | `flaky-report.json`, `detect-flaky-tests` |
| Manual exploratory | Tester observation | Steps, screenshots |
| Production | Support / monitoring | Incident ticket |
| Quality agents | Gap / drift reports | `docs/ai-reports/*.md` |

## 3. Severity Classification

| Severity | Definition | Examples | Response SLA |
|----------|------------|----------|--------------|
| **P1 — Critical** | Production down, data loss, security breach, release blocker | Auth bypass, wrong tax totals, stack unavailable | 4h triage, fix before release |
| **P2 — High** | Major feature broken, no workaround | Cannot create transactions, imports fail | 1 business day triage |
| **P3 — Medium** | Feature degraded, workaround exists | UI layout issue, non-critical API error | Sprint planning |
| **P4 — Low** | Cosmetic, minor inconvenience | Typo, minor alignment | Backlog |

## 4. Priority vs Automation Failure

| Test failure type | Default severity | Notes |
|-------------------|------------------|-------|
| Contract on PR | P1 | Blocks merge |
| Security 401 failure | P1 | Auth boundary broken |
| Smoke failure (nightly) | P2 | Core module unreachable |
| Regression negative | P2 | Business rule violation |
| Regression positive | P2–P1 | Depends on feature |
| Flaky (classified) | P3 | Fix test or product within 2 sprints |
| Cross-browser only | P3 | Compatibility |

Root cause agent: `mvn verify -Proot-cause-analysis` → classifies `BACKEND_BUG`, `UI_BUG`, `TEST_BUG`.

## 5. Defect Lifecycle

```
┌─────────┐    ┌──────────┐    ┌─────────┐    ┌──────────┐    ┌────────┐
│   New   │───▶│ Triaged  │───▶│ In Dev  │───▶│ In QA    │───▶│ Closed │
└─────────┘    └──────────┘    └─────────┘    └──────────┘    └────────┘
                    │                              │
                    └──────── Won't Fix ───────────┘
                    └──────── Duplicate ───────────┘
```

| State | Owner | Actions |
|-------|-------|---------|
| **New** | Reporter | Log steps, env, build SHA, link CI run |
| **Triaged** | QA Lead | Set severity, component, assignee |
| **In Dev** | Developer | Fix code, link PR |
| **In QA** | QA | Verify fix; run targeted Maven profile |
| **Closed** | QA | Confirm automated test passes or manual sign-off |

## 6. Required Defect Fields

| Field | Required | Example |
|-------|----------|---------|
| Title | Yes | `[Transactions] 500 on create expense` |
| Environment | Yes | `ci`, `stage`, `local` |
| Suite / test class | Yes | `TransactionsRegressionTest.shouldCreateExpense` |
| Build / run URL | Yes | GitHub Actions run link |
| Severity | Yes | P2 |
| Component | Yes | API / UI / Infra / Test |
| Steps to reproduce | Yes | From Allure or manual |
| Expected / actual | Yes | Assertion message |
| Attachments | If UI | Playwright trace, screenshot |

## 7. Verification Protocol

After fix merge:

| Severity | Verification |
|----------|--------------|
| P1 | Re-run full nightly or affected suite on CI; E2E if user journey |
| P2 | Re-run module regression + smoke |
| P3 | Re-run failing test class |
| Test bug | Add/fix test; ensure not flaky |

Commands:

```bash
# Targeted API regression module
mvn test -Papi-regression -Plocal -Dtest=TransactionsRegressionTest

# UI smoke module
mvn test -Pui-smoke -Plocal -Dtest=TransactionsSmokeTest

# Full contract
mvn test -Pcontract -Plocal
```

## 8. Release Blockers

Open **P1** defects block release per [ENTRY_EXIT_CRITERIA.md](ENTRY_EXIT_CRITERIA.md).

**P2** require Product Owner + QA waiver documented in release notes.

## 9. Flaky Defect Handling

| Step | Action |
|------|--------|
| 1 | Nightly `detect-flaky-tests` flags test |
| 2 | QA opens defect type `Test Reliability` or links existing product bug |
| 3 | If `TEST_BUG` — fix synchronization, selector, or data isolation |
| 4 | If product bug — normal defect flow |
| 5 | Monitor 5 consecutive greens before closing |

## 10. Integration with CI

| Event | Action |
|-------|--------|
| Nightly failure | CI summary job lists failed suites |
| Failed-only count | Non-flaky failures require defect within 24h |
| PR failure | Author fixes before merge (no separate defect unless environmental) |

## 11. References

- [ENTRY_EXIT_CRITERIA.md](ENTRY_EXIT_CRITERIA.md)
- [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md)
- [CI_DIAGNOSTICS.md](../automation/CI_DIAGNOSTICS.md)
- [FLAKY_TEST_DETECTION.md](../automation/FLAKY_TEST_DETECTION.md)
