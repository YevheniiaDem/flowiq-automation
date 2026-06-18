package com.flowiq.agents.architecture;

import com.flowiq.agents.architecture.checker.ArchitectureDriftCheckerPipeline;
import com.flowiq.agents.architecture.checker.EndpointDocumentationDriftChecker;
import com.flowiq.agents.architecture.config.ArchitectureDriftAgentConfig;
import com.flowiq.agents.architecture.inventory.ApiEndpointRef;
import com.flowiq.agents.architecture.inventory.ArchitectureContext;
import com.flowiq.agents.architecture.inventory.ArchitectureInventoryLoader;
import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;
import com.flowiq.agents.architecture.model.ArchitectureDriftResult;
import com.flowiq.agents.architecture.model.DriftIssueType;
import com.flowiq.agents.architecture.model.DriftSeverity;
import com.flowiq.agents.architecture.report.ArchitectureDriftReportGenerator;
import com.flowiq.agents.architecture.scorer.ArchitectureHealthScorer;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ArchitectureDriftAgentTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/architecture-drift-test.properties")
    interface TestArchitectureDriftConfig extends ArchitectureDriftAgentConfig {
    }

    private static ArchitectureDriftAgentConfig testConfig() {
        return ConfigFactory.create(TestArchitectureDriftConfig.class);
    }

    @Test(groups = "unit")
    public void inventoryLoaderShouldIndexOpenApiAndDocumentation() {
        ArchitectureContext context = new ArchitectureInventoryLoader(testConfig()).load();

        assertThat(context.getOpenApiEndpoints()).hasSize(3);
        assertThat(context.getDocumentedEndpoints()).isNotEmpty();
        assertThat(context.getServices()).extracting("name").contains("OrphanService");
        assertThat(context.getPages()).extracting("name").contains("OrphanPage");
    }

    @Test(groups = "unit")
    public void endpointDriftCheckerShouldFindUndocumentedOpenApiEndpoints() {
        ArchitectureContext context = ArchitectureContext.builder()
                .openApiEndpoint(new ApiEndpointRef("POST", "/imports/upload", "OpenAPI"))
                .documentedEndpoint(new ApiEndpointRef("GET", "/tasks", "CONTRACT-COVERAGE.md"))
                .build();

        List<ArchitectureDriftIssue> issues = new EndpointDocumentationDriftChecker().check(context);

        assertThat(issues).anyMatch(issue -> issue.getType() == DriftIssueType.ENDPOINT_WITHOUT_DOCUMENTATION
                && issue.getLocation().contains("/imports/upload"));
    }

    @Test(groups = "unit")
    public void endpointDriftCheckerShouldFindDocumentationWithoutEndpoint() {
        ArchitectureContext context = ArchitectureContext.builder()
                .openApiEndpoint(new ApiEndpointRef("GET", "/tasks", "OpenAPI"))
                .documentedEndpoint(new ApiEndpointRef("GET", "/legacy/export", "stale-endpoint.md"))
                .build();

        List<ArchitectureDriftIssue> issues = new EndpointDocumentationDriftChecker().check(context);

        assertThat(issues).anyMatch(issue -> issue.getType() == DriftIssueType.DOCUMENTATION_WITHOUT_ENDPOINT);
    }

    @Test(groups = "unit")
    public void healthScorerShouldComputeArchitectureHealthScore() {
        List<ArchitectureDriftIssue> issues = List.of(
                ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.ENDPOINT_WITHOUT_DOCUMENTATION)
                        .issue("missing doc")
                        .severity(DriftSeverity.HIGH)
                        .location("/imports/upload")
                        .recommendation("document")
                        .build(),
                ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.DTO_WITHOUT_SCHEMA)
                        .issue("missing schema")
                        .severity(DriftSeverity.LOW)
                        .location("OrphanRequest")
                        .recommendation("add schema")
                        .build());

        int score = new ArchitectureHealthScorer().score(issues);

        assertThat(score).isBetween(80, 95);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        ArchitectureDriftAgentConfig config = testConfig();
        ArchitectureDriftResult result = ArchitectureDriftResult.builder()
                .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .architectureHealthScore(82)
                .issuesFound(1)
                .criticalIssues(0)
                .issue(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.PAGE_WITHOUT_UI_TESTS)
                        .issue("Page object has no UI smoke test coverage")
                        .severity(DriftSeverity.MEDIUM)
                        .location("src/main/java/com/flowiq/pages/OrphanPage.java")
                        .recommendation("Add UI smoke test.")
                        .build())
                .summaryLine("Architecture health score: 82/100.")
                .dataSourcesSummary("test-fixtures")
                .build();

        Path reportPath = new ArchitectureDriftReportGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Architecture Drift Analysis");
        assertThat(content).contains("Architecture Health Score");
        assertThat(content).contains("**Issue**");
        assertThat(content).contains("**Severity**");
        assertThat(content).contains("**Location**");
        assertThat(content).contains("**Recommendation**");
    }

    @Test(groups = "unit")
    public void agentShouldDetectDriftFromFixtures() {
        ArchitectureDriftAgentConfig config = testConfig();
        ArchitectureContext context = new ArchitectureInventoryLoader(config).load();
        ArchitectureDriftResult result = new ArchitectureDriftAgent(config)
                .run(context, "architecture drift fixtures");

        assertThat(result.getIssuesFound()).isGreaterThan(0);
        assertThat(result.getArchitectureHealthScore()).isBetween(0, 100);
        assertThat(result.getIssues().stream().map(ArchitectureDriftIssue::getType))
                .contains(
                        DriftIssueType.ENDPOINT_WITHOUT_DOCUMENTATION,
                        DriftIssueType.DOCUMENTATION_WITHOUT_ENDPOINT,
                        DriftIssueType.SERVICE_WITHOUT_TESTS,
                        DriftIssueType.PAGE_WITHOUT_UI_TESTS,
                        DriftIssueType.DTO_WITHOUT_SCHEMA);
        assertThat(new ArchitectureDriftCheckerPipeline().analyze(context)).isNotEmpty();
    }
}
