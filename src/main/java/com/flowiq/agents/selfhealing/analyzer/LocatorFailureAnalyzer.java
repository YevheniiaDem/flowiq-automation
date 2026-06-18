package com.flowiq.agents.selfhealing.analyzer;

import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import com.flowiq.agents.selfhealing.model.LocatorType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocatorFailureAnalyzer {

    private static final Pattern LOCATOR_CALL = Pattern.compile(
            "(?i)(?:locator|getByTestId|getByRole|getByLabel|getByText|getByPlaceholder)\\s*\\(\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern WAITING_FOR = Pattern.compile(
            "(?i)waiting for locator\\(['\"]([^'\"]+)['\"]\\)");
    private static final Pattern STRICT_MODE = Pattern.compile(
            "(?i)strict mode violation.*?(?:locator|selector)\\s*['\"]([^'\"]+)['\"]",
            Pattern.DOTALL);
    private static final Pattern TEST_ID = Pattern.compile(
            "(?i)data-testid=['\"]([^'\"]+)['\"]");
    private static final Pattern LOCATOR_ISSUE = Pattern.compile(
            "(?i)(locator|element not found|timeout.*locator|strict mode)");

    public boolean isLocatorFailure(String message, String stackTrace) {
        String combined = safe(message) + "\n" + safe(stackTrace);
        return LOCATOR_ISSUE.matcher(combined).find();
    }

    public LocatorFailureContext enrich(LocatorFailureContext context) {
        String combined = safe(context.getFailureMessage()) + "\n" + safe(context.getStackTrace());
        ParsedLocator parsed = extractLocator(combined);
        return LocatorFailureContext.builder()
                .testKey(context.getTestKey())
                .testName(context.getTestName())
                .className(context.getClassName())
                .methodName(context.getMethodName())
                .failureMessage(context.getFailureMessage())
                .stackTrace(context.getStackTrace())
                .oldLocator(parsed.value())
                .oldLocatorType(parsed.type())
                .screenshotPath(context.getScreenshotPath())
                .domSnapshotPath(context.getDomSnapshotPath())
                .domElements(context.getDomElements())
                .build();
    }

    public ParsedLocator extractLocator(String text) {
        List<Pattern> patterns = List.of(WAITING_FOR, STRICT_MODE, LOCATOR_CALL, TEST_ID);
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String value = matcher.group(1).trim();
                return new ParsedLocator(value, inferType(value, pattern));
            }
        }
        return new ParsedLocator("unknown", LocatorType.UNKNOWN);
    }

    public List<String> extractLocatorHints(String oldLocator) {
        List<String> hints = new ArrayList<>();
        if (oldLocator == null || oldLocator.isBlank() || "unknown".equals(oldLocator)) {
            return hints;
        }
        hints.add(oldLocator);
        String stripped = oldLocator
                .replaceAll("^#", "")
                .replaceAll("^\\.", "")
                .replaceAll("[\\[\\]\"']", "");
        if (!stripped.equals(oldLocator)) {
            hints.add(stripped);
        }
        for (String part : oldLocator.split("[\\s.#\\[\\]'\"]+")) {
            if (part.length() >= 3) {
                hints.add(part);
            }
        }
        return hints.stream().distinct().toList();
    }

    private static LocatorType inferType(String value, Pattern matchedPattern) {
        if (matchedPattern == TEST_ID) {
            return LocatorType.TEST_ID;
        }
        if (value.startsWith("#") || value.startsWith(".") || value.contains(" > ")) {
            return LocatorType.CSS;
        }
        if (value.matches("(?i)button|textbox|link|dialog|checkbox|heading")) {
            return LocatorType.ROLE;
        }
        return LocatorType.CSS;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public record ParsedLocator(String value, LocatorType type) {
    }
}
