package com.flowiq.agents.traceability.matrix;

import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.traceability.docs.ModuleNameNormalizer;
import com.flowiq.agents.traceability.model.BusinessFeature;
import com.flowiq.agents.traceability.model.FeatureTraceabilityRow;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureTraceabilityMatrixBuilder {

    private final BusinessImpactPrioritizer prioritizer;

    public FeatureTraceabilityMatrixBuilder(BusinessImpactPrioritizer prioritizer) {
        this.prioritizer = prioritizer;
    }

    public List<FeatureTraceabilityRow> build(List<BusinessFeature> documentedFeatures,
                                              List<EndpointCoverage> endpointCoverages,
                                              List<ScannedTestReference> testReferences) {
        Map<String, List<EndpointCoverage>> byModule = endpointCoverages.stream()
                .collect(Collectors.groupingBy(EndpointCoverage::getModule, LinkedHashMap::new, Collectors.toList()));

        Map<String, BusinessFeature> docsByModule = documentedFeatures.stream()
                .collect(Collectors.toMap(BusinessFeature::getModule, f -> f, (a, b) -> a, LinkedHashMap::new));

        Set<String> allModules = new LinkedHashSet<>();
        allModules.addAll(byModule.keySet());
        allModules.addAll(docsByModule.keySet());
        testReferences.stream().map(ScannedTestReference::getModule).forEach(allModules::add);

        return allModules.stream()
                .map(module -> buildRow(module, docsByModule.get(module),
                        byModule.getOrDefault(module, List.of()),
                        testReferences))
                .sorted(Comparator.comparing(FeatureTraceabilityRow::getBusinessImpact)
                        .thenComparing(row -> -row.getCoveragePercent()))
                .toList();
    }

    private FeatureTraceabilityRow buildRow(String module,
                                            BusinessFeature documented,
                                            List<EndpointCoverage> endpoints,
                                            List<ScannedTestReference> testReferences) {
        boolean uiExpected = prioritizer.uiExpected(module);
        String featureName = documented != null
                ? documented.getDisplayName()
                : ModuleNameNormalizer.toDisplayName(module);

        Set<String> smokeTests = testClassNames(testReferences, module, TestSuiteType.SMOKE);
        Set<String> regressionTests = testClassNames(testReferences, module, TestSuiteType.REGRESSION);
        Set<String> contractTests = testClassNames(testReferences, module, TestSuiteType.CONTRACT);
        Set<String> uiTests = testClassNames(testReferences, module, TestSuiteType.UI);

        boolean smokeCovered = !smokeTests.isEmpty() || endpoints.stream().anyMatch(EndpointCoverage::isSmokeCovered);
        boolean regressionCovered = !regressionTests.isEmpty()
                || endpoints.stream().anyMatch(EndpointCoverage::isRegressionCovered);
        boolean contractCovered = !contractTests.isEmpty()
                || endpoints.stream().anyMatch(EndpointCoverage::isContractCovered);
        boolean uiCovered = !uiTests.isEmpty() || endpoints.stream().anyMatch(EndpointCoverage::isUiCovered);

        double coveragePercent = endpoints.isEmpty()
                ? suiteOnlyCoverage(smokeCovered, regressionCovered, contractCovered, uiCovered, uiExpected)
                : endpoints.stream()
                .mapToDouble(e -> e.coverageScore(uiExpected))
                .average()
                .orElse(0.0);

        Set<TestSuiteType> missing = missingSuites(smokeCovered, regressionCovered, contractCovered, uiCovered, uiExpected);

        var rowBuilder = FeatureTraceabilityRow.builder()
                .module(module)
                .featureName(featureName)
                .endpointsSummary(formatEndpoints(endpoints))
                .smokeCovered(smokeCovered)
                .regressionCovered(regressionCovered)
                .contractCovered(contractCovered)
                .uiCovered(uiCovered)
                .smokeTests(joinTests(smokeTests))
                .regressionTests(joinTests(regressionTests))
                .contractTests(joinTests(contractTests))
                .uiTests(joinTests(uiTests))
                .coveragePercent(round(coveragePercent))
                .endpointCount(endpoints.size())
                .documentedInDocs(documented != null)
                .businessImpact(prioritizer.businessImpactFor(module))
                .highRisk(isHighRisk(module, coveragePercent))
                .missingSuites(missing);

        if (documented != null) {
            documented.getDocSources().forEach(rowBuilder::docSource);
        }
        return rowBuilder.build();
    }

    private boolean isHighRisk(String module, double coveragePercent) {
        var impact = prioritizer.businessImpactFor(module);
        return (impact.ordinal() <= com.flowiq.agents.gap.model.GapSeverity.HIGH.ordinal())
                && coveragePercent < 75.0;
    }

    private static Set<TestSuiteType> missingSuites(boolean smoke, boolean regression,
                                                    boolean contract, boolean ui, boolean uiExpected) {
        Set<TestSuiteType> missing = new LinkedHashSet<>();
        if (!smoke) missing.add(TestSuiteType.SMOKE);
        if (!regression) missing.add(TestSuiteType.REGRESSION);
        if (!contract) missing.add(TestSuiteType.CONTRACT);
        if (uiExpected && !ui) missing.add(TestSuiteType.UI);
        return missing;
    }

    private static double suiteOnlyCoverage(boolean smoke, boolean regression, boolean contract,
                                            boolean ui, boolean uiExpected) {
        int total = uiExpected ? 4 : 3;
        int covered = 0;
        if (smoke) covered++;
        if (regression) covered++;
        if (contract) covered++;
        if (uiExpected && ui) covered++;
        return total == 0 ? 0.0 : (covered * 100.0) / total;
    }

    private static Set<String> testClassNames(List<ScannedTestReference> references,
                                              String module, TestSuiteType suite) {
        return references.stream()
                .filter(r -> module.equals(r.getModule()))
                .filter(r -> r.getSuites().contains(suite))
                .map(ScannedTestReference::getClassName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String formatEndpoints(List<EndpointCoverage> endpoints) {
        if (endpoints.isEmpty()) {
            return "—";
        }
        return endpoints.stream()
                .limit(6)
                .map(e -> e.getMethod() + " " + e.getPath())
                .collect(Collectors.joining(", "))
                + (endpoints.size() > 6 ? " (+" + (endpoints.size() - 6) + " more)" : "");
    }

    private static String joinTests(Set<String> tests) {
        return tests.isEmpty() ? "—" : String.join(", ", tests);
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
