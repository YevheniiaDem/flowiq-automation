# API Change Report

Generated: 2026-06-17T10:00:00Z

## Changes

- **REMOVED_ENDPOINT**: Endpoint removed: GET /transactions _(breaking)_
- **ADDED_ENDPOINT**: Endpoint added: GET /tasks
- **MODIFIED_RESPONSE_SCHEMA**: Component schema modified: LoginRequest
- **MODIFIED_RESPONSE_SCHEMA**: Component schema modified: AuthResponse
- **MODIFIED_RESPONSE_SCHEMA**: Schema removed from components: TransactionPage _(breaking)_
- **MODIFIED_RESPONSE_SCHEMA**: Component schema modified: TaskPriority
- **ENUM_VALUE_ADDED**: Enum value added to TaskPriority: CRITICAL
- **STATUS_CODE_ADDED**: Status code added: POST /auth/login -> 401
- **ADDED_REQUIRED_FIELD**: Required field added to LoginRequest: rememberMe _(breaking)_
- **ADDED_REQUIRED_FIELD**: Required field added to AuthResponse: expiresAt _(breaking)_

## Risk Level

**MEDIUM**

## Affected Tests

### Contract Tests

- TransactionsContractTest
- TasksContractTest
- AuthContractTest

### Smoke Tests

- TransactionsSmokeApiTest
- TasksSmokeApiTest
- AuthSmokeApiTest

### Regression Tests

- TransactionsRegressionTest
- TasksRegressionTest
- AuthRegressionTest

### UI Tests

- TransactionsUiSmokeTest
- TasksUiSmokeTest

## Impact Matrix

| Endpoint | Method | Risk | Contract | Smoke | Regression | UI |
|----------|--------|------|----------|-------|------------|----|
| /transactions | GET | MEDIUM | TransactionsContractTest | TransactionsSmokeApiTest | TransactionsRegressionTest | TransactionsUiSmokeTest |
| /tasks | GET | LOW | TasksContractTest | TasksSmokeApiTest | TasksRegressionTest | TasksUiSmokeTest |
| - | - | MEDIUM | - | - | - | - |
| - | - | MEDIUM | - | - | - | - |
| - | - | MEDIUM | - | - | - | - |
| - | - | LOW | - | - | - | - |
| /auth/login | POST | LOW | AuthContractTest | AuthSmokeApiTest | AuthRegressionTest | - |

## Recommended Actions

- Review breaking changes with the backend team before merging.
- Update JSON Schema contract files under src/test/resources/schemas/.
- Run contract suite: mvn test -Pcontract
- Run API smoke suite: mvn test -Papi-smoke
- Run API regression suite: mvn test -Papi-regression
- Run UI smoke suite: mvn test -Pui-smoke
- Add coverage for new endpoints in services, contract schemas, and regression tests.
- Remove or deprecate tests and client code for removed endpoints.

