package com.flowiq.agents.gap.scanner;

import java.util.regex.Pattern;

public final class EndpointMatcher {

    private EndpointMatcher() {
    }

    public static boolean matches(String testPath, String operationPath) {
        String normalizedTest = TestSourceScanner.normalizePath(testPath);
        String normalizedOp = TestSourceScanner.normalizePath(operationPath);
        if (normalizedTest.equals(normalizedOp)) {
            return true;
        }
        String testPattern = toRegex(normalizedTest);
        return Pattern.compile(testPattern).matcher(normalizedOp).matches()
                || Pattern.compile(toRegex(normalizedOp)).matcher(normalizedTest).matches();
    }

    public static boolean methodMatches(String testMethod, String operationMethod) {
        return testMethod == null || "*".equals(testMethod)
                || operationMethod.equalsIgnoreCase(testMethod);
    }

    private static String toRegex(String path) {
        return path.replaceAll("\\{[^}]+}", "[^/]+");
    }
}
