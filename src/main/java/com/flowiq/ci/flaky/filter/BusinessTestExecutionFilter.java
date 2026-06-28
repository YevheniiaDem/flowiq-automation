package com.flowiq.ci.flaky.filter;

import com.flowiq.agents.flaky.model.TestExecutionRecord;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Excludes CI infrastructure signals from flaky classification.
 * Infrastructure retries ({@code retry.sh}, Docker, compose) never produce Allure/Surefire test results.
 */
public final class BusinessTestExecutionFilter {

    private static final Pattern INFRASTRUCTURE = Pattern.compile(
            "(?i)(retry\\.sh|docker|compose|ci-up|ci-down|infrastructure|ephemeral-stack|health.?check.?retry)");

    private BusinessTestExecutionFilter() {
    }

    public static boolean isBusinessTest(TestExecutionRecord record) {
        if (record == null) {
            return false;
        }
        String probe = String.join("|",
                nullToEmpty(record.getTestKey()),
                nullToEmpty(record.getClassName()),
                nullToEmpty(record.getSource()));
        if (INFRASTRUCTURE.matcher(probe).find()) {
            return false;
        }
        return record.getClassName() != null
                && !record.getClassName().isBlank()
                && !"unknown".equalsIgnoreCase(record.getClassName());
    }

    public static List<TestExecutionRecord> filterBusinessTests(List<TestExecutionRecord> records) {
        return records.stream().filter(BusinessTestExecutionFilter::isBusinessTest).toList();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
