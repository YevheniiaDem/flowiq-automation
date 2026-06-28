package com.flowiq.clients.filters;

import java.util.regex.Pattern;

/**
 * Redacts sensitive values from API log and Allure attachments.
 */
public final class ApiLogSanitizer {

    private static final Pattern SENSITIVE_JSON_FIELD = Pattern.compile(
            "(\"(?:password|token|refreshToken|accessToken|authorization)\"\\s*:\\s*)\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BEARER_HEADER = Pattern.compile(
            "Bearer\\s+[A-Za-z0-9\\-._~+/]+=*",
            Pattern.CASE_INSENSITIVE);

    private ApiLogSanitizer() {
    }

    public static String sanitize(Object value) {
        if (value == null) {
            return "";
        }
        return sanitize(String.valueOf(value));
    }

    public static String sanitize(String content) {
        if (content == null || content.isBlank()) {
            return content == null ? "" : content;
        }
        String redacted = SENSITIVE_JSON_FIELD.matcher(content).replaceAll("$1\"***\"");
        return BEARER_HEADER.matcher(redacted).replaceAll("Bearer ***");
    }
}
