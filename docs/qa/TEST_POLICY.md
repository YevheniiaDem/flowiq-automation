# FlowIQ Test Policy

| Field | Value |
|-------|-------|
| **Document ID** | QA-POL-001 |
| **Version** | 1.0 |
| **Status** | Approved |
| **Last updated** | 2026-06-28 |
| **Owner** | QA Engineering / Engineering Leadership |

---

## 1. Policy Statement

All FlowIQ releases must meet defined quality gates backed by automated tests in `flowiq-automation` and unit tests in `flowiq-backend`. Quality is a shared responsibility; this policy sets mandatory standards for test design, execution, reporting, and defect handling.

## 2. Scope

Applies to:

- `flowiq-automation` (API, UI, E2E, contract, security tests)
- CI workflows under `.github/workflows/`
- Contributors to `flowiq-backend` and `flowiq-frontend` affecting user-visible behavior

## 3. Mandatory Requirements

### 3.1 Pull requests

| Rule | Enforcement |
|------|-------------|
| Automation must compile | `pr-validation` → `compile` job |
| Contract tests must pass against PR backend branch | `contract-tests` job |
| No committed secrets in test code | Code review; use `TEST_USER_*` env vars |
| New API endpoints require contract schema or documented exception | Review + `-Papi-change-detection` |

### 3.2 Test design

| Rule | Rationale |
|------|-----------|
| API tests use service layer only | No raw Rest Assured in test classes (`BaseApiTest` pattern) |
| UI tests use page objects | `com.flowiq.pages.*`, `data-testid` via `TestIds` |
| Unique data for registration | `TestDataFactory.randomRegisterRequest()` |
| Destructive tests avoid shared demo user when possible | Prefer ephemeral register for onboarding E2E |
| Tests declare TestNG groups | Enables suite filtering (`smoke`, `api-regression`, `e2e`, etc.) |
| Allure annotations on UI/E2E | `@Epic`, `@Feature`, `@Story`, `@Severity` |

### 3.3 Retries

| Allowed | Not allowed |
|---------|-------------|
| Infrastructure failures (connection, timeout, 5xx) via `InfrastructureRetryAnalyzer` (max 2) | Re-running failed assertions to "hope it passes" |
| API layer `RetrySupport` for transient HTTP errors | Blanket `@Test(retryAnalyzer = ...)` on business logic |

### 3.4 Reporting

| Rule | Implementation |
|------|----------------|
| Failed nightly jobs upload artifacts | Surefire, Allure, Playwright traces (failures) |
| Flaky tests tracked | `detect-flaky-tests` job; investigate within 2 sprints |
| P1 defects block release | See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) |

### 3.5 Documentation

| Rule | Trigger |
|------|---------|
| Update QA docs when adding user flows | New feature merge |
| Update coverage report after major automation changes | Sprint end |
| Regenerate traceability on API surface change | `-Prequirements-traceability` |

## 4. Prohibited Practices

1. Disabling or skipping tests to merge without QA and tech lead approval.
2. Using production credentials in automation repositories.
3. Hard-coding environment URLs outside `environments/*.properties`.
4. Ignoring contract failures on PR (gate is mandatory).
5. Retrying business assertion failures in CI configuration.
6. Committing `reportportal.properties` or local-only secrets (project rule).

## 5. Roles & Accountability

| Role | Accountability |
|------|----------------|
| Author | Write tests with correct groups; fix own PR failures |
| Reviewer | Verify coverage, data isolation, no flaky patterns |
| QA Engineering | Maintain suites, page objects, nightly health |
| On-call / CI owner | Restore ephemeral stack, secrets, runner capacity |

## 6. Exceptions

Exceptions to this policy require:

1. Written rationale in PR description or Jira ticket.
2. QA Lead approval for skipping regression on release.
3. Time-bound remediation plan for missing coverage.

## 7. Compliance Review

| Review | Frequency |
|--------|-----------|
| Suite health (pass rate, duration) | Weekly |
| Flaky test report | After each nightly |
| Coverage vs requirements matrix | Per release |
| Policy adherence audit | Quarterly |

## 8. Related Documents

- [TEST_STRATEGY.md](TEST_STRATEGY.md)
- [DEFECT_WORKFLOW.md](DEFECT_WORKFLOW.md)
- [ENTRY_EXIT_CRITERIA.md](ENTRY_EXIT_CRITERIA.md)
- [AUTOMATION_STRATEGY.md](AUTOMATION_STRATEGY.md)
