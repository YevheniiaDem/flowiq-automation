# FlowIQ Test Data Strategy

| Field | Value |
|-------|-------|
| **Document ID** | QA-DAT-001 |
| **Version** | 1.0 |
| **Last updated** | 2026-06-28 |

---

## 1. Purpose

Defines how test data is created, managed, isolated, and cleaned up across FlowIQ automation suites.

## 2. Data Sources

| Source | Location | Use |
|--------|----------|-----|
| Environment config | `environments/*.properties` | Default login credentials |
| Test data factory | `com.flowiq.factories.TestDataFactory` | Requests, files, random users |
| Random generator | `com.flowiq.utils.RandomDataGenerator` | Emails, passwords, strings |
| DataFaker | `net.datafaker` | Names, companies |
| Builders | `com.flowiq.factories.builders.*` | Fluent request construction |
| Static fixtures | `src/test/resources/testdata/` | CSV samples, JSON payloads |
| Seeded demo data | Backend seed / demo workspace | Dashboard, analytics with data |

## 3. User Accounts

| User type | Credentials | Used by |
|-----------|-------------|---------|
| **Demo user** | `demo@flowiq.ai` / `demo123` (local/ci) | Smoke, regression, most UI tests |
| **Stage/dev user** | `TEST_USER_EMAIL` / `TEST_USER_PASSWORD` secrets | Manual smoke workflows |
| **Ephemeral register** | `TestDataFactory.randomRegisterRequest()` | Registration API/UI/E2E |

### Rules

1. **Prefer unique emails** for registration tests to avoid collisions.
2. **Avoid destructive operations** on demo user when an ephemeral user can be created.
3. **Profile update tests** should restore original values after mutation.
4. **Never commit** real production credentials.

## 4. Factory Methods

| Method | Returns | Purpose |
|--------|---------|---------|
| `defaultLoginRequest()` | `LoginRequest` | Config-based demo login |
| `randomLoginRequest()` | `LoginRequest` | Negative auth tests |
| `randomRegisterRequest()` | `RegisterRequest` | Unique registration |
| `validTransactionRequest()` | `CreateTransactionRequest` | Create smoke/regression |
| `invalidTransactionRequest()` | `CreateTransactionRequest` | Negative tests |
| `validTaskRequest()` | `CreateTaskRequest` | Task CRUD |
| `validReportRequest()` | `GenerateReportRequest` | Report generation |
| `sampleCsvFile()` | `File` | Import upload tests |

## 5. Data Isolation by Suite

| Suite | Isolation strategy |
|-------|-------------------|
| API smoke | Read-heavy on demo user; register creates new user |
| API regression | Creates transactions/tasks; uses unique data where needed; isolation tests use second user |
| Contract | Minimal mutation; read endpoints preferred |
| UI smoke | Demo user; onboarding dismissed via `OnboardingUiHelper` |
| UI regression | Onboarding tests use `BaseOnboardingUiSmokeTest` (no dismiss) |
| E2E | Journey-specific; register flow uses new user |
| Integration DB | Testcontainers fresh DB + Flyway per class/suite |

## 6. File & Import Data

| Asset | Path | Usage |
|-------|------|-------|
| Sample CSV | `testdata/imports/*.csv` | `ImportCsvE2ETest`, imports regression |
| Invalid CSV | Generated empty/temp files | Negative import tests |

## 7. Database Access

| Environment | Connection | Tests |
|-------------|------------|-------|
| local/ci | `db.url` in properties | `*IntegrationDbTest` |
| Testcontainers | Dynamic PostgreSQL | `-Pintegration-db` |

Direct DB tests validate persistence layer bypassing HTTP where appropriate.

## 8. Cleanup Policy

| Data type | Cleanup |
|-----------|---------|
| Registered users | No auto-delete; use unique emails; periodic env refresh |
| Transactions/tasks created in tests | Prefer demo user data; delete in test when API supports |
| Import jobs | Left in demo tenant; acceptable for CI ephemeral reset |
| CI ephemeral stack | Destroyed after nightly — full data reset |
| localStorage (onboarding) | `OnboardingUiHelper` sets keys; tests may reset |

## 9. Sensitive Data

| Rule | Implementation |
|------|----------------|
| No PII in repo | Faker-generated names/emails |
| Secrets via env | Owner `system:env` merge |
| Ignore reportportal.properties | Project rule — not committed |
| Allure attachments | No passwords in logs (`TestExecutionListener`) |

## 10. Environment Refresh

| Environment | Refresh mechanism |
|-------------|-------------------|
| CI ephemeral | New compose stack per `github.run_id` |
| local Docker | `docker compose down -v` + reseed |
| stage/dev | Ops-managed; coordinate before destructive suites |

## 11. Future Improvements

| Gap | Recommendation |
|-----|----------------|
| Zero-data tenant | Dedicated user with empty transactions for empty-state tests |
| Test data API | Backend endpoint to reset demo workspace (if added) |
| Password change tests | Disposable user per test class |

## 12. References

- [ENVIRONMENT_DESCRIPTION.md](ENVIRONMENT_DESCRIPTION.md)
- [TEST_POLICY.md](TEST_POLICY.md)
- `com.flowiq.factories.TestDataFactory`
