package com.flowiq.agents.gap.scanner;

import com.flowiq.agents.model.TestSuiteType;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TestSuiteClassifier {

    private static final Pattern CONTRACT = Pattern.compile("ContractTest$");
    private static final Pattern SMOKE = Pattern.compile("(SmokeApiTest|SmokeTest)$");
    private static final Pattern REGRESSION = Pattern.compile("(RegressionTest|RegressionApiTest)$");
    private static final Pattern UI = Pattern.compile("(UiSmokeTest|UiTest|E2ETest)$");

    private TestSuiteClassifier() {
    }

    public static Set<TestSuiteType> classify(String className, String source) {
        Set<TestSuiteType> suites = EnumSet.noneOf(TestSuiteType.class);
        if (CONTRACT.matcher(className).find() || containsGroup(source, "contract")) {
            suites.add(TestSuiteType.CONTRACT);
        }
        if (SMOKE.matcher(className).find() || containsGroup(source, "smoke", "api-smoke")) {
            suites.add(TestSuiteType.SMOKE);
        }
        if (REGRESSION.matcher(className).find() || containsGroup(source, "regression", "api-regression")) {
            suites.add(TestSuiteType.REGRESSION);
        }
        if (UI.matcher(className).find() || containsGroup(source, "ui-smoke", "ui", "e2e")) {
            suites.add(TestSuiteType.UI);
        }
        return suites;
    }

    private static boolean containsGroup(String source, String... groups) {
        Matcher matcher = Pattern.compile("groups\\s*=\\s*\\{([^}]*)}").matcher(source);
        while (matcher.find()) {
            String groupBlock = matcher.group(1);
            for (String group : groups) {
                if (groupBlock.contains("\"" + group + "\"")) {
                    return true;
                }
            }
        }
        return false;
    }
}
