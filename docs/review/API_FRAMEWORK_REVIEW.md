# FlowIQ API Automation Framework — Review

**Date:** 2026-06-28  
**Scope:** API client layer, services, models, auth, serialization, RestAssured configuration, logging, retry, specifications, contract/schema validation, error handling  
**Goal:** Elevate the API automation framework to enterprise-quality standards without changing business functionality.

---

## Executive Summary

The FlowIQ API automation framework follows a **service-layer architecture** on RestAssured with a clear separation between HTTP transport (`clients/`), domain API access (`services/`), typed DTOs (`models/`), and test assertions. The design is sound and aligns with industry practice for Java/TestNG API test suites.

This review applied **12 targeted refactors** to centralize HTTP execution, unify schema paths, fix Allure step placeholders, harden deserialization, and improve log sanitization. Remaining debt is primarily **API–test drift** (4 smoke tests fail against the current local backend) and **structural duplication** (dual schema trees, dual regression suites, thin assertion passthroughs).

### Verification After Refactors

| Suite | Before (main) | After (this review) |
|-------|---------------|---------------------|
| `mvn test-compile` | Pass | Pass |
| `mvn test -Pcontract -Denv=local` (19 tests) | Pass | Pass |
| `mvn test -Papi-smoke -Denv=local` (49 tests) | 8 failures | **4 failures** (pre-existing API drift) |

The 4 remaining smoke failures exist on unmodified `main` and reflect backend behavior that no longer matches negative-test expectations — not regressions introduced by this refactor.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Tests: smoke / regression / contract / integration / security  │
│  BaseApiTest → BaseSmokeApiTest → domain *ApiTest               │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│  Domain Services (13) extending BaseApiService                    │
│  AuthService, TransactionService, DashboardService, …           │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│  ApiRequestExecutor — central HTTP verbs + RetrySupport         │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│  RestAssured                                                      │
│  BaseRequestSpecification (config, headers, filters, Jackson)     │
│  BaseResponseSpecification (status validation, typed extraction)│
│  ApiResponse / ApiCallResult (response wrappers)                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│  TokenManager (ThreadLocal JWT session)                           │
│  ConfigManager → EnvironmentConfig (Owner)                        │
└───────────────────────────────────────────────────────────────────┘
```

---

## Review by Area

### 1. API Clients

| Component | Location | Assessment |
|-----------|----------|------------|
| `ApiRequestExecutor` | `clients/ApiRequestExecutor.java` | **New.** Single entry point for GET/POST/PUT/DELETE/multipart. All domain services route here. |
| `ApiClientFactory` | `clients/ApiClientFactory.java` | Test-lifecycle helper (reset specs, clear tokens). Services bypass it and use `BaseRequestSpecification` directly — acceptable split. |
| `ApiResponse` | `clients/ApiResponse.java` | Thin wrapper over RestAssured `Response`. Deserialization now uses `JsonUtils` for consistent Jackson config. |
| `ApiCallResult<T>` | `clients/ApiCallResult.java` | Status + body + timing for unchecked/negative flows. Body parsed only on 2xx. |
| `PlaywrightFactory` | `clients/PlaywrightFactory.java` | UI scope — outside API framework. |

**Refactors applied:**
- Extracted `ApiRequestExecutor` from duplicated RestAssured calls in `BaseApiService`.
- `ApiResponse.as()` now delegates to `JsonUtils.fromJson()` instead of RestAssured's default mapper (fixes `FAIL_ON_UNKNOWN_PROPERTIES` inconsistency).

---

### 2. Request Builders & Payload Factories

| Component | Location | Role |
|-----------|----------|------|
| `TestDataFactory` | `factories/TestDataFactory.java` | Static builders: login, register, transactions, tasks, reports, AI chat, import CSV files |
| `TransactionRequestBuilder` | `factories/builders/TransactionRequestBuilder.java` | Fluent wrapper over `validTransactionRequest()` |
| `TaskRequestBuilder` | `factories/builders/TaskRequestBuilder.java` | Fluent task builder with date helpers |
| `ReportRequestBuilder` | `factories/builders/ReportRequestBuilder.java` | Fluent report builder |

**Duplication found (documented, not removed):**

| Domain | Factory entry | Builder / service shorthand |
|--------|---------------|----------------------------|
| Transactions | `TestDataFactory.validTransactionRequest()` | `TransactionRequestBuilder.expense()/income()` |
| Tasks | `TestDataFactory.validTaskRequest()` | `TaskRequestBuilder.custom()` |
| Reports | `TestDataFactory.validReportRequest()` | `ReportRequestBuilder.profitAndLossPdf()` |
| AI Chat | `TestDataFactory.chatRequest()` | `AIAccountantService.chat(String)` |
| Login | `TestDataFactory.loginRequest()` | `AuthService.login(String, String)` |

**Refactor applied:** `AuthService.login(String, String)` and `AIAccountantService.chat(String)` now delegate to `TestDataFactory` — single source for default payloads.

**Recommendation:** Keep factory + builder dual entry points (factory for quick defaults, builder for fluent customization). Deprecate service-level payload shorthands only when builders are universally adopted.

---

### 3. Response Models & DTOs

**77 model classes** organized by domain subpackage:

| Subpackage | Count | Examples |
|------------|-------|----------|
| `models/request/` | 8 | `LoginRequest`, `CreateTransactionRequest`, `AIAccountantChatRequest` |
| `models/response/` | ~40 | `StatCardResponse`, `TransactionPageResponse`, shared chart DTOs |
| `models/tasks/` | 12 | Task CRUD, enums, pagination |
| `models/forecasts/` | 14 | Forecast metrics, horizons, tax cards |
| `models/notifications/` | 7 | Notification pages, mark-read request |
| `models/knowledge/` | 6 | Business guide articles, search response |

**Inconsistencies (documented):**

| Issue | Details |
|-------|---------|
| Mixed naming | `*Dto` (knowledge), `*Response` (general), `*Request` (input) |
| Split packages | Tasks/notifications/forecasts domain-local; transactions split request/response |
| Duplicate horizon types | `ForecastHorizonDto` vs `ForecastHorizonResponse` |
| Orphan model | `SendChatMessageResponse` — no request or service reference |
| API fields ahead of models | API returns `vatPayer` on FOP profile; model lacks field — mitigated by `FAIL_ON_UNKNOWN_PROPERTIES` via `JsonUtils` |

**Recommendation:** Adopt a single suffix convention (`*Request` / `*Response`) and consolidate domain packages incrementally. Add missing fields when contract tests require them.

---

### 4. Authentication Handling

| Component | Location | Role |
|-----------|----------|------|
| `TokenManager` | `auth/TokenManager.java` | ThreadLocal JWT session (`accessToken`, `refreshToken`, `UserResponse`) |
| `AuthService` | `services/AuthService.java` | Login/register/logout; persists token; schema-validates login response |
| `BaseRequestSpecification.authenticated()` | `clients/BaseRequestSpecification.java` | Reads token from `TokenManager`, adds `Authorization: Bearer` header |

**Flow:** `AuthService.login()` → POST `/auth/login` → schema validate → `TokenManager.save()` → subsequent calls use authenticated spec.

**Strengths:**
- ThreadLocal isolation supports parallel TestNG execution.
- Clear failure when token missing (`IllegalStateException` with actionable message).
- Login response validated against schema at service layer.

**Gaps (documented):**
- `AuthService.attemptLogin()` and `fetchLogin()` are identical — consolidate in a follow-up.
- `ApiEndpoints` defines `PROFILE_CHANGE_PASSWORD`, `PROFILE_SESSIONS_LOGOUT_*` with no service methods.
- No refresh-token rotation logic in automation layer.

---

### 5. Serialization & Deserialization

| Component | Location | Configuration |
|-----------|----------|---------------|
| `JsonUtils` | `utils/JsonUtils.java` | Shared `ObjectMapper`: JavaTime module, no timestamp dates, **ignore unknown properties** |
| RestAssured config | `BaseRequestSpecification` static block | `jackson2ObjectMapperFactory` → `JsonUtils.mapper()` |
| `ApiResponse.as()` | `clients/ApiResponse.java` | **Fixed:** uses `JsonUtils.fromJson()` for all typed extraction |

**Issue fixed:** RestAssured's `response.as(Class)` did not consistently honor the configured mapper, causing `UnrecognizedPropertyException` on `FopProfileResponse` when API added `vatPayer`. Centralizing through `JsonUtils` ensures tolerant deserialization everywhere.

---

### 6. RestAssured Configuration

`BaseRequestSpecification.buildBase()` configures:

| Setting | Value |
|---------|-------|
| Base URI | `ConfigManager.getConfig().apiUrl()` |
| Content-Type | `application/json` |
| Accept | JSON |
| Headers | `Accept-Language: uk`, `Accept-Currency: UAH` |
| Filters | `RequestLoggingFilter`, `ResponseLoggingFilter`, `AllureRestAssured` |
| HTTPS | Relaxed validation (local/dev) |
| Object mapper | `JsonUtils.mapper()` via global `RestAssured.config` |

**Variants:**
- `base()` — unauthenticated JSON
- `authenticated()` — base + Bearer token
- `multipart()` — authenticated + `multipart/form-data`

**Recommendation:** Consider environment-gated HTTPS validation (strict in staging/prod profiles).

---

### 7. Logging Filters

| Filter | Location | Behavior |
|--------|----------|----------|
| `RequestLoggingFilter` | `clients/filters/RequestLoggingFilter.java` | Logs method, URI, headers, body; attaches sanitized copy to Allure |
| `ResponseLoggingFilter` | `clients/filters/ResponseLoggingFilter.java` | Logs status, time, body; attaches sanitized copy to Allure |
| `ApiLogSanitizer` | `clients/filters/ApiLogSanitizer.java` | **New.** Redacts passwords, tokens, Bearer values from logs and Allure |

**Refactor applied:** Both filters delegate sanitization to `ApiLogSanitizer` — enterprise requirement for CI log safety.

---

### 8. Retry Strategy

| Component | Location | Behavior |
|-----------|----------|----------|
| `RetrySupport` | `support/RetrySupport.java` | Retries on 408, 429, 500, 502, 503, 504 (3 attempts, 1s delay) |
| `ApiRequestExecutor` | `clients/ApiRequestExecutor.java` | All HTTP calls wrapped in `RetrySupport.executeApi()` |
| `InfrastructureRetryAnalyzer` | `listeners/InfrastructureRetryAnalyzer.java` | TestNG-level retry for `RetryableApiException` |

**Refactor applied:** `executeApi()` now **returns the final response** after exhausted retries instead of throwing `RetryableApiException`. This allows negative tests to assert on 4xx/5xx status codes while still retrying transient failures on happy-path calls.

**Recommendation:** Consider removing `500` from retryable codes — application errors are often not transient. Retain 502/503/504/429/408 only.

---

### 9. Reusable Request Specifications

| Method | Auth | Content-Type | Use Case |
|--------|------|--------------|----------|
| `BaseRequestSpecification.base()` | No | JSON | Public endpoints, login/register |
| `BaseRequestSpecification.authenticated()` | Bearer JWT | JSON | All protected API calls |
| `BaseRequestSpecification.multipart()` | Bearer JWT | multipart/form-data | CSV import upload |

All specs share base URI, locale headers, logging filters, and Jackson config. Specs are lazily initialized singletons with `reset()` for test teardown.

---

### 10. Reusable Response Specifications

| Method | Expected Status | Use Case |
|--------|-----------------|----------|
| `BaseResponseSpecification.ok()` | 200 | Standard GET/PUT |
| `BaseResponseSpecification.created()` | 201 | POST create |
| `BaseResponseSpecification.noContent()` | 204 | DELETE |
| `BaseResponseSpecification.unauthorized()` | 401 | Auth negative tests |
| `BaseResponseSpecification.badRequest()` | 400 | Validation negative tests |
| `BaseResponseSpecification.validateAnyOf()` | Custom set | Flexible status assertion |

Typed extraction helpers: `extractOk()`, `extractCreated()`, `extract()`.

**Refactor applied:** Removed invalid Allure `@Step` placeholders (`{type.simpleName}`, `{file.name}`) that caused `NoSuchFieldException` at runtime in `BaseResponseSpecification` and `ImportService`.

---

### 11. Contract Validation

**Location:** `src/test/java/com/flowiq/contracts/` — 12 contract test classes, 19 test methods.

**Pattern:**
```
BaseContractTest → service.fetch*() → ContractAssertions.assertAllRequired(result, status, schema, fields...)
```

**Coverage:**

| Domain | Endpoints | Schema |
|--------|-----------|--------|
| Auth | login, register, me | `SchemaPaths.AUTH_*` |
| Dashboard | stats | `DASHBOARD_STATS` |
| Profile | GET profile | `PROFILE` |
| Transactions | list, summary + enum | `TRANSACTIONS_*` |
| Tasks | page, grouped | `TASKS_*` |
| Imports | list | `IMPORTS_LIST` |
| Analytics | overview, fop-insights | `ANALYTICS_*` |
| Reports | list, preview | `REPORTS_*` |
| Notifications | page, summary | `NOTIFICATIONS_*` |
| Forecasts | summary | `FORECASTS_SUMMARY` |
| Business Guide | articles | `BUSINESS_GUIDE_ARTICLES` |
| AI Accountant | health | `AI_ACCOUNTANT_HEALTH` |

**Gaps (documented):** No contract tests for settings, individual forecast endpoints, AI chat, report generate/download, profile update/FOP, dashboard widgets beyond stats, business guide search/categories.

**Refactor applied:** `ContractAssertions.assertSchemaValid()` delegates to `ApiAssertions.assertMatchesSchema()` — single schema validation path.

---

### 12. Schema Validation

| Component | Location | Role |
|-----------|----------|------|
| `SchemaPaths` | `constants/SchemaPaths.java` | **New.** Single source for legacy flat + canonical organized schema paths |
| `SmokeSchemas` | `test/.../constants/SmokeSchemas.java` | Delegates to `SchemaPaths.*_LEGACY` |
| `ContractSchemas` | `test/.../contracts/ContractSchemas.java` | Delegates to `SchemaPaths` canonical paths |
| `JsonSchemaValidator` | `validation/JsonSchemaValidator.java` | RestAssured JSON Schema Validator; resolves legacy vs `schemas/` prefix |

**Dual schema tree (documented debt):**

| Tree | Location | Used By |
|------|----------|---------|
| Legacy flat | `schemas/auth-login-response-schema.json`, etc. (12 files) | Smoke tests, `AuthService.login()` |
| Canonical organized | `schemas/auth/login-response.schema.json`, etc. (20 files) | Contract tests |

**Recommendation:** Migrate smoke tests to canonical paths and delete legacy flat files in a dedicated PR.

---

### 13. Error Handling

| Layer | Mechanism |
|-------|-----------|
| HTTP transport | `RetrySupport` retries transient status codes; returns final response |
| Status validation | `BaseResponseSpecification.validate()` / `validateAnyOf()` throw `AssertionError` with body |
| Typed extraction | `extractOk()` validates status before deserialization |
| Unchecked flows | `ApiCallResult.from()` — body null on non-2xx; status preserved for assertions |
| Assertions | `ApiAssertions.assertValidationError()` expects 400 or 422 |
| TestNG retry | `InfrastructureRetryAnalyzer` for infrastructure failures |

**Assertion centralization:**

| Class | Role |
|-------|------|
| `ApiAssertions` | Canonical HTTP outcome methods (status, JSON fields, schema, timing, polling) |
| `RegressionAssertions` | Passthrough to `ApiAssertions` |
| `IntegrationAssertions` | Thin wrapper + `assertSuccess(status)` |
| `ContractAssertions` | Schema + required fields + enum validation |
| `BaseSmokeApiTest` | Protected convenience wrappers |

---

## Domain Services Inventory

| Service | Endpoints | Pattern |
|---------|-----------|---------|
| `AuthService` | `/auth/login`, `/auth/register`, `/auth/me`, `/auth/logout` | Login + token persistence + schema validation |
| `DashboardService` | stats, insights, health, summary, charts, widgets | List getters + fetch* unchecked |
| `TransactionService` | CRUD, list, search, summary | Full CRUD + attempt/fetch |
| `ImportService` | upload (multipart), list, get by id | Multipart + fetch* |
| `AnalyticsService` | overview, trends, FOP insights | Typed getters + fetch* |
| `ReportService` | list, preview, generate, download | CRUD-like + binary download |
| `NotificationService` | list, unread count, summary, mark read, delete | Pagination + raw jsonPath for counts |
| `TaskService` | CRUD, today, upcoming, grouped, suggestions | Full CRUD + attempt/fetch |
| `ForecastService` | revenue, expenses, profit, taxes, FOP limit, summary | Typed getters + fetch* |
| `BusinessGuideService` | articles, categories, search, snapshot | Search + slug lookup |
| `AIAccountantService` | health, recommendations, tax advisor, forecasts, chat | AI domain endpoints |
| `ProfileService` | profile, FOP profile, sessions | Partial CRUD |
| `SettingsService` | notification preferences | Fetch-only stub |

---

## Duplication Audit

### Duplicated Endpoints Across Services

| Concept | Dashboard | Analytics / Business Guide | Same DTO |
|---------|-----------|---------------------------|----------|
| Revenue trend | `/dashboard/charts/revenue-trend` | `/analytics/revenue-trend` | `List<MonthlyAmountResponse>` |
| Expense breakdown | `/dashboard/charts/expense-breakdown` | `/analytics/expense-breakdown` | `List<CategoryAmountResponse>` |
| Business guide snapshot | `/dashboard/business-guide-snapshot` | `/business-guide/dashboard-snapshot` | `KnowledgeDashboardSnapshotDto` |

Both snapshot paths implemented in `DashboardService` and `BusinessGuideService` — likely backend alias endpoints.

### Duplicated Assertions

| Duplication | Action |
|-------------|--------|
| `RegressionAssertions` → 100% delegation to `ApiAssertions` | **Documented** — keep as stable import for regression package |
| `BaseSmokeApiTest` re-exposes 4 `ApiAssertions` methods | **Acceptable** — reduces boilerplate in smoke tests |
| `BaseContractTest.ensureAuthenticated()` duplicates parent | **Documented** — consolidate in follow-up |
| `AuthRegressionApiTest` vs `AuthRegressionTest` overlap | **Documented** — part of dual regression suite debt |

### Endpoint Constants Without Services

`ApiEndpoints` defines paths with no service implementation: `HEALTH`, `HEALTH_PING`, `PROFILE_CHANGE_PASSWORD`, `PROFILE_SESSIONS_LOGOUT_*`.

---

## Refactors Applied

| # | Area | Change | Benefit |
|---|------|--------|---------|
| 1 | HTTP execution | New `ApiRequestExecutor` — central RestAssured verbs | Single place for retry, logging, future interceptors |
| 2 | BaseApiService | Delegates all HTTP to `ApiRequestExecutor` | Removed ~80 lines of duplicated `given().spec()` blocks |
| 3 | Deserialization | `ApiResponse.as()` uses `JsonUtils.fromJson()` | Consistent Jackson config; tolerant of new API fields |
| 4 | Schema paths | New `SchemaPaths` constant class | Single source for legacy + canonical schema locations |
| 5 | Smoke/contract schemas | `SmokeSchemas`, `ContractSchemas` delegate to `SchemaPaths` | No divergent path strings |
| 6 | Log sanitization | New `ApiLogSanitizer`; filters use it | Passwords/tokens redacted from logs and Allure |
| 7 | Allure steps | Fixed invalid `{type.simpleName}`, `{file.name}` placeholders | Eliminated 3 runtime `NoSuchFieldException` failures |
| 8 | Retry behavior | `executeApi()` returns final response after retries | Negative tests can assert on 4xx/5xx instead of catching exceptions |
| 9 | Contract assertions | Schema validation delegates to `ApiAssertions` | One validation implementation |
| 10 | Payload builders | `AuthService`, `AIAccountantService` use `TestDataFactory` | Single source for default request bodies |
| 11 | ApiAssertions | Uses `response.jsonPath()` helper | Consistent jsonPath access |
| 12 | Dead code | Removed unused `SendChatMessageRequest.java` (prior review) | Cleaner model surface |

---

## Pre-Existing Smoke Test Failures (API Drift)

These 4 tests fail on both unmodified `main` and after this refactor against the current local backend:

| Test | Expected | Actual | Root Cause |
|------|----------|--------|------------|
| `NotificationsSmokeApiTest.shouldRejectInvalidPagination` | 400/422 | 200 | Backend accepts `page=-1, size=0` and returns first page |
| `BusinessGuideSmokeApiTest.shouldRejectSearchWithoutQuery` | 400/422 | 200 | Backend returns full search results when `query` is absent |
| `ForecastsSmokeApiTest.shouldReturnErrorForUnknownEndpoint` | 400/404 | 500 | Unknown sub-resource triggers unhandled server exception |
| `ImportsSmokeApiTest.shouldRejectInvalidFileUpload` | 400/422 | 500 | Non-CSV upload triggers unhandled server exception |

**Resolution options (require backend or test-spec change — outside scope of framework refactor):**
1. Backend team restores strict validation (400/422) for invalid inputs.
2. Update smoke test expectations to match current API behavior (product decision).
3. Mark tests `@Test(enabled = false)` with linked defect until API is fixed.

---

## Recommendations (Follow-Up PRs)

| Priority | Item | Effort |
|----------|------|--------|
| High | Fix 4 API drift smoke failures with backend team | Backend |
| High | Consolidate dual schema trees → canonical `schemas/` only | Medium |
| Medium | Retire legacy `*RegressionApiTest` suite in favor of `api/regression/*` | Large |
| Medium | Consolidate `AuthService.attemptLogin()` / `fetchLogin()` | Small |
| Medium | Add `vatPayer` to `FopProfileResponse` when contract test covers FOP profile | Small |
| Low | Remove `500` from retryable status codes | Small |
| Low | Implement service methods for orphan `ApiEndpoints` constants | Medium |
| Low | Typed DTOs for notification count/updated instead of raw `jsonPath().getInt()` | Small |
| Low | Lazy service wiring in `BaseApiTest` | Medium |

---

## File Reference

| Area | Path |
|------|------|
| HTTP executor | `src/main/java/com/flowiq/clients/ApiRequestExecutor.java` |
| Request specs | `src/main/java/com/flowiq/clients/BaseRequestSpecification.java` |
| Response specs | `src/main/java/com/flowiq/clients/BaseResponseSpecification.java` |
| Response wrapper | `src/main/java/com/flowiq/clients/ApiResponse.java` |
| Unchecked result | `src/main/java/com/flowiq/clients/ApiCallResult.java` |
| Log sanitizer | `src/main/java/com/flowiq/clients/filters/ApiLogSanitizer.java` |
| Service base | `src/main/java/com/flowiq/services/BaseApiService.java` |
| Auth | `src/main/java/com/flowiq/auth/TokenManager.java` |
| JSON utils | `src/main/java/com/flowiq/utils/JsonUtils.java` |
| Schema validator | `src/main/java/com/flowiq/validation/JsonSchemaValidator.java` |
| Schema paths | `src/main/java/com/flowiq/constants/SchemaPaths.java` |
| Endpoints | `src/main/java/com/flowiq/constants/ApiEndpoints.java` |
| Assertions | `src/main/java/com/flowiq/assertions/ApiAssertions.java` |
| Retry | `src/main/java/com/flowiq/support/RetrySupport.java` |
| Test data | `src/main/java/com/flowiq/factories/TestDataFactory.java` |
| Contract tests | `src/test/java/com/flowiq/contracts/` |
| Schema resources | `src/test/resources/schemas/` |

---

## Conclusion

The FlowIQ API automation framework is **well-structured and production-ready** at the service-layer level. This review strengthened the infrastructure layer (centralized HTTP execution, consistent deserialization, log sanitization, unified schema paths) without altering test intent or business assertions.

Contract tests (19/19) pass. API smoke tests improved from 8 to 4 failures, with remaining failures attributable to backend behavior drift documented above. The recommended follow-up work focuses on schema consolidation, regression suite deduplication, and coordinating with the backend team on validation semantics.
