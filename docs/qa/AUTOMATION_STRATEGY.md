# FlowIQ Automation Strategy

| Field | Value |
|-------|-------|
| **Document ID** | QA-AUT-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Vision

Maintain a single automation framework (`flowiq-automation`) that provides fast PR feedback, comprehensive nightly regression, and traceable coverage of all FlowIQ user flows — with infrastructure-only retries and parallel execution at scale.

## 2. Framework Architecture

```
src/main/java/com/flowiq/
├── config/           EnvironmentConfig, ConfigManager
├── constants/        ApiEndpoints, TestIds, Routes
├── services/         BaseApiService, *Service (API layer)
├── pages/            Page Object Model (Playwright)
├── factories/        TestDataFactory, builders
├── utils/            OnboardingUiHelper, UiAssertions
└── listeners/        Allure, InfrastructureRetry*

src/test/java/com/flowiq/
├── api/              smoke + legacy regression API tests
├── api/regression/   modular regression (12 modules)
├── contracts/        JSON Schema validation
├── ui/smoke/         UI smoke
├── ui/regression/    UI deep checks
├── e2e/              journey tests
└── base/             BaseApiTest, BaseUiTest, AuthenticatedUiTest
```

## 3. Design Patterns

| Pattern | Application |
|---------|-------------|
| **Service layer** | Tests call `AuthService`, `TransactionService`, etc. — not raw HTTP |
| **Page Object Model** | `LoginPage`, `SettingsPage`, `Pages` facade |
| **Builder** | `TransactionRequestBuilder`, `TaskRequestBuilder` |
| **Factory** | `TestDataFactory` for reproducible payloads |
| **Base test hierarchy** | Shared setup, auth injection, Playwright lifecycle |
| **TestNG groups** | Suite composition via XML |
| **JSON Schema contracts** | Decoupled API shape validation |

## 4. Suite Catalog

| Profile | Suite XML | Parallel | Retries |
|---------|-----------|----------|---------|
| `-Papi-smoke` | `api-smoke-suite.xml` | 4 threads | Infra only |
| `-Pui-smoke` | `ui-smoke-suite.xml` | 3 threads | Infra only |
| `-Papi-regression` | `regression-api-suite.xml` | 4 threads | Infra only |
| `-Pui-regression` | `ui-regression-suite.xml` | 3 threads | Infra only |
| `-Pcontract` | `contract-suite.xml` | 3 threads | Infra only |
| `-Psecurity` | `security-suite.xml` | Sequential | Infra only |
| `-Pe2e` | `e2e-suite.xml` | Sequential | Infra only |
| `-Pcross-browser-*` | `cross-browser-suite.xml` | Sequential | Infra only |

Listeners on all suites:

- `AllureListener`
- `TestExecutionListener`
- `InfrastructureRetryTransformer`

## 5. Retry Policy

**Infrastructure only** — never mask product bugs.

```java
// InfrastructureRetryAnalyzer — max 2 attempts
// Retries: connection, timeout, 5xx, RetryableApiException
// Never retries: assertion failures, 4xx business errors
```

API layer additionally uses `RetrySupport` for transient HTTP failures in services.

## 6. Parallel Execution

| Layer | Strategy |
|-------|----------|
| API | TestNG `parallel="methods"` — stateless HTTP |
| UI | ThreadLocal Playwright browser/context per thread |
| E2E | Sequential — avoid shared UI state |
| CI jobs | GHA parallel jobs: smoke, api-regression, ui-regression, contract, security |

## 7. Cross-Browser

| Browser | Maven profile |
|---------|---------------|
| Chromium | Default all profiles |
| Firefox | `-Pcross-browser-firefox` |
| WebKit | `-Pcross-browser-webkit` |

Property override: `-Dbrowser=firefox`

Recommended: weekly manual or scheduled cross-browser run (not yet in nightly matrix).

## 8. Reporting & Observability

| Tool | Output |
|------|--------|
| Allure | Steps, attachments, severity, epic/feature |
| Surefire | `target/surefire-reports/` |
| Playwright trace | On UI failure (CI artifacts) |
| GitHub Pages | Combined nightly Allure |
| CI summary | `write-ci-summary` action |
| Flaky report | `flaky-report.json` + agent markdown |

## 9. CI/CD Integration

| Event | Automation |
|-------|------------|
| PR | Compile + contract (15→19 tests against PR backend) |
| Nightly | Ephemeral full stack + all suites |
| Manual | API/UI smoke against stage/dev |
| Agents | Optional `verify` profiles for gap, flaky, traceability |

See [CI-CD.md](../CI-CD.md), [CI_INFRASTRUCTURE.md](../automation/CI_INFRASTRUCTURE.md).

## 10. Coverage Status

| Metric | Value |
|--------|-------|
| User flows with automation | **21 / 21** |
| API smoke modules | 12 |
| API regression modules | 12 |
| Contract tests | 19 |
| UI smoke classes | 14 |
| UI regression classes | 3 |
| E2E classes | 11 |

Detail: [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md).

## 11. Quality Intelligence Agents

| Profile | Agent | Output |
|---------|-------|--------|
| `-Prequirements-traceability` | RequirementsTraceabilityAgent | traceability-matrix.md |
| `-Ptest-gap-analysis` | TestGapAnalyzerAgent | test-gap-analysis.md |
| `-Pflaky-test-investigation` | FlakyTestInvestigator | flaky-tests-report.md |
| `-Prisk-based-regression` | RiskBasedRegressionAgent | regression recommendation |
| `-Prelease-risk-assessment` | ReleaseRiskAssessmentAgent | release-readiness-report.md |
| `-Papi-change-detection` | ApiChangeDetectionAgent | api-change-report.md |

## 12. Implementation Standards

### New API test

1. Add endpoint to `ApiEndpoints`
2. Extend appropriate `*Service`
3. Add smoke method with groups `smoke`, `api`
4. Add regression positives/negatives in `api.regression.{module}`
5. Add JSON schema + `*ContractTest` if public response

### New UI test

1. Add `data-testid` to frontend → `TestIds`
2. Create/extend page object
3. Smoke in `ui.smoke` with group `ui-smoke`
4. E2E if multi-step journey

### Selectors

- Prefer `getByTestId()` over CSS text
- Settings tabs: index-based (`openTabByIndex`) for locale independence
- Onboarding: use `OnboardingUiHelper` unless testing overlays explicitly

## 13. Roadmap

| Priority | Item |
|----------|------|
| P1 | Password change API + E2E |
| P1 | Avatar upload tests |
| P2 | Notification preferences contract + UI |
| P2 | Nightly cross-browser GHA matrix |
| P3 | Expand contract to POST create endpoints |

## 14. References

- [TEST_STRATEGY.md](TEST_STRATEGY.md)
- [SMOKE_STRATEGY.md](SMOKE_STRATEGY.md)
- [REGRESSION_STRATEGY.md](REGRESSION_STRATEGY.md)
- [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md)
