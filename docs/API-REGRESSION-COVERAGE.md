# API Regression Test Coverage

Full API regression suite in `com.flowiq.api.regression` — **269 test executions** (198 test methods, 21 parameterized with data providers).

Run against live backend:

```bash
mvn test -Papi-regression -Plocal
mvn test -Papi-regression -Pstage
```

Suite: `regression-api-suite.xml` | Group: `api-regression` | Profile: `-Papi-regression`

## Architecture

| Component | Location |
|-----------|----------|
| Base class | `com.flowiq.api.regression.base.BaseRegressionApiTest` |
| Assertions | `com.flowiq.api.regression.support.RegressionAssertions` |
| Data providers | `com.flowiq.api.regression.support.RegressionDataProviders` |
| Service layer only | `com.flowiq.services.*` (no RestAssured in tests) |

## Module Coverage Summary

| Module | Class | Tests (executions) | Positive | Negative | Business rules |
|--------|-------|-------------------|----------|----------|----------------|
| Auth | `AuthRegressionTest` | ~28 | login, register, me, logout | invalid email/password, duplicate, unauthorized | session persistence |
| Transactions | `TransactionsRegressionTest` | ~39 | CRUD, search, filter, pagination, sort | unauthorized, invalid id/payload, isolation | user isolation |
| Dashboard | `DashboardRegressionTest` | 15 | stats, health, summary, charts, widgets | unauthorized endpoints | KPI + chart data |
| Analytics | `AnalyticsRegressionTest` | 15 | overview, trends, breakdown, FOP | unauthorized | FOP limit insights |
| Imports | `ImportsRegressionTest` | 14 | upload, list, read | invalid/empty file, unauthorized, invalid id | import job status |
| Reports | `ReportsRegressionTest` | ~37 | list, preview, generate (18 combos), download | invalid payload, unauthorized, invalid id | report generation |
| Notifications | `NotificationsRegressionTest` | ~25 | list, summary, unread, mark read, delete | unauthorized, invalid id | read state consistency |
| Tasks | `TasksRegressionTest` | ~35 | CRUD, grouped, today, upcoming | unauthorized, invalid payload/id | task completion |
| Forecasts | `ForecastsRegressionTest` | 19 | revenue, expense, profit, tax, FOP, summary | unauthorized | FOP limits, profit calc |
| Business Guide | `BusinessGuideRegressionTest` | ~28 | list, search, categories, article | invalid slug, unauthorized | article detail read |
| AI Accountant | `AIAccountantRegressionTest` | ~20 | health, recommendations, tax, chat | invalid chat, unauthorized | AI recommendations |
| **Total** | **11 classes** | **~269** | | | |

## Positive Scenarios by Module

### Auth
- Login with valid credentials
- Register new user
- Read `/auth/me`
- Logout and session clear

### Transactions
- Create (income/expense)
- Read by id
- Update fields
- Delete
- List with pagination (pages 0–2, sizes 5–50)
- Sort (amount/date ASC/DESC)
- Filter by type (INCOME/EXPENSE)
- Search by description
- Date range filter
- Summary endpoint

### Dashboard
- Stats, insights, health, AI summary
- Revenue trend, expense breakdown charts
- Forecast/tasks/business-guide snapshots

### Analytics
- Overview, revenue trend, expense breakdown
- Profit trend, income vs expenses, FOP insights

### Imports
- Upload CSV, list jobs, get by id

### Reports
- List history, preview (4 period presets)
- Generate all 6 report types × 3 formats (18 combinations)
- Download, get job by id

### Notifications
- Paginated list, summary, unread count
- Mark single read, mark all read, delete

### Tasks
- Create, update, complete, delete
- Grouped, today, upcoming endpoints
- Pagination and section filters

### Forecasts
- Revenue, expenses, profit, taxes, FOP limit, summary

### Business Guide
- Article list with pagination
- Search (5 queries), categories
- Article detail by slug

### AI Accountant
- Health, recommendations, tax advisor, forecasts, chat

## Negative Scenarios

| Category | Coverage |
|----------|----------|
| Unauthorized (401/403) | All modules — protected endpoints without token |
| Forbidden / isolation | Transactions cross-user access |
| Validation (400/422) | Auth, transactions, tasks, reports, imports, AI chat |
| Invalid IDs (404) | Transactions, tasks, reports, imports, notifications |
| Invalid payloads | Empty/null fields, wrong file types |

## Business Rules

| Rule | Module | Test focus |
|------|--------|------------|
| FOP limits | Analytics, Forecasts | `getFopInsights()`, `getFopLimit()` — limit %, group |
| Report generation | Reports | All type/format combos complete with COMPLETED status |
| Task completion | Tasks | Status COMPLETED + `completedAt` set |
| Notification read state | Notifications | Unread count decreases after mark read |
| Forecast calculations | Forecasts | Summary aligns with metric endpoints |
| AI recommendations | AI Accountant | Recommendations list non-empty structure |

## Data Providers

| Provider | Rows | Used in |
|----------|------|---------|
| `paginationPages` | 3 | Transactions, Tasks, Notifications, Business Guide |
| `pageSizes` | 4 | Transactions, Tasks, Notifications, Business Guide |
| `transactionSorts` | 4 | Transactions |
| `transactionTypes` | 2 | Transactions |
| `invalidEmails` | 5 | Auth |
| `invalidPasswords` | 4 | Auth |
| `reportPeriodPresets` | 4 | Reports |
| `reportTypeFormatCombinations` | 18 | Reports |
| `businessGuideSearchQueries` | 5 | Business Guide |
| `invalidSlugs` | 4 | Business Guide |
| `taskSections` | 4 | Tasks |
| `notificationUnreadFilters` | 2 | Notifications |
| `invalidChatMessages` | 3 | AI Accountant |

## Legacy Tests

Older regression tests remain in `com.flowiq.api.{module}.*RegressionApiTest` for `-Pregression` / `api-regression-suite.xml` compatibility.  
**New development:** use `com.flowiq.api.regression` only.

## Not Covered

- Rate limiting / throttling
- Concurrent write conflicts
- WebSocket / streaming endpoints
- Bulk delete operations
- Admin/superuser roles
