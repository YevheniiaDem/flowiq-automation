# Self-Healing Locator Report

_Automated Playwright locator recovery suggestions_

Generated: 2026-06-17T12:00:00Z

## Executive Summary

- Analyzed 1 failure

| Metric | Value |
|--------|-------|
| Failures analyzed | 1 |
| Suggestions generated | 1 |

## Suggestion 1

**Test**

shouldSendMessage (`com.flowiq.ui.smoke.AIAccountantSmokeTest#shouldSendMessage`)

**Old Locator**

`.old-send-btn`

**Suggested Locator**

`page.getByTestId('ai-accountant-chat-send-btn')`

**Confidence**

HIGH (score 0.91)

**Reasoning**

Matched button via test-id

**Risk**

LOW

- **Screenshot:** —
- **DOM Snapshot:** fixture.html

---

## Implementation Notes

1. Prefer `data-testid` suggestions (LOW risk) over CSS/text selectors.
2. Validate suggested locators in headed mode: `mvn test -Pui-headed -Dtest=...`
3. Update Page Objects under `com.flowiq.pages` after manual verification.
4. Re-run with `-Pself-healing` after the next UI failure batch.

## Data Sources

fixtures

