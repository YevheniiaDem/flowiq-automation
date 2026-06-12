# API Integration Test Coverage

Full business lifecycle integration tests against a **live backend** (no mocks).  
DB consistency tests additionally require backend connected to Testcontainers PostgreSQL.

## Run Commands

```bash
# All API integration tests (live backend)
mvn test -Papi-integration -Plocal

# Stage
mvn test -Papi-integration -Pstage

# Single module
mvn test -Papi-integration -Dtest=TransactionsIntegrationTest
```

DB consistency subset (`*IntegrationDbTest`) runs in the same suite when Docker + backend-on-test-DB are available; otherwise those tests skip.

## Module Coverage

### Transactions (`TransactionsIntegrationTest` + `TransactionsIntegrationDbTest`)

| Scenario | Type | Test |
|----------|------|------|
| Create transaction | Positive | `shouldCreateTransaction` |
| Get by id | Positive | `shouldGetTransactionById` |
| Update | Positive | `shouldUpdateTransaction` |
| Delete | Positive | `shouldDeleteTransaction` |
| Pagination | Positive | `shouldPaginateTransactions` |
| Sorting ASC | Positive | `shouldSortTransactionsAscending` |
| Sorting DESC | Positive | `shouldSortTransactionsDescending` |
| Search | Positive | `shouldSearchTransactionsByDescription` |
| Date filters | Positive | `shouldFilterTransactionsByDateRange` |
| Validation errors | Negative | `shouldRejectInvalidTransactionPayload` |
| Unauthorized access | Auth | `shouldRejectUnauthorizedAccess` |
| User isolation | Security | `shouldEnforceUserIsolation` |
| API ↔ DB sync | Testcontainers | `shouldPersistCreatedTransactionInDatabase` |

### Tasks (`TasksIntegrationTest` + `TasksIntegrationDbTest`)

| Scenario | Type | Test |
|----------|------|------|
| Create | Positive | `shouldCreateTask` |
| Update | Positive | `shouldUpdateTask` |
| Complete | Positive | `shouldCompleteTask` |
| Delete | Positive | `shouldDeleteTask` |
| Grouped endpoint | Positive | `shouldReturnGroupedTasks` |
| Today endpoint | Positive | `shouldReturnTodayTasks` |
| Upcoming endpoint | Positive | `shouldReturnUpcomingTasks` |
| Overdue logic | Positive | `shouldIncludeOverdueTasksInGroupedResponse` |
| Validation errors | Negative | `shouldRejectInvalidTaskPayload` |
| Unauthorized access | Auth | `shouldRejectUnauthorizedAccess` |
| API ↔ DB sync | Testcontainers | `shouldPersistCompletedTaskStatusInDatabase` |

### Notifications (`NotificationsIntegrationTest` + `NotificationsIntegrationDbTest`)

| Scenario | Type | Test |
|----------|------|------|
| Mark read | Positive | `shouldMarkSingleNotificationAsRead` |
| Mark all read | Positive | `shouldMarkAllNotificationsAsRead` |
| Delete | Positive | `shouldDeleteNotification` |
| Unread count | Positive | `shouldReturnUnreadCount` |
| Summary validation | Positive | `shouldReturnValidNotificationSummary` |
| Unauthorized access | Auth | `shouldRejectUnauthorizedAccess` |
| API ↔ DB sync | Testcontainers | `shouldSyncMarkAllReadWithDatabase` |

### Reports (`ReportsIntegrationTest` + `ReportsIntegrationDbTest`)

| Scenario | Type | Test |
|----------|------|------|
| Generate CSV | Positive | `shouldGenerateCsvReport` |
| Generate PDF | Positive | `shouldGeneratePdfReport` |
| Generate Excel | Positive | `shouldGenerateExcelReport` |
| Download | Positive | `shouldDownloadGeneratedReport` |
| Report history | Positive | `shouldReturnReportHistory` |
| Invalid generation | Negative | `shouldRejectInvalidReportGenerationRequest` |
| Invalid preview period | Negative | `shouldHandleInvalidPreviewDateRange` |
| Unauthorized access | Auth | `shouldRejectUnauthorizedAccess` |
| API ↔ DB sync | Testcontainers | `shouldPersistGeneratedReportInDatabase` |

### Imports (`ImportsIntegrationTest` + `ImportsIntegrationDbTest`)

| Scenario | Type | Test |
|----------|------|------|
| Upload CSV | Positive | `shouldUploadCsvFile` |
| Duplicate import | Positive | `shouldAllowDuplicateImportUpload` |
| Invalid file | Negative | `shouldRejectInvalidImportFile` |
| Empty file | Negative | `shouldRejectEmptyImportFile` |
| Import history | Positive | `shouldReturnImportHistory` |
| Unauthorized access | Auth | `shouldRejectUnauthorizedAccess` |
| API ↔ DB sync | Testcontainers | `shouldPersistImportJobInDatabase` |

### Forecasts (`ForecastsIntegrationTest`)

| Scenario | Type | Test |
|----------|------|------|
| Revenue forecast | Positive | `shouldReturnRevenueForecast` |
| Expense forecast | Positive | `shouldReturnExpenseForecast` |
| Profit forecast | Positive | `shouldReturnProfitForecast` |
| FOP limit forecast | Positive | `shouldReturnFopLimitForecast` |
| Tax forecast | Positive | `shouldReturnTaxForecast` |
| Data consistency | Positive | `shouldKeepSummaryConsistentWithMetrics` |
| Unauthorized access | Auth | `shouldRejectUnauthorizedAccess` |

## Summary

| Module | API Tests | DB Tests | Total |
|--------|-----------|----------|-------|
| Transactions | 12 | 1 | 13 |
| Tasks | 10 | 1 | 11 |
| Notifications | 6 | 1 | 7 |
| Reports | 8 | 1 | 9 |
| Imports | 6 | 1 | 7 |
| Forecasts | 7 | 0 | 7 |
| **Total** | **49** | **5** | **54** |

## Infrastructure

| Component | Location |
|-----------|----------|
| Base class (live API) | `com.flowiq.api.integration.base.BaseApiIntegrationTest` |
| Base class (Testcontainers) | `com.flowiq.api.integration.base.BaseApiIntegrationDbTest` |
| Assertions | `com.flowiq.api.integration.support.IntegrationAssertions` |
| Import helper | `com.flowiq.api.integration.support.ImportIntegrationSupport` |
| TestNG suite | `testng/api-integration-suite.xml` |
| Maven profile | `-Papi-integration` |

## Not Yet Covered

- Forecasts DB consistency (read-only API, no persistence layer)
- Cross-module E2E flows (covered in `com.flowiq.e2e`)
- Bulk operations / concurrent updates
- Rate limiting and performance SLAs

## Prerequisites

- **Live API tests:** running `flowiq-backend` + valid credentials in `environments/{env}.properties`
- **DB tests:** Docker + backend with `spring.datasource.url` = Testcontainer JDBC URL printed at suite start
