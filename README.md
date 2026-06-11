# Flowiq Test Automation Framework

Production-ready API and UI test automation framework for the Flowiq platform.

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Build | Maven |
| Test Runner | TestNG |
| API Testing | Rest Assured |
| UI Testing | Playwright Java |
| Reporting | Allure Report |
| JSON | Jackson |
| Config | Owner |
| Logging | SLF4J + Logback |
| Assertions | AssertJ |
| Waits | Awaitility |
| Test Data | DataFaker |
| DB (future) | Testcontainers + PostgreSQL |

## Project Structure

```
src/
├── main/java/com/flowiq/
│   ├── config/          # ConfigManager, EnvironmentConfig (Owner)
│   ├── constants/       # API endpoints, UI paths, shared constants
│   ├── utils/           # JsonUtils, DateUtils, RandomDataGenerator
│   ├── models/          # DTOs / request-response models
│   ├── clients/         # ApiClientFactory, PlaywrightFactory
│   ├── services/        # API service layer (AuthService, etc.)
│   ├── factories/       # TestDataFactory
│   ├── pages/           # Page Objects for UI tests
│   └── listeners/       # AllureListener
│
├── main/resources/
│   ├── config/          # Framework-level properties
│   └── logback.xml
│
└── test/java/com/flowiq/
    ├── api/             # API tests by domain (auth, transactions, reports, ...)
    ├── ui/              # UI tests by feature (login, dashboard, ...)
    ├── base/            # BaseApiTest, BaseUiTest, assertions
    ├── data/            # Test data helpers
    ├── providers/       # TestNG DataProviders
    └── listeners/       # Test execution listeners

test/resources/
├── testng/              # TestNG suite files
├── environments/        # Environment-specific properties (local, dev, stage)
├── testdata/            # JSON/CSV test data
└── schemas/             # JSON schemas for API contract validation
```

## Prerequisites

- JDK 17+
- Maven 3.9+
- Git

## Quick Start

### 1. Install Playwright browsers

```bash
# Headless CI / default (bundled Chromium)
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"

# Visible Google Chrome (uses installed Chrome on your machine)
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chrome"
```

### 2. Configure environment

Edit `src/main/resources/environments/local.properties` or pass `-Denv=dev|stage`:

| Environment | UI (`base.url`) | API (`api.url`) | Default user |
|-------------|-----------------|-----------------|--------------|
| **local** | `http://localhost:3000` | `http://localhost:8080/api` | `demo@flowiq.ai` / `demo123` |
| **dev** | `http://localhost:3000` | `http://localhost:8080/api` | via `TEST_USER_EMAIL` / `TEST_USER_PASSWORD` |
| **stage** | `https://flowiq.vercel.app` | `https://api.flowiq.ai` | via secrets |

### 3. Run tests

```bash
# Full suite
mvn clean test

# Smoke tests only
mvn clean test -Psmoke

# API tests only
mvn clean test -Papi

# UI tests only
mvn clean test -Pui

# UI smoke in visible Google Chrome (local debugging)
mvn test -Pui-headed

# E2E in visible Google Chrome
mvn test -Pe2e-headed

# Dev environment
mvn clean test -Pdev
```

### Watch UI tests in Chrome (headed mode)

By default UI tests run in **headless** bundled Chromium (`headless=true` in `local.properties`).

| Goal | What to change |
|------|----------------|
| **Quick one-off** | Add Maven flags: `-Dheadless=false -Dbrowser=chrome` |
| **Profile (recommended)** | `mvn test -Pui-headed` — uses `local-headed.properties` |
| **Permanent local default** | Edit `src/main/resources/environments/local.properties`: set `headless=false`, `browser=chrome` |
| **IntelliJ run** | VM options: `-Dheadless=false -Dbrowser=chrome -Dslow.mo=200` |

| Property | Headless (CI/default) | Visible Chrome (debug) |
|----------|----------------------|-------------------------|
| `headless` | `true` | `false` |
| `browser` | `chromium` | `chrome` |
| `slow.mo` | `0` | `200` (optional, ms delay per action) |

`browser` values: `chromium` (bundled), `chrome` / `msedge` (installed browser), `firefox`, `webkit`.

Config file for headed runs: `src/main/resources/environments/local-headed.properties` (activated by `-Pui-headed` or `-Denv=local-headed`).

### 4. Generate Allure report

```bash
mvn allure:serve
```

## Environment Configuration

| Property | Description |
|----------|-------------|
| `env` | Environment name (local, dev, stage) |
| `base.url` | UI application base URL |
| `api.url` | API base URL |
| `browser` | `chromium` (default), `chrome`, `msedge`, `firefox`, `webkit` |
| `headless` | `true` = invisible browser; `false` = visible window |
| `slow.mo` | Delay in ms between Playwright actions (useful when watching tests) |
| `test.user.email` | Default test user email |
| `test.user.password` | Default test user password |

Environment is resolved in order: `-Denv` → `ENV` env var → `local`.

## Test Groups

| Group | Description |
|-------|-------------|
| `unit` | Framework unit tests (CI build gate; add tests with `@Test(groups = "unit")`) |
| `smoke` | Critical path smoke tests |
| `api` | API test suite |
| `ui` | UI test suite |
| `regression` | API regression tests |
| `e2e` | End-to-end UI flows |
| `integration` | Database / Testcontainers tests |

## CI/CD (GitHub Actions)

Smoke and UI tests **do not run automatically against `localhost`** on push.  
Live tests run manually or on a nightly schedule against **stage/dev** with GitHub Secrets.

### Workflows

| Workflow | Trigger | Purpose | Maven command |
|----------|---------|---------|---------------|
| **CI** (`ci.yml`) | `push`, `pull_request` → `main`, `develop` | Compile + unit gate only | `mvn clean compile` then `mvn test -Dgroups=unit` |
| **API Smoke** (`api-smoke.yml`) | Manual (`workflow_dispatch`) | API smoke against live env | `mvn test -Papi-smoke -Denv=stage` |
| **UI Smoke** (`ui-smoke.yml`) | Manual (`workflow_dispatch`) | Playwright UI smoke | `mvn test -Pui-smoke -Denv=stage` |
| **Nightly Regression** (`nightly-regression.yml`) | Cron `0 3 * * *` + manual | Full regression suite | `mvn test -Pregression -Denv=stage` |

Nightly regression uses `continue-on-error: true` — failures are reported but do not block day-to-day development.

### Manual run (GitHub UI)

1. Open **Actions** in the repository.
2. Select **API Smoke**, **UI Smoke**, or **Nightly Regression**.
3. Click **Run workflow**.
4. Choose environment: `stage` (default) or `dev`.
5. Download artifacts: `allure-results`, `surefire-reports` (and Playwright traces for UI).

### Required GitHub Secrets

Configure under **Settings → Secrets and variables → Actions**:

| Secret | Used by |
|--------|---------|
| `TEST_USER_EMAIL` | API Smoke, UI Smoke, Nightly Regression |
| `TEST_USER_PASSWORD` | API Smoke, UI Smoke, Nightly Regression |

If secrets are missing, workflows exit early with a clear message (no stacktrace).

Optional (for future DB integration in CI):

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

### Maven profiles

| Profile | Description |
|---------|-------------|
| `local` | Local URLs (`localhost:3000` / `localhost:8080`) |
| `dev` | Dev environment properties + secrets |
| `stage` | Stage URLs (`flowiq.vercel.app` / `api.flowiq.ai`) |
| `unit` | Auto-activated by `-Dgroups=unit` → `unit-suite.xml` |
| `api-smoke` | `api-smoke-suite.xml` |
| `ui-smoke` | `ui-smoke-suite.xml` |
| `regression` | `regression-suite.xml` (smoke + API regression) |
| `ui-headed` | Visible Chrome for local UI debugging |

### Local commands (live environments)

```bash
# API smoke against stage (set env vars or use secrets locally)
export TEST_USER_EMAIL=your@email.com
export TEST_USER_PASSWORD=yourpassword
mvn test -Papi-smoke -Denv=stage

# UI smoke against stage (install Playwright first)
mvn test -Pui-smoke -Denv=stage

# Full regression locally
mvn test -Pregression -Denv=stage

# CI-equivalent build gate
mvn clean compile
mvn test -Dgroups=unit
```

### Local commands (localhost)

Requires running frontend (`:3000`) and backend (`:8080`):

```bash
mvn test -Plocal -Papi-smoke
mvn test -Plocal -Pui-smoke
mvn test -Pui-headed          # visible Chrome
```

## Framework Components

| Component | Purpose |
|-----------|---------|
| `BaseApiTest` | API test lifecycle, config, AuthService setup |
| `BaseUiTest` | Playwright session per test method |
| `ApiClientFactory` | Rest Assured base/authenticated specs with Allure filter |
| `PlaywrightFactory` | Thread-local Playwright browser session management |
| `ConfigManager` | Singleton Owner config loader |
| `EnvironmentConfig` | Typed environment properties interface |
| `TestDataFactory` | Reusable test data builders |
| `ApiAssertions` / `UiAssertions` | Shared assertion helpers with Awaitility |
| `AllureListener` | TestNG listener for Allure reporting |

## Adding New Tests

### API Test

1. Create service in `com.flowiq.services`
2. Add model in `com.flowiq.models`
3. Create test class extending `BaseApiTest` in `com.flowiq.api.<domain>`
4. Annotate with `@Epic`, `@Feature`, `@Severity`

### UI Test

1. Create Page Object in `com.flowiq.pages`
2. Create test class extending `BaseUiTest` in `com.flowiq.ui.<feature>`
3. Use `UiAssertions` for common checks

## License

Internal — Flowiq QA Team
