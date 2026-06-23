# Flaky Test Investigation Report

_Prepared for QA leadership review_

Generated: 2026-06-18T12:20:19.9967654Z

## Executive Summary

- Portfolio pass rate is 50.0% with 50.0% failure rate across analyzed runs.
- 2 flaky tests identified (50.0% portfolio flakiness).
- Highest priority: `shouldSendMessage` (score 40.0, TIMEOUT).

## Portfolio Metrics

| Metric | Value |
|--------|-------|
| Executions analyzed | 4 |
| Unique tests | 2 |
| **Pass rate** | **50.0%** |
| **Failure rate** | **50.0%** |
| **Flakiness %** | **50.0%** |
| Flaky tests detected | 2 |

## Data Sources

test-fixtures

## Top 2 Unstable Tests

| Rank | Test | Suite | Pass % | Fail % | Flaky % | Priority | Root Cause |
|------|------|-------|--------|--------|---------|----------|------------|
| 1 | `shouldSendMessage` | smoke | 50.0 | 50.0 | 50.0 | **40.0** | TIMEOUT |
| 2 | `shouldShowWarnings` | smoke | 50.0 | 50.0 | 50.0 | **40.0** | LOCATOR_ISSUE |

## Root Cause Hypotheses & Recommended Fixes

### 1. `shouldSendMessage`

- **Priority score:** 40.0
- **Pass / Fail / Runs:** 1 / 1 / 2
- **Primary hypothesis:** TIMEOUT — Test exceeded configured timeout — likely slow UI render, API latency, or insufficient wait strategy.
- **Alternate hypotheses:** TIMEOUT
- **Last failure:** `Timeout 30000ms exceeded waiting for locator`
- **Recommended fix:** Increase explicit waits; use Playwright auto-waiting locators; raise timeout for smoke suite or mock slow dependencies.

### 2. `shouldShowWarnings`

- **Priority score:** 40.0
- **Pass / Fail / Runs:** 1 / 1 / 2
- **Primary hypothesis:** LOCATOR_ISSUE — UI locator instability — DOM timing, missing data-testid, or strict-mode duplicate matches.
- **Last failure:** `Element not found: locator('.rounded-xl.border')`
- **Recommended fix:** Replace CSS/text selectors with data-testid; add waitForVisible before interactions; review strict-mode violations.

## Root Cause Distribution

- **TIMEOUT:** 1
- **LOCATOR_ISSUE:** 1

## Recommended Actions for Leadership

1. **Stabilize top 5 by priority score** before expanding suite coverage.
2. **Quarantine** tests with flakiness > 40% from PR gates; keep in nightly only.
3. **Track weekly** pass rate and flakiness % trend from this report.
4. **Investigate environment** when NETWORK or BACKEND root causes dominate.
5. **Assign owners** per suite (UI → frontend QA, API → backend QA).

