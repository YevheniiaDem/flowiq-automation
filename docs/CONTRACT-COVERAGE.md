# API Contract Testing Coverage

Contract tests validate live API responses against JSON Schema definitions.  
Run against a real backend: `mvn test -Pcontract -Denv=local|stage|dev`.

## Covered Endpoints (15)

| Domain | Method | Endpoint | Schema | Test Class |
|--------|--------|----------|--------|------------|
| Auth | POST | `/api/auth/login` | `schemas/auth/login-response.schema.json` | `AuthContractTest` |
| Auth | POST | `/api/auth/register` | `schemas/auth/register-response.schema.json` | `AuthContractTest` |
| Transactions | GET | `/api/transactions` | `schemas/transactions/page-response.schema.json` | `TransactionsContractTest` |
| Transactions | GET | `/api/transactions/summary` | `schemas/transactions/summary-response.schema.json` | `TransactionsContractTest` |
| Analytics | GET | `/api/analytics/overview` | `schemas/analytics/overview-response.schema.json` | `AnalyticsContractTest` |
| Analytics | GET | `/api/analytics/fop-insights` | `schemas/analytics/fop-insights-response.schema.json` | `AnalyticsContractTest` |
| Reports | GET | `/api/reports` | `schemas/reports/list-response.schema.json` | `ReportsContractTest` |
| Reports | GET | `/api/reports/preview` | `schemas/reports/preview-response.schema.json` | `ReportsContractTest` |
| Tasks | GET | `/api/tasks` | `schemas/tasks/page-response.schema.json` | `TasksContractTest` |
| Tasks | GET | `/api/tasks/grouped` | `schemas/tasks/grouped-response.schema.json` | `TasksContractTest` |
| Notifications | GET | `/api/notifications` | `schemas/notifications/page-response.schema.json` | `NotificationsContractTest` |
| Notifications | GET | `/api/notifications/summary` | `schemas/notifications/summary-response.schema.json` | `NotificationsContractTest` |
| Forecasts | GET | `/api/forecasts/summary` | `schemas/forecasts/summary-response.schema.json` | `ForecastsContractTest` |
| Business Guide | GET | `/api/business-guide/articles` | `schemas/businessguide/articles-page-response.schema.json` | `BusinessGuideContractTest` |
| AI Accountant | GET | `/api/ai-accountant/health` | `schemas/aiaccountant/health-response.schema.json` | `AIAccountantContractTest` |

**Total contract tests:** 15  
**Total JSON schemas (domain-organized):** 15

## Schema Inventory by Domain

```
schemas/
├── auth/
│   ├── login-response.schema.json
│   └── register-response.schema.json
├── transactions/
│   ├── page-response.schema.json
│   └── summary-response.schema.json
├── analytics/
│   ├── overview-response.schema.json
│   └── fop-insights-response.schema.json
├── reports/
│   ├── list-response.schema.json
│   └── preview-response.schema.json
├── tasks/
│   ├── page-response.schema.json
│   └── grouped-response.schema.json
├── notifications/
│   ├── page-response.schema.json
│   └── summary-response.schema.json
├── forecasts/
│   └── summary-response.schema.json
├── businessguide/
│   └── articles-page-response.schema.json
└── aiaccountant/
    └── health-response.schema.json
```

Legacy flat schemas under `schemas/*.json` remain for smoke tests.

## Not Yet Covered (high priority)

| Domain | Endpoint | Notes |
|--------|----------|-------|
| Imports | GET `/api/imports` | Package reserved; no contract test yet |
| Imports | POST `/api/imports/upload` | Multipart response contract |
| Auth | GET `/api/auth/me` | User profile contract |
| Dashboard | GET `/api/dashboard/summary` | Dashboard aggregate |
| Transactions | POST `/api/transactions` | Create response contract |
| Reports | POST `/api/reports/generate` | Async job contract |
| Tasks | POST `/api/tasks` | Create task contract |
| Business Guide | GET `/api/business-guide/articles/{slug}` | Article detail |
| Business Guide | GET `/api/business-guide/search` | Search response |
| AI Accountant | POST `/api/ai-accountant/chat` | Chat response |
| Forecasts | GET `/api/forecasts/revenue` | Metric endpoints |

## Validation Layers

Each contract test applies:

1. **HTTP status** — expected success code (200/201)
2. **JSON Schema** — via `matchesJsonSchemaInClasspath()` / `JsonSchemaValidator`
3. **Required fields** — `ContractAssertions.assertRequiredFieldsPresent()`
4. **Enum values** — where applicable (`type`, `status`, `severity`, etc.)

## Run Commands

```bash
# Local backend (localhost:8080)
mvn test -Pcontract -Plocal

# Stage
mvn test -Pcontract -Pstage

# Single domain
mvn test -Pcontract -Dtest=TransactionsContractTest
```

## Risks

- Contract tests require a **live backend**; they are not part of CI build gate.
- `POST /auth/register` creates real users on each run.
- Schema drift: update schemas when backend DTOs change.
- Empty arrays skip enum validation (no items to verify).
