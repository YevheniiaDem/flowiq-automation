package com.flowiq.agents.release.analyzer;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.gap.scanner.TestSuiteClassifier;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.release.model.CriticalFailure;
import com.flowiq.agents.release.model.FailureSeverity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CriticalFailureAnalyzer {

    private static final Pattern MODULE = Pattern.compile("([A-Z][a-zA-Z0-9]+)(Contract|Smoke|Regression|Ui)");

    public List<CriticalFailure> analyze(List<TestExecutionRecord> records) {
        List<CriticalFailure> failures = new ArrayList<>();
        for (TestExecutionRecord record : records) {
            if (!record.getOutcome().isFailure()) {
                continue;
            }
            TestSuiteType suiteType = resolveSuiteType(record);
            if (suiteType == null || suiteType == TestSuiteType.UI) {
                continue;
            }
            failures.add(CriticalFailure.builder()
                    .testKey(record.getTestKey())
                    .className(record.getClassName())
                    .methodName(record.getMethodName())
                    .suiteType(suiteType)
                    .module(extractModule(record.getClassName()))
                    .message(record.getMessage())
                    .source(record.getSource())
                    .severity(severityFor(suiteType))
                    .build());
        }
        failures.sort(Comparator.comparing(CriticalFailure::getSeverity)
                .thenComparing(CriticalFailure::getSuiteType));
        return failures;
    }

    private static TestSuiteType resolveSuiteType(TestExecutionRecord record) {
        String simple = simpleClassName(record.getClassName());
        Set<TestSuiteType> suites = TestSuiteClassifier.classify(simple, record.getSource());
        if (suites.contains(TestSuiteType.SMOKE)) {
            return TestSuiteType.SMOKE;
        }
        if (suites.contains(TestSuiteType.CONTRACT)) {
            return TestSuiteType.CONTRACT;
        }
        if (suites.contains(TestSuiteType.REGRESSION)) {
            return TestSuiteType.REGRESSION;
        }
        return switch (record.getSuite()) {
            case "smoke" -> TestSuiteType.SMOKE;
            case "contract" -> TestSuiteType.CONTRACT;
            case "regression" -> TestSuiteType.REGRESSION;
            default -> null;
        };
    }

    private static FailureSeverity severityFor(TestSuiteType suiteType) {
        return switch (suiteType) {
            case SMOKE -> FailureSeverity.BLOCKER;
            case CONTRACT -> FailureSeverity.CRITICAL;
            case REGRESSION -> FailureSeverity.MAJOR;
            default -> FailureSeverity.MAJOR;
        };
    }

    static String extractModule(String className) {
        String simple = simpleClassName(className);
        Matcher matcher = MODULE.matcher(simple);
        if (matcher.find()) {
            return camelToKebab(matcher.group(1));
        }
        int dot = className.lastIndexOf('.');
        if (dot >= 0) {
            String pkg = className.substring(0, dot);
            int last = pkg.lastIndexOf('.');
            return last >= 0 ? pkg.substring(last + 1) : pkg;
        }
        return "unknown";
    }

    private static String simpleClassName(String className) {
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private static String camelToKebab(String value) {
        return value.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }
}
