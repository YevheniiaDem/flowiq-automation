package com.flowiq.agents.traceability.analyzer;

import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.traceability.config.TraceabilityAgentConfig;
import com.flowiq.agents.traceability.model.BusinessFeature;
import com.flowiq.agents.traceability.model.FeatureTraceabilityRow;
import com.flowiq.agents.traceability.model.TraceabilityIssue;
import com.flowiq.agents.traceability.model.TraceabilityIssueType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TraceabilityIssueAnalyzer {

    private final double highRiskThreshold;

    public TraceabilityIssueAnalyzer(TraceabilityAgentConfig config) {
        this.highRiskThreshold = config.highRiskCoverageThreshold();
    }

    public List<TraceabilityIssue> analyze(List<FeatureTraceabilityRow> matrix,
                                         List<BusinessFeature> documentedFeatures,
                                         List<ScannedTestReference> testReferences) {
        List<TraceabilityIssue> issues = new ArrayList<>();
        issues.addAll(missingCoverageIssues(matrix));
        issues.addAll(brokenTraceabilityIssues(matrix, documentedFeatures, testReferences));
        issues.addAll(highRiskIssues(matrix));
        issues.sort(Comparator.comparing(TraceabilityIssue::getSeverity)
                .thenComparing(TraceabilityIssue::getType));
        return issues;
    }

    private List<TraceabilityIssue> missingCoverageIssues(List<FeatureTraceabilityRow> matrix) {
        List<TraceabilityIssue> issues = new ArrayList<>();
        for (FeatureTraceabilityRow row : matrix) {
            for (TestSuiteType suite : row.getMissingSuites()) {
                issues.add(TraceabilityIssue.builder()
                        .type(TraceabilityIssueType.MISSING_COVERAGE)
                        .module(row.getModule())
                        .featureName(row.getFeatureName())
                        .severity(severityForMissing(row, suite))
                        .description("Missing " + suite.name().toLowerCase()
                                + " coverage for feature `" + row.getFeatureName() + "` ("
                                + row.getModule() + ")")
                        .build());
            }
        }
        return issues;
    }

    private List<TraceabilityIssue> brokenTraceabilityIssues(List<FeatureTraceabilityRow> matrix,
                                                             List<BusinessFeature> documentedFeatures,
                                                             List<ScannedTestReference> testReferences) {
        List<TraceabilityIssue> issues = new ArrayList<>();

        Set<String> documentedModules = documentedFeatures.stream()
                .map(BusinessFeature::getModule)
                .collect(Collectors.toSet());

        for (FeatureTraceabilityRow row : matrix) {
            if (row.isDocumentedInDocs() && row.getEndpointCount() == 0) {
                issues.add(TraceabilityIssue.builder()
                        .type(TraceabilityIssueType.BROKEN_TRACEABILITY)
                        .module(row.getModule())
                        .featureName(row.getFeatureName())
                        .severity(GapSeverity.HIGH)
                        .description("Feature documented in docs but no matching OpenAPI endpoints found")
                        .build());
            }
            if (!row.isDocumentedInDocs() && row.getEndpointCount() > 0) {
                issues.add(TraceabilityIssue.builder()
                        .type(TraceabilityIssueType.BROKEN_TRACEABILITY)
                        .module(row.getModule())
                        .featureName(row.getFeatureName())
                        .severity(GapSeverity.MEDIUM)
                        .description("OpenAPI module exists without documentation trace in docs/")
                        .build());
            }
            if (hasOrphanTests(row.getModule(), testReferences) && row.getEndpointCount() == 0) {
                issues.add(TraceabilityIssue.builder()
                        .type(TraceabilityIssueType.BROKEN_TRACEABILITY)
                        .module(row.getModule())
                        .featureName(row.getFeatureName())
                        .severity(GapSeverity.HIGH)
                        .description("Automated tests exist but cannot be traced to OpenAPI endpoints")
                        .build());
            }
        }

        for (BusinessFeature feature : documentedFeatures) {
            if (matrix.stream().noneMatch(r -> r.getModule().equals(feature.getModule()))) {
                issues.add(TraceabilityIssue.builder()
                        .type(TraceabilityIssueType.BROKEN_TRACEABILITY)
                        .module(feature.getModule())
                        .featureName(feature.getDisplayName())
                        .severity(GapSeverity.HIGH)
                        .description("Documented feature has no traceability row in matrix")
                        .build());
            }
        }

        return issues;
    }

    private List<TraceabilityIssue> highRiskIssues(List<FeatureTraceabilityRow> matrix) {
        List<TraceabilityIssue> issues = new ArrayList<>();
        for (FeatureTraceabilityRow row : matrix) {
            if (row.getCoveragePercent() < highRiskThreshold
                    && row.getBusinessImpact().ordinal() <= GapSeverity.HIGH.ordinal()) {
                issues.add(TraceabilityIssue.builder()
                        .type(TraceabilityIssueType.HIGH_RISK)
                        .module(row.getModule())
                        .featureName(row.getFeatureName())
                        .severity(row.getBusinessImpact())
                        .description(String.format(
                                "High-risk feature with %.1f%% coverage (threshold %.0f%%)",
                                row.getCoveragePercent(), highRiskThreshold))
                        .build());
            } else if (row.isHighRisk()) {
                issues.add(TraceabilityIssue.builder()
                        .type(TraceabilityIssueType.HIGH_RISK)
                        .module(row.getModule())
                        .featureName(row.getFeatureName())
                        .severity(row.getBusinessImpact())
                        .description("Business-critical feature with incomplete test traceability")
                        .build());
            }
        }
        return issues;
    }

    private static boolean hasOrphanTests(String module, List<ScannedTestReference> references) {
        return references.stream().anyMatch(r -> module.equals(r.getModule()));
    }

    private static GapSeverity severityForMissing(FeatureTraceabilityRow row, TestSuiteType suite) {
        return switch (suite) {
            case CONTRACT -> row.getBusinessImpact().ordinal() <= GapSeverity.HIGH.ordinal()
                    ? GapSeverity.HIGH : GapSeverity.MEDIUM;
            case SMOKE, REGRESSION -> row.getBusinessImpact() == GapSeverity.CRITICAL
                    ? GapSeverity.CRITICAL : GapSeverity.HIGH;
            case UI -> GapSeverity.MEDIUM;
        };
    }
}
