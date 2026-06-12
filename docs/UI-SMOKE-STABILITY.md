# UI Smoke Suite — Stability Report

Production-ready UI smoke tests in `com.flowiq.ui.smoke`.  
Run: `mvn test -Pui-smoke -Plocal` (frontend `:3000` + backend `:8080`).

## Test Inventory (27 tests)

| Page | Tests | Scenarios |
|------|-------|-----------|
| Login | 4 | page open, form fields, valid login, invalid login |
| Dashboard | 3 | KPI cards, charts, sidebar |
| Transactions | 3 | page open, search, table |
| Imports | 1 | page open (upload zone + history) |
| Reports | 3 | page open, generate report, history |
| Tasks | 3 | list, filters, create task |
| Notifications | 2 | list, mark as read |
| Forecasts | 2 | charts, warnings |
| Business Guide | 2 | search, open article |
| AI Accountant | 2 | chat UI, send message |

## Failure Diagnostics

On test failure, the framework automatically attaches:

| Artifact | Location | Allure |
|----------|----------|--------|
| Screenshot (full page) | captured in-memory | `*- screenshot` PNG |
| Playwright trace | `target/traces/trace-*.zip` | `Playwright trace` |
| Video recording | `target/videos/*.webm` | `Playwright video` |

Tracing is always started per session; trace/video are saved and attached **only on failure**.

## Stability Classification

| Risk | Tests | Notes |
|------|-------|-------|
| **Stable** | Login (page/fields), Dashboard sidebar, Transactions page/search/table, Imports page, Reports page open, Tasks filters, Business Guide search, AI chat UI | Static UI elements, minimal API dependency |
| **Medium** | Login success, Dashboard KPI/charts, Reports generate/history, Tasks create, Forecasts charts | Requires backend API; loading spinners |
| **Conditional skip** | Notifications mark-as-read | Skips when no unread notifications |
| **Flaky-prone** | AI Accountant send message, Business Guide open article, Forecasts warnings | LLM latency, dynamic content, warnings may be empty |

## Known Dependencies

1. **Authenticated tests** — API login + `localStorage` token injection (`AuthenticatedUiTest`)
2. **Demo user** — `demo@flowiq.ai` / `demo123` for local (`local.properties`)
3. **Stage** — `TEST_USER_EMAIL` / `TEST_USER_PASSWORD` env vars
4. **Playwright browsers** — `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"`

## Recommended Run Matrix

| Environment | Command | Expected pass rate |
|-------------|---------|-------------------|
| Local (headed debug) | `mvn test -Pui-headed -Dtest=LoginSmokeTest` | ~100% if stack up |
| Local (full suite) | `mvn test -Pui-smoke -Plocal` | 24–27/27 (skips possible) |
| Stage | `mvn test -Pui-smoke -Pstage` | depends on env data |

## Maintenance Checklist

- [ ] Add `data-testid` to Report history table and Forecast warnings (reduce CSS fallback)
- [ ] Add `data-testid` to Task form dialog for resilient create-task flow
- [ ] Seed demo notifications for stable mark-as-read smoke
- [ ] Mock or stub AI Accountant in smoke env to reduce LLM flakiness
- [ ] Track pass rate in CI after adding `ui-smoke.yml` workflow

## Legacy Tests

Older smoke tests remain under `com.flowiq.ui.{domain}` for backward compatibility with `-Psmoke` suite.  
New development should use `com.flowiq.ui.smoke` only.
