# FlowIQ Test Levels

| Field | Value |
|-------|-------|
| **Document ID** | QA-LVL-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Overview

FlowIQ quality is validated at multiple test levels aligned with the ISO/IEC 29119 model and implemented in `flowiq-automation` and `flowiq-backend`.

```
Level 4 — Acceptance / E2E     com.flowiq.e2e.*
Level 3 — System (UI)          com.flowiq.ui.*
Level 3 — System (API)         com.flowiq.api.*
Level 2 — Integration          com.flowiq.api.integration.*
Level 1 — Component / Unit     flowiq-backend unit + com.flowiq.* unit tests
Level 0 — Contract             com.flowiq.contracts.*
```

## 2. Level Definitions

### 2.1 Unit (Component)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Validate isolated classes and business logic |
| **Location** | `flowiq-backend`: `com.flowiq.unit.**`; automation: `com.flowiq.agents.*Test`, `InfrastructureRetryAnalyzerTest` |
| **Runner** | JUnit/TestNG via Maven Surefire |
| **CI** | `pr-validation` → `unit-tests` job (backend) |
| **Dependencies** | Mocked; no live API |

### 2.2 Integration

| Attribute | Detail |
|-----------|--------|
| **Purpose** | API + database interactions, multi-service flows |
| **Location** | `com.flowiq.api.integration.*`, `*IntegrationDbTest` |
| **Profile** | `-Papi-integration`, `-Pintegration-db` |
| **Dependencies** | Testcontainers PostgreSQL, Flyway migrations |
| **When run** | On demand; not in default nightly matrix |

### 2.3 Contract (Interface)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Validate API response shape against JSON Schema |
| **Location** | `com.flowiq.contracts.*` |
| **Profile** | `-Pcontract` |
| **Suite** | `contract-suite.xml` (parallel, 3 threads) |
| **CI** | PR gate + nightly `contract` job |
| **Count** | 19 contract test methods across 12 domain classes |

### 2.4 API System

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Functional API validation via service layer |
| **Smoke** | `com.flowiq.api.*` smoke classes, `-Papi-smoke` |
| **Regression** | `com.flowiq.api.regression.*`, `-Papi-regression` |
| **Base classes** | `BaseApiTest`, `BaseRegressionApiTest` |
| **Auth** | `AuthService` token injection per test class |

### 2.5 UI System

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Page load, navigation, critical UI elements |
| **Smoke** | `com.flowiq.ui.smoke.*`, `-Pui-smoke` |
| **Regression** | `com.flowiq.ui.regression.*`, `-Pui-regression` |
| **Base classes** | `BaseUiTest`, `AuthenticatedUiTest`, `BaseUiSmokeTest` |
| **Browser** | Playwright; default Chromium headless |

### 2.6 End-to-End (Acceptance)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Multi-step user journeys across UI + API effects |
| **Location** | `com.flowiq.e2e.*` (11 classes) |
| **Profile** | `-Pe2e` |
| **Execution** | Sequential (`e2e-suite.xml`, `parallel="false"`) |
| **Examples** | Login → create transaction; register → onboarding; import CSV |

### 2.7 Security

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Unauthorized access, auth boundary checks |
| **Location** | `SecuritySmokeApiTest`, auth negative regression |
| **Profile** | `-Psecurity` |

## 3. Level-to-Suite Mapping

| Level | Maven profile | TestNG suite | Groups |
|-------|---------------|--------------|--------|
| Unit | `-Punit` | `unit-suite.xml` | `unit` |
| Contract | `-Pcontract` | `contract-suite.xml` | `contract` |
| API smoke | `-Papi-smoke` | `api-smoke-suite.xml` | `smoke`, `api` |
| API regression | `-Papi-regression` | `regression-api-suite.xml` | `api-regression` |
| UI smoke | `-Pui-smoke` | `ui-smoke-suite.xml` | `ui-smoke` |
| UI regression | `-Pui-regression` | `ui-regression-suite.xml` | `ui-regression` |
| E2E | `-Pe2e` | `e2e-suite.xml` | `e2e` |
| Security | `-Psecurity` | `security-suite.xml` | `security` |
| Combined smoke | `-Psmoke` | `smoke-suite.xml` | smoke groups |
| Full regression | `-Pregression` | `regression-suite.xml` | regression groups |

## 4. When to Use Each Level

| Scenario | Recommended level |
|----------|-------------------|
| New REST endpoint | Contract + API regression |
| New page / route | UI smoke + page object |
| Cross-page workflow | E2E |
| Refactor internal service | Unit (backend) |
| Auth change | Security + auth regression + contract `/auth/me` |
| CSS-only change | UI smoke spot check |

## 5. References

- [TEST_TYPES.md](TEST_TYPES.md)
- [AUTOMATION_STRATEGY.md](AUTOMATION_STRATEGY.md)
