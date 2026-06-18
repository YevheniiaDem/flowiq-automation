# API Change Report

## Changes

- **ADDED_REQUIRED_FIELD**: Required field added to LoginRequest: rememberMe _(breaking)_
- **REMOVED_ENDPOINT**: Endpoint removed: GET /transactions _(breaking)_
- **STATUS_CODE_ADDED**: Status code added: POST /auth/login -> 401

## Risk Level

**MEDIUM**

### Contract Tests

- AuthContractTest
- TransactionsContractTest
