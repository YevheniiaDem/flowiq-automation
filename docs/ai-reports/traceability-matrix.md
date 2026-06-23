# Requirements Traceability Matrix

_Business features mapped to OpenAPI endpoints and automated test suites_

Generated: 2026-06-17T12:00:00Z

## Executive Summary

_For architects and QA managers_

- Overall feature traceability coverage is 82.5%.

| Metric | Value |
|--------|-------|
| Features in matrix | 3 |
| Documented in docs/ | 2 |
| OpenAPI endpoints | 10 |
| **Overall coverage** | **82.5%** |

## Traceability Matrix

| Feature | Endpoint | Smoke | Regression | Contract | UI | Coverage % |
|---------|----------|:-----:|:----------:|:--------:|:--:|------------|
| **Auth** | POST /auth/login | ✓ | ✓ | ✓ | ✗ | 75% |

### Test Class References

#### Auth (`auth`)

| Suite | Test Classes |
|-------|-------------|
| Smoke | AuthSmokeApiTest |
| Regression | AuthRegressionTest |
| Contract | AuthContractTest |
| UI | — |

## Missing Coverage

_None identified._

## Broken Traceability

_None identified._

## High-Risk Features

_None identified._

## Data Sources

test

## Recommendations

1. Close **missing coverage** gaps before major releases, starting with CRITICAL/HIGH business modules.
2. Fix **broken traceability** links between docs/, OpenAPI, and test classes.
3. Prioritize **high-risk features** below 50.0% coverage in sprint planning.
4. Re-run `RequirementsTraceabilityAgent` after adding endpoints, tests, or documentation.

