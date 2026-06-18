package com.flowiq.agents.prreview;

import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.prreview.analyzers.ApiReviewAnalyzer;
import com.flowiq.agents.prreview.analyzers.AutomationReviewAnalyzer;
import com.flowiq.agents.prreview.analyzers.BackendReviewAnalyzer;
import com.flowiq.agents.prreview.analyzers.PrReviewVerdictEvaluator;
import com.flowiq.agents.prreview.analyzers.QualityReviewAnalyzer;
import com.flowiq.agents.prreview.analyzers.UiReviewAnalyzer;
import com.flowiq.agents.prreview.config.PullRequestReviewAgentConfig;
import com.flowiq.agents.prreview.model.PrChangedArtifact;
import com.flowiq.agents.prreview.model.PrChangedArtifactType;
import com.flowiq.agents.prreview.model.PrReviewCategory;
import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewSeverity;
import com.flowiq.agents.prreview.model.PrReviewVerdict;
import com.flowiq.agents.prreview.model.PullRequestReviewResult;
import com.flowiq.agents.prreview.report.PullRequestReviewReportGenerator;
import com.flowiq.agents.prreview.scanner.PrReviewContext;
import com.flowiq.agents.prreview.scanner.PullRequestChangeScanner;
import com.flowiq.agents.prreview.scanner.SourceInventory;
import com.flowiq.agents.prreview.scanner.SourceInventoryScanner;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestReviewAgentTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/pr-review-test.properties")
    interface PrReviewTestConfig extends PullRequestReviewAgentConfig {
    }

    private static PullRequestReviewAgentConfig testConfig() {
        return ConfigFactory.create(PrReviewTestConfig.class);
    }

    @Test(groups = "unit")
    public void changeScannerShouldLoadManifestFiles() {
        PullRequestReviewAgentConfig config = testConfig();
        List<String> changedFiles = new PullRequestChangeScanner(config).loadChangedFiles();

        assertThat(changedFiles).hasSize(4);
        assertThat(changedFiles).anyMatch(f -> f.contains("ImportsController"));
        assertThat(changedFiles).anyMatch(f -> f.contains("ImportUploadRequest"));
    }

    @Test(groups = "unit")
    public void changeScannerShouldMapApiEndpointsAndDtoArtifacts() {
        PullRequestReviewAgentConfig config = testConfig();
        List<ApiChange> apiChanges = List.of(
                ApiChange.endpoint(ChangeType.ADDED_ENDPOINT, "POST", "/imports/upload",
                        "New CSV upload endpoint"),
                ApiChange.schema(ChangeType.MODIFIED_REQUEST_SCHEMA, "ImportUploadRequest",
                        "Added required field fileName", false));

        List<PrChangedArtifact> artifacts = new PullRequestChangeScanner(config).scan(List.of(), apiChanges);

        assertThat(artifacts).anyMatch(a -> a.getType() == PrChangedArtifactType.ENDPOINT
                && a.isNewlyAdded()
                && "/imports/upload".equals(a.getEndpointPath()));
        assertThat(artifacts).anyMatch(a -> a.getType() == PrChangedArtifactType.DTO
                && "ImportUploadRequest".equals(a.getSchemaName()));
    }

    @Test(groups = "unit")
    public void apiReviewAnalyzerShouldFlagMissingContractAuthAndNegative() {
        PullRequestReviewAgentConfig config = testConfig();
        PrChangedArtifact endpoint = PrChangedArtifact.builder()
                .artifactId("imports-post-upload")
                .name("POST /imports/upload")
                .type(PrChangedArtifactType.ENDPOINT)
                .module("imports")
                .httpMethod("POST")
                .endpointPath("/imports/upload")
                .newlyAdded(true)
                .build();

        PrReviewContext context = PrReviewContext.builder()
                .changedFiles(List.of())
                .apiChanges(List.of())
                .artifact(endpoint)
                .testReferences(List.of())
                .sourceInventory(SourceInventory.builder().build())
                .dataSourcesSummary("unit-test")
                .build();

        List<PrReviewFinding> findings = new ApiReviewAnalyzer(config).analyze(context);

        assertThat(findings).extracting(PrReviewFinding::getTitle)
                .contains("Endpoint without contract test coverage",
                        "Endpoint without authorization test coverage",
                        "Endpoint without negative test coverage");
        assertThat(findings.stream().filter(f -> f.getTitle().contains("contract")).findFirst())
                .get()
                .satisfies(f -> {
                    assertThat(f.getSeverity()).isEqualTo(PrReviewSeverity.CRITICAL);
                    assertThat(f.isBlocking()).isTrue();
                });
    }

    @Test(groups = "unit")
    public void apiReviewAnalyzerShouldFlagDtoChangedWithoutSchemaUpdate() {
        PullRequestReviewAgentConfig config = testConfig();
        PrChangedArtifact dto = PrChangedArtifact.builder()
                .artifactId("imports-dto")
                .name("ImportUploadRequest")
                .type(PrChangedArtifactType.DTO)
                .module("imports")
                .schemaName("ImportUploadRequest")
                .filePath("src/main/java/com/flowiq/imports/ImportUploadRequest.java")
                .build();

        PrReviewContext context = PrReviewContext.builder()
                .changedFiles(List.of(dto.getFilePath()))
                .apiChanges(List.of())
                .artifact(dto)
                .testReferences(List.of())
                .sourceInventory(SourceInventory.builder().build())
                .dataSourcesSummary("unit-test")
                .build();

        List<PrReviewFinding> findings = new ApiReviewAnalyzer(config).analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().equals("DTO changed without schema update")
                && f.getCategory() == PrReviewCategory.API_REVIEW);
    }

    @Test(groups = "unit")
    public void backendReviewAnalyzerShouldDetectControllerWithoutService() {
        SourceInventoryScanner inventoryScanner = new SourceInventoryScanner(testConfig());
        PrChangedArtifact controller = PrChangedArtifact.builder()
                .artifactId("imports-controller")
                .name("ImportsController")
                .type(PrChangedArtifactType.CONTROLLER)
                .module("imports")
                .filePath("src/main/java/com/flowiq/imports/ImportsController.java")
                .build();

        PrReviewContext context = PrReviewContext.builder()
                .changedFiles(List.of(controller.getFilePath()))
                .apiChanges(List.of())
                .artifact(controller)
                .testReferences(List.of())
                .sourceInventory(SourceInventory.builder()
                        .serviceClass("ImportService")
                        .build())
                .dataSourcesSummary("unit-test")
                .build();

        List<PrReviewFinding> findings = new BackendReviewAnalyzer(inventoryScanner).analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().equals("Controller without backing service")
                && f.getSeverity() == PrReviewSeverity.CRITICAL);
    }

    @Test(groups = "unit")
    public void automationReviewAnalyzerShouldFlagMissingSmokeAndRegression() {
        PullRequestReviewAgentConfig config = testConfig();
        BusinessImpactPrioritizer prioritizer = new BusinessImpactPrioritizer(
                ConfigFactory.create(TestGapAgentConfig.class));

        PrChangedArtifact endpoint = PrChangedArtifact.builder()
                .artifactId("imports-post-upload")
                .name("POST /imports/upload")
                .type(PrChangedArtifactType.ENDPOINT)
                .module("imports")
                .httpMethod("POST")
                .endpointPath("/imports/upload")
                .newlyAdded(true)
                .build();

        PrReviewContext context = PrReviewContext.builder()
                .changedFiles(List.of())
                .apiChanges(List.of())
                .artifact(endpoint)
                .testReferences(List.of())
                .sourceInventory(SourceInventory.builder().build())
                .dataSourcesSummary("unit-test")
                .build();

        List<PrReviewFinding> findings = new AutomationReviewAnalyzer(config, prioritizer).analyze(context);

        assertThat(findings).extracting(PrReviewFinding::getTitle)
                .contains("Endpoint without smoke test coverage",
                        "Endpoint without regression test coverage");
    }

    @Test(groups = "unit")
    public void uiReviewAnalyzerShouldDetectXpathAndMissingTestId() throws Exception {
        String fixture = Files.readString(Path.of(
                "src/test/resources/agents/prreview/fixtures/ImportsPage.java"));

        PrChangedArtifact page = PrChangedArtifact.builder()
                .artifactId("imports-page")
                .name("Imports Page")
                .type(PrChangedArtifactType.PAGE)
                .module("imports")
                .filePath("src/main/java/com/flowiq/pages/ImportsPage.java")
                .sourceContent(fixture)
                .build();

        PrReviewContext context = PrReviewContext.builder()
                .changedFiles(List.of(page.getFilePath()))
                .apiChanges(List.of())
                .artifact(page)
                .testReferences(List.of())
                .sourceInventory(SourceInventory.builder()
                        .pageObjectFile(page.getFilePath())
                        .pageClass("ImportsPage")
                        .build())
                .dataSourcesSummary("unit-test")
                .build();

        List<PrReviewFinding> findings = new UiReviewAnalyzer().analyze(context);

        assertThat(findings).extracting(PrReviewFinding::getTitle)
                .contains("XPath locator usage detected",
                        "Potentially unstable CSS selector",
                        "Missing data-testid based locators");
    }

    @Test(groups = "unit")
    public void qualityReviewAnalyzerShouldDetectDuplicatedEndpointTests() {
        ScannedTestReference first = ScannedTestReference.builder()
                .className("ImportsSmokeApiTest")
                .module("imports")
                .suites(EnumSet.of(TestSuiteType.SMOKE))
                .method("POST")
                .path("/imports/upload")
                .build();
        ScannedTestReference second = ScannedTestReference.builder()
                .className("ImportsRegressionApiTest")
                .module("imports")
                .suites(EnumSet.of(TestSuiteType.REGRESSION))
                .method("POST")
                .path("/imports/upload")
                .build();

        PrReviewContext context = PrReviewContext.builder()
                .changedFiles(List.of())
                .apiChanges(List.of())
                .testReferences(List.of(first, second))
                .sourceInventory(SourceInventory.builder().build())
                .dataSourcesSummary("unit-test")
                .build();

        List<PrReviewFinding> findings = new QualityReviewAnalyzer().analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().equals("Duplicated test coverage for the same endpoint"));
    }

    @Test(groups = "unit")
    public void verdictEvaluatorShouldMapSeveritiesToVerdicts() {
        PrReviewVerdictEvaluator evaluator = new PrReviewVerdictEvaluator();

        List<PrReviewFinding> critical = List.of(PrReviewFinding.builder()
                .category(PrReviewCategory.API_REVIEW)
                .area(com.flowiq.agents.prreview.model.PrReviewArea.QA)
                .severity(PrReviewSeverity.CRITICAL)
                .title("Blocking")
                .location("POST /imports/upload")
                .recommendation("Fix")
                .blocking(true)
                .build());

        List<PrReviewFinding> medium = List.of(PrReviewFinding.builder()
                .category(PrReviewCategory.UI_REVIEW)
                .area(com.flowiq.agents.prreview.model.PrReviewArea.ARCHITECTURE)
                .severity(PrReviewSeverity.MEDIUM)
                .title("CSS selector")
                .location("ImportsPage")
                .recommendation("Use test id")
                .blocking(false)
                .build());

        assertThat(evaluator.evaluate(critical)).isEqualTo(PrReviewVerdict.REJECTED);
        assertThat(evaluator.evaluate(medium)).isEqualTo(PrReviewVerdict.APPROVED_WITH_RISK);
        assertThat(evaluator.evaluate(List.of())).isEqualTo(PrReviewVerdict.APPROVED);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        PullRequestReviewAgentConfig config = testConfig();
        PullRequestReviewResult result = PullRequestReviewResult.builder()
                .reviewedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .verdict(PrReviewVerdict.REJECTED)
                .findingsCount(2)
                .criticalFindings(1)
                .highFindings(1)
                .changedFile("src/main/java/com/flowiq/imports/ImportsController.java")
                .finding(PrReviewFinding.builder()
                        .category(PrReviewCategory.API_REVIEW)
                        .area(com.flowiq.agents.prreview.model.PrReviewArea.QA)
                        .severity(PrReviewSeverity.CRITICAL)
                        .title("Endpoint without contract test coverage")
                        .location("POST /imports/upload")
                        .recommendation("Add contract tests")
                        .blocking(true)
                        .build())
                .finding(PrReviewFinding.builder()
                        .category(PrReviewCategory.BACKEND_REVIEW)
                        .area(com.flowiq.agents.prreview.model.PrReviewArea.ARCHITECTURE)
                        .severity(PrReviewSeverity.CRITICAL)
                        .title("Controller without backing service")
                        .location("ImportsController.java")
                        .recommendation("Add ImportsService")
                        .blocking(true)
                        .build())
                .summaryLine("Pull Request review verdict: REJECTED.")
                .recommendation("Do not merge until blocking findings are resolved.")
                .dataSourcesSummary("unit-test fixtures")
                .pullRequestSummary("4 changed file(s), 1 API change(s).")
                .build();

        Path reportPath = new PullRequestReviewReportGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Pull Request Review");
        assertThat(content).contains("## Executive Summary");
        assertThat(content).contains("## Files Changed");
        assertThat(content).contains("## Detected Risks");
        assertThat(content).contains("## Missing Coverage");
        assertThat(content).contains("## Architecture Findings");
        assertThat(content).contains("## QA Findings");
        assertThat(content).contains("## Recommendation");
        assertThat(content).contains("REJECTED");
    }

    @Test(groups = "unit")
    public void agentShouldRunWithManifestAndApiChanges() {
        PullRequestReviewAgentConfig config = testConfig();
        List<String> changedFiles = new PullRequestChangeScanner(config).loadChangedFiles();
        List<ApiChange> apiChanges = List.of(
                ApiChange.endpoint(ChangeType.ADDED_ENDPOINT, "POST", "/imports/upload",
                        "New CSV upload endpoint"));

        PullRequestReviewResult result = new PullRequestReviewAgent(config)
                .run(changedFiles, apiChanges, "unit-test fixtures");

        assertThat(result.getChangedFiles()).isNotEmpty();
        assertThat(result.getFindingsCount()).isGreaterThan(0);
        assertThat(result.getVerdict()).isIn(
                PrReviewVerdict.REJECTED, PrReviewVerdict.APPROVED_WITH_RISK, PrReviewVerdict.APPROVED);
        assertThat(result.getExecutiveSummary()).isNotEmpty();
        assertThat(result.getRecommendation()).isNotBlank();
    }
}
