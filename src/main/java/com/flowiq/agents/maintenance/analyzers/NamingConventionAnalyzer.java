package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NamingConventionAnalyzer implements MaintenanceAnalyzer {

    private static final Pattern VALID_TEST_CLASS = Pattern.compile(
            "^(Smoke|Contract|Regression|Ui|Integration|Db)?[A-Z][A-Za-z0-9]*(Test|IT)$");
    private static final Pattern VALID_TEST_METHOD = Pattern.compile(
            "^(should|test|verify|when)[A-Z][A-Za-z0-9_]*$");

    @Override
    public String name() {
        return "NamingConventionAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        for (ScannedTestClass test : context.getTestClasses()) {
            if (!VALID_TEST_CLASS.matcher(test.getClassName()).matches()) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.NAMING)
                        .severity(MaintenanceSeverity.LOW)
                        .title("Test class naming convention violation")
                        .location(test.getClassName())
                        .recommendation("Rename to *SmokeTest, *ContractTest, *RegressionTest, *UiSmokeTest, or *IT.")
                        .priorityRank(4)
                        .build());
            }
            findings.addAll(checkMethodNames(test));
        }
        return findings;
    }

    private List<MaintenanceFinding> findInvalidMethods(String source, String className) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        Pattern methodPattern = Pattern.compile("@Test[^\\n]*\\n\\s*(?:public\\s+)?void\\s+(\\w+)\\s*\\(");
        var matcher = methodPattern.matcher(source);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (!VALID_TEST_METHOD.matcher(methodName).matches()) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.NAMING)
                        .severity(MaintenanceSeverity.LOW)
                        .title("Test method naming convention violation")
                        .location(className + "#" + methodName)
                        .recommendation("Use should*/test*/verify* naming for test methods.")
                        .priorityRank(4)
                        .build());
            }
        }
        return findings;
    }

    private List<MaintenanceFinding> checkMethodNames(ScannedTestClass test) {
        return findInvalidMethods(test.getSource(), test.getClassName());
    }
}
