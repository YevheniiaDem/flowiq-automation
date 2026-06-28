# FlowIQ Test Scope

| Field | Value |
|-------|-------|
| **Document ID** | QA-SCP-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. In Scope

### 1.1 Functional areas (21 user flows)

| Flow | Description | Automation status |
|------|-------------|-------------------|
| Authentication | Login, logout, session (`/auth/me`) | Full |
| Registration | New user signup | Full |
| Dashboard | KPIs, charts, health widgets | Full |
| Imports | CSV upload, job list, status | Full |
| Transactions | CRUD, search, filter, summary | Full |
| Analytics | Overview, trends, FOP insights | Full |
| Forecasts | Revenue, expense, profit, tax projections | Full |
| AI Accountant | Health, recommendations, chat | Partial (no E2E chat) |
| Tasks | CRUD, grouped, today/upcoming | Full |
| Notifications | List, read state, summary | Full |
| Reports | Preview, generate, download | Full |
| Settings | Tabs: profile, notifications, security, help | Full |
| Profile | GET/PUT profile, FOP profile | Full |
| Security | 401 unauthorized matrix, password tab UI | Partial (no password-change E2E) |
| Help Center | Business Guide articles, help & learn | Full |
| Demo Workspace | Demo banner, demo data | UI only |
| Onboarding | Welcome, overlays, checklist | Full |
| Empty States | Analytics/transactions empty UI | UI only |
| Activation Checklist | Onboarding checklist widget | UI only |
| What's New | Release modal dismiss | UI smoke |
| Product Tour | Driver.js tour from Help center | UI regression |

### 1.2 Test types in scope

| Type | Implementation |
|------|----------------|
| API smoke | `-Papi-smoke` — 12 test classes |
| API regression | `-Papi-regression` — 12 modules, ~269 executions |
| Contract | `-Pcontract` — 19 JSON Schema tests |
| UI smoke | `-Pui-smoke` — 14 classes in `com.flowiq.ui.smoke` |
| UI regression | `-Pui-regression` — 3 classes in `com.flowiq.ui.regression` |
| E2E | `-Pe2e` — 11 classes in `com.flowiq.e2e` |
| Security | `-Psecurity` — `SecuritySmokeApiTest` |
| Cross-browser | `-Pcross-browser-firefox`, `-Pcross-browser-webkit` |
| Integration DB | `-Pintegration-db` — Testcontainers + Flyway |
| Unit | `-Punit` — framework and agent unit tests |

### 1.3 Non-functional (limited)

| Area | Scope |
|------|-------|
| API response time | Implicit via timeouts (`api.timeout=30000`) |
| Auth isolation | Regression negative tests + security suite |
| Schema backward compatibility | Contract tests on PR and nightly |
| UI stability | Flaky detection, Playwright traces on failure |

## 2. Out of Scope

| Item | Notes |
|------|-------|
| Load / stress testing | Not implemented in this repository |
| SOC2 / formal penetration test | External process |
| Email delivery (Mailhog) | Optional Compose profile; not in default nightly |
| S3/MinIO file storage | Optional Compose profile |
| Native mobile applications | Web responsive only |
| Localization matrix (all locales) | Default Ukrainian UI; tests use `data-testid` where possible |
| Avatar upload | Documented gap (P1) |
| Integrations marketplace | Future `/integrations` page |
| Full Product Tour step-by-step | Documented gap (P2) |

## 3. Environment Scope

| Environment | `env` property | In scope for |
|-------------|----------------|--------------|
| Local | `local` | Developer daily runs |
| Docker | `docker` | Compose-based local stack |
| CI ephemeral | `ci` | Nightly regression |
| Stage | `stage` | Manual smoke workflows |
| Dev | `dev` | Early integration smoke |
| Headed debug | `local-headed` | `-Pui-headed`, `-Pe2e-headed` |

URLs (local/ci): `base.url=http://localhost:3000`, `api.url=http://localhost:8080/api`.

## 4. Repository Boundaries

```
flowiq-automation  → API/UI/E2E tests, schemas, CI orchestration
flowiq-backend     → Unit tests, API implementation
flowiq-frontend    → UI under test (no test code in scope of this doc)
```

## 5. Known Gaps (documented, tracked)

| Priority | Gap |
|----------|-----|
| P1 | Password change API/E2E |
| P1 | Avatar upload |
| P2 | Notification preferences PUT + UI toggle |
| P2 | Full Product Tour assertions |
| P2 | Integrations page |
| P3 | Dedicated zero-data tenant for empty states |
| P3 | Nightly cross-browser matrix in GHA |

Source: [AUTOMATION_COVERAGE_REPORT.md](../automation/AUTOMATION_COVERAGE_REPORT.md) §5.

## 6. References

- [TEST_LEVELS.md](TEST_LEVELS.md)
- [TEST_TYPES.md](TEST_TYPES.md)
- [REQUIREMENTS_COVERAGE_MATRIX.md](REQUIREMENTS_COVERAGE_MATRIX.md)
