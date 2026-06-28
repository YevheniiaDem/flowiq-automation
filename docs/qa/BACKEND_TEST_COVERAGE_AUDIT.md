# FlowIQ Backend — Test Coverage Audit

**Project:** `flowiq-backend` (`c:\Users\YevheniiaDemchuk\IdeaProjects\flowiq-backend`)  
**Stack:** Spring Boot 3.5.14, Java 17, PostgreSQL, JWT, Flyway  
**Audit date:** 2026-06-26  
**Audit scope:** `src/main/java` vs `src/test/java` (фактический код)

> **Примечание:** репозиторий `flowiq-automation` — это API/UI automation framework (TestNG + Rest Assured).  
> Backend unit/integration тесты (JUnit 5, MockMvc, Testcontainers) живут в **`flowiq-backend`**.

---

## 1. Executive Summary

| Метрика | До аудита | После реализации (текущее) |
|---------|-----------|----------------------------|
| Тестовых классов | 17 | 26 |
| Тестов (Surefire) | ~115 (оценка, часть не компилировалась) | **176** |
| Instruction coverage (JaCoCo) | ~35% (оценка) | **49.2%** |
| Testcontainers | ❌ | ✅ PostgreSQL 16 |
| Controllers с тестами | 1 partial (Auth refresh) | 4 (+ partial Auth refresh legacy) |
| Repositories с IT-тестами | 0 | 1 (TransactionRepository) |

**Вывод:** до работ было сильное покрытие rule engines и части сервисов, но **почти нулевое покрытие REST-слоя, репозиториев и security integration**. После доработки база стала production-viable для CI, но требуются controller/service тесты для оставшихся 10 контроллеров.

---

## 2. Покрытие по слоям (JaCoCo, instruction %)

| Слой | Классов | Покрыто тестами | Instruction coverage | Статус |
|------|---------|-----------------|----------------------|--------|
| **Controllers** | 14 | 4 | ~18% | 🔴 Критичный gap |
| **Services (@Service)** | 28 | 14 | ~52% | 🟡 Частично |
| **Repositories** | 12 | 1 (IT) | ~5% (runtime через mocks) | 🔴 |
| **Security** | 6 core | 3 | ~55% | 🟡 |
| **Utils / helpers** | 7 key | 4 | ~60% | 🟡 |
| **Rule engines / renderers** | 8 | 8 | ~85% | 🟢 |
| **Overall** | 135 analyzed | — | **49.2%** | 🟡 |

---

## 3. Существующие тесты (до доработки)

| Test class | Что покрывает |
|------------|---------------|
| `FlowiqBackendApplicationTests` | Context load |
| `AuthControllerRefreshTest` | `POST /api/auth/refresh` (2 кейса) |
| `AuthServiceTest` | `AuthService.refresh()` (5 кейсов) — **устарел, не компилировался** |
| `JwtServiceTest` | JWT generate/validate/expiry |
| `ForecastServiceTest` / `ForecastEngineTest` | Forecasts |
| `ReportsServiceTest` | Reports CRUD/generate |
| `KnowledgeServiceTest` | Business guide |
| `TaskRuleEngineTest` | Task rules |
| `NotificationRuleEngineTest` | Notification rules |
| `AIRecommendationEngineTest` | AI recommendations |
| `AuditServiceTest` / `AuditMetadata*` | Audit |
| `PoiReportRendererTest` / `OpenPdfReportRendererTest` | Report renderers |

---

## 4. Controllers — покрытие endpoints

| Controller | Endpoints | Тесты | Покрытые сценарии |
|------------|-----------|-------|-------------------|
| `AuthController` | 5 | ✅ Partial | register, login, refresh, me, logout, validation |
| `HealthController` | 2 | ✅ | success |
| `TransactionController` | 6 | ✅ Partial | CRUD, summary, validation, 401/404 |
| `DashboardController` | 9 | ❌ | — |
| `AnalyticsController` | 6 | ❌ | — |
| `ImportController` | 3 | ❌ | — |
| `ReportsController` | 5 | ❌ | — |
| `NotificationController` | 6 | ❌ | — |
| `NotificationPreferenceController` | 3 | ❌ | — |
| `TaskController` | 9 | ❌ | — |
| `ForecastController` | 6 | ❌ | — |
| `BusinessGuideController` | 5 | ❌ | — |
| `AIAccountantController` | 5 | ❌ | — |
| `ChatController` | 3 | ❌ | — |
| `ProfileController` | 10 | ❌ | — |

**Endpoint coverage:** ~15 / 76 (~20%)

---

## 5. Services — покрытие

| Service | Unit tests | Integration | Статус |
|---------|------------|-------------|--------|
| `AuthService` | ✅ Extended | Security IT (register) | 🟢 |
| `JwtService` | ✅ | — | 🟢 |
| `ForecastService` | ✅ | — | 🟢 |
| `ReportsService` | ✅ | — | 🟢 |
| `KnowledgeService` | ✅ | — | 🟢 |
| `TransactionService` | ✅ NEW | — | 🟡 |
| `ImportService` | ✅ NEW | — | 🟡 |
| `NotificationService` | ✅ NEW | — | 🟡 |
| `TaskRuleEngine` | ✅ | — | 🟢 |
| `NotificationRuleEngine` | ✅ | — | 🟢 |
| `AuditServiceImpl` | ✅ Fixed | — | 🟡 |
| `AIRecommendationEngine` | ✅ | — | 🟢 |
| `ProfileService` | ❌ | — | 🔴 |
| `FopProfileService` | ❌ | partial via register IT | 🔴 |
| `SessionService` | ❌ | — | 🔴 |
| `AvatarStorageService` | ❌ | — | 🔴 |
| `DashboardService` | ❌ | — | 🔴 |
| `AnalyticsService` | ❌ | — | 🔴 |
| `AIAccountantService` | ❌ | — | 🔴 |
| `ChatService` | ❌ | — | 🔴 |
| `TaskService` | ❌ | — | 🔴 |
| `TaskGeneratorService` | ❌ | — | 🔴 |
| `NotificationGeneratorService` | ❌ | — | 🔴 |
| `NotificationPreferenceService` | ❌ | — | 🔴 |
| `TransactionSeedService` | ❌ | — | 🔴 |
| `TransactionInsightService` | ❌ | — | 🔴 |
| `CustomUserDetailsService` | ❌ | — | 🔴 |
| `CategorizationEngine` | ❌ | — | 🔴 |
| CSV strategies (Monobank/Privat/Universal) | Universal only | — | 🟡 |

---

## 6. Repositories — покрытие

| Repository | CRUD | JPQL/Native | Pagination | IT Test |
|------------|------|-------------|------------|---------|
| `TransactionRepository` | ✅ IT | ✅ IT | via Spec IT | ✅ |
| `UserRepository` | partial IT | — | — | via IT setup |
| `TaskRepository` | ❌ | ❌ | ❌ | ❌ |
| `NotificationRepository` | ❌ | ❌ | ❌ | ❌ |
| `ImportJobRepository` | ❌ | ❌ | ❌ | ❌ |
| `ReportJobRepository` | ❌ | ❌ | ❌ | ❌ |
| `KnowledgeArticleRepository` | ❌ | ❌ | ❌ | ❌ |
| `FopProfileRepository` | ❌ | ❌ | — | ❌ |
| `UserSessionRepository` | ❌ | ❌ | — | ❌ |
| `NotificationPreferenceRepository` | ❌ | — | — | ❌ |
| `ChatConversationRepository` | ❌ | — | — | ❌ |
| `AuditLogRepository` | ❌ | — | — | ❌ |

---

## 7. Security — покрытие

| Компонент | Покрытие | Сценарии |
|-----------|----------|----------|
| `JwtService` | ✅ Unit | access/refresh, expiry, invalid signature |
| `AuthService` | ✅ Unit | register, login, refresh, logout, me |
| `AuthController` | ✅ MockMvc | public/protected endpoints, validation |
| `SecurityConfig` | ✅ Integration | 403 без JWT, public health/auth |
| `JwtAuthenticationFilter` | 🟡 Integration | косвенно через Security IT |
| `CorsConfig` | ✅ Unit | origins, methods, headers |
| `CustomUserDetailsService` | ❌ | — |
| `UserPrincipal` | 🟡 | через service tests |
| Session management | 🟡 | AuthService unit (logout) |
| RBAC | ❌ | только USER role в тестах |
| Password change | ❌ | ProfileController не покрыт |

---

## 8. Utils / Validation

| Class | Tests | Edge cases |
|-------|-------|------------|
| `TransactionDateValidator` | ✅ | null, future, before 2000 |
| `CurrencyFormatter` | ✅ | UAH/USD/EUR |
| `CsvLineParser` | ✅ via UniversalCsvStrategy | quoted fields, bad columns |
| `UniversalCsvStrategy` | ✅ | empty CSV, invalid category, missing columns |
| `MonobankCsvStrategy` | ❌ | — |
| `PrivatBankCsvStrategy` | ❌ | — |
| `UserAgentParser` | ❌ | — |
| `AuditMetadataBuilder/Sanitizer` | ✅ | sensitive keys |
| DTO Bean Validation | 🟡 | Auth + Transaction controllers |

---

## 9. Отсутствующие edge cases (приоритет)

| # | Сценарий | Приоритет |
|---|----------|-----------|
| 1 | Expired JWT / invalid refresh (E2E security) | P0 |
| 2 | Profile password change + session revoke | P0 |
| 3 | Import: corrupted CSV, empty rows, 10MB boundary | P0 |
| 4 | Transaction duplicate detection on import | P1 |
| 5 | RBAC / forbidden (ADMIN vs USER) | P1 |
| 6 | Notification preference disabled → no notification | P1 |
| 7 | Concurrent session refresh (race) | P2 |
| 8 | Report download content-type / file stream | P1 |
| 9 | AI Accountant chat timeout / empty message | P2 |
| 10 | Flyway migration rollback scenarios | P3 |

---

## 10. Матрица Controller → Service → Tests

| Controller | Service(s) | Unit Service | Controller Test | Integration |
|------------|------------|--------------|-----------------|-------------|
| `AuthController` | `AuthService`, `SessionService`, `JwtService` | ✅ AuthService | ✅ AuthController | ✅ Security IT |
| `HealthController` | — | — | ✅ | ✅ Security IT |
| `TransactionController` | `TransactionService` | ✅ | ✅ | ❌ |
| `ImportController` | `ImportService` | ✅ | ❌ | ❌ |
| `DashboardController` | `DashboardService` | ❌ | ❌ | ❌ |
| `AnalyticsController` | `AnalyticsService` | ❌ | ❌ | ❌ |
| `ReportsController` | `ReportsService` | ✅ | ❌ | ❌ |
| `NotificationController` | `NotificationService` | ✅ | ❌ | ❌ |
| `NotificationPreferenceController` | `NotificationPreferenceService` | ❌ | ❌ | ❌ |
| `TaskController` | `TaskService` | ❌ | ❌ | ❌ |
| `ForecastController` | `ForecastService` | ✅ | ❌ | ❌ |
| `BusinessGuideController` | `KnowledgeService` | ✅ | ❌ | ❌ |
| `AIAccountantController` | `AIAccountantService` | ❌ | ❌ | ❌ |
| `ChatController` | `ChatService` | ❌ | ❌ | ❌ |
| `ProfileController` | `ProfileService`, `SessionService`, `FopProfileService` | ❌ | ❌ | ❌ |

---

## 11. Приоритет реализации

### P0 — блокеры production CI
1. Controller MockMvc для Import, Reports, Notifications, Tasks, Profile
2. `ProfileService` + password change + session tests
3. Repository IT: Task, Notification, ImportJob, UserSession
4. Security: expired JWT, invalid refresh integration

### P1 — regression safety
5. `DashboardService`, `AnalyticsService` unit tests
6. `AIAccountantService`, `ChatService` unit tests
7. Monobank/PrivatBank CSV strategy tests
8. `NotificationPreferenceService` tests

### P2 — polish
9. `@WebMvcTest` slice tests для быстрого CI
10. Contract tests (OpenAPI) aligned with controllers
11. Performance tests for large CSV import

---

## 12. Инфраструктура тестов

| Компонент | Статус |
|-----------|--------|
| JUnit 5 | ✅ |
| Mockito | ✅ |
| AssertJ | ✅ |
| Spring Boot Test | ✅ |
| MockMvc | ✅ |
| spring-security-test | ✅ |
| Testcontainers PostgreSQL | ✅ NEW |
| JaCoCo | ✅ |
| H2 | ❌ не используется (PostgreSQL-specific) |

**Test profile:** `src/test/resources/application-test.properties`  
**Base IT class:** `com.flowiq.integration.support.AbstractPostgresIntegrationTest`

---

*Аудит выполнен по фактическому коду `flowiq-backend` на 2026-06-26.*
