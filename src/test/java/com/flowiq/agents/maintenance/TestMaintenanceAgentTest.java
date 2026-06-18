package com.flowiq.agents.maintenance;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import com.flowiq.agents.maintenance.analyzers.DeadDtoAnalyzer;
import com.flowiq.agents.maintenance.analyzers.DeadPageObjectAnalyzer;
import com.flowiq.agents.maintenance.analyzers.DeadTestAnalyzer;
import com.flowiq.agents.maintenance.analyzers.DuplicateTestAnalyzer;
import com.flowiq.agents.maintenance.analyzers.LocatorQualityAnalyzer;
import com.flowiq.agents.maintenance.analyzers.MaintenanceAnalyzerPipeline;
import com.flowiq.agents.maintenance.analyzers.NamingConventionAnalyzer;
import com.flowiq.agents.maintenance.analyzers.OversizedPageObjectAnalyzer;
import com.flowiq.agents.maintenance.analyzers.OversizedTestClassAnalyzer;
import com.flowiq.agents.maintenance.analyzers.TestComplexityAnalyzer;
import com.flowiq.agents.maintenance.config.TestMaintenanceAgentConfig;
import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceHealthCategory;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedDto;
import com.flowiq.agents.maintenance.model.ScannedPageObject;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.maintenance.model.TestMaintenanceResult;
import com.flowiq.agents.maintenance.report.TestMaintenanceReportGenerator;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;
import com.flowiq.agents.maintenance.scanner.MaintenanceInventoryScanner;
import com.flowiq.agents.maintenance.scorer.MaintenanceHealthScorer;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMaintenanceAgentTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/test-maintenance-test.properties")
    interface MaintenanceTestConfig extends TestMaintenanceAgentConfig {
    }

    private static TestMaintenanceAgentConfig testConfig() {
        return ConfigFactory.create(MaintenanceTestConfig.class);
    }

    @Test(groups = "unit")
    public void inventoryScannerShouldLoadOpenApiAndAllureHistory() {
        TestMaintenanceAgentConfig config = testConfig();
        MaintenanceContext context = new MaintenanceInventoryScanner(config).scan();

        assertThat(context.getOpenApiEndpoints()).isNotEmpty();
        assertThat(context.getTestClasses()).isNotEmpty();
        assertThat(context.getPageObjects()).isNotEmpty();
        assertThat(context.getDataSourcesSummary()).contains("OpenAPI endpoints");
    }

    @Test(groups = "unit")
    public void duplicateTestAnalyzerShouldDetectDuplicateEndpointsAndAssertions() {
        ScannedTestClass first = ScannedTestClass.builder()
                .className("ImportsSmokeApiTest")
                .filePath("src/test/java/ImportsSmokeApiTest.java")
                .source("class ImportsSmokeApiTest {}")
                .endpointKey("POST /imports/upload")
                .assertion("assertThat(response.getStatusCode()).isEqualTo(201);")
                .build();
        ScannedTestClass second = ScannedTestClass.builder()
                .className("ImportsRegressionApiTest")
                .filePath("src/test/java/ImportsRegressionApiTest.java")
                .source("class ImportsRegressionApiTest {}")
                .endpointKey("POST /imports/upload")
                .assertion("assertThat(response.getStatusCode()).isEqualTo(201);")
                .build();

        MaintenanceContext context = MaintenanceContext.builder()
                .testClass(first)
                .testClass(second)
                .dataSourcesSummary("unit-test")
                .build();

        List<MaintenanceFinding> findings = new DuplicateTestAnalyzer().analyze(context);

        assertThat(findings).extracting(MaintenanceFinding::getTitle)
                .contains("Duplicate test coverage for endpoint",
                        "Duplicate assertion across test classes");
    }

    @Test(groups = "unit")
    public void deadTestAnalyzerShouldFlagRemovedEndpointCoverage() {
        ScannedTestClass test = ScannedTestClass.builder()
                .className("LegacyEndpointTest")
                .filePath("src/test/java/LegacyEndpointTest.java")
                .source("class LegacyEndpointTest {}")
                .endpointKey("DELETE /removed/legacy")
                .build();

        MaintenanceContext context = MaintenanceContext.builder()
                .testClass(test)
                .openApiEndpoint("GET /imports")
                .openApiEndpoint("POST /imports/upload")
                .dataSourcesSummary("unit-test")
                .build();

        List<MaintenanceFinding> findings = new DeadTestAnalyzer().analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().equals("Endpoint removed but tests still exist"));
    }

    @Test(groups = "unit")
    public void deadPageObjectAnalyzerShouldFlagUnreferencedPage() {
        ScannedPageObject page = ScannedPageObject.builder()
                .className("OrphanMaintenancePage")
                .filePath("src/test/resources/agents/maintenance/fixtures/OrphanMaintenancePage.java")
                .source("class OrphanMaintenancePage {}")
                .lineCount(10)
                .publicMethodCount(1)
                .build();

        MaintenanceContext context = MaintenanceContext.builder()
                .pageObject(page)
                .combinedMainAndTestSources("class UnrelatedTest {}")
                .dataSourcesSummary("unit-test")
                .build();

        List<MaintenanceFinding> findings = new DeadPageObjectAnalyzer().analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().equals("Page Object not referenced"));
    }

    @Test(groups = "unit")
    public void deadDtoAnalyzerShouldFlagUnreferencedDto() {
        ScannedDto dto = ScannedDto.builder()
                .className("OrphanMaintenanceRequest")
                .filePath("src/main/java/com/flowiq/models/OrphanMaintenanceRequest.java")
                .build();

        MaintenanceContext context = MaintenanceContext.builder()
                .dto(dto)
                .combinedMainAndTestSources("class OtherDto {}")
                .dataSourcesSummary("unit-test")
                .build();

        List<MaintenanceFinding> findings = new DeadDtoAnalyzer().analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().equals("DTO not referenced"));
    }

    @Test(groups = "unit")
    public void locatorQualityAnalyzerShouldDetectXpathAndNthChild() throws Exception {
        String source = Files.readString(Path.of(
                "src/test/resources/agents/maintenance/fixtures/OrphanMaintenancePage.java"));

        ScannedPageObject page = ScannedPageObject.builder()
                .className("OrphanMaintenancePage")
                .filePath("fixtures/OrphanMaintenancePage.java")
                .source(source)
                .lineCount(10)
                .publicMethodCount(1)
                .build();

        MaintenanceContext context = MaintenanceContext.builder()
                .pageObject(page)
                .dataSourcesSummary("unit-test")
                .build();

        List<MaintenanceFinding> findings = new LocatorQualityAnalyzer().analyze(context);

        assertThat(findings).extracting(MaintenanceFinding::getTitle)
                .contains("XPath usage detected", "nth-child selector usage detected");
    }

    @Test(groups = "unit")
    public void oversizedAnalyzersShouldFlagLargeClasses() {
        TestMaintenanceAgentConfig config = testConfig();
        ScannedTestClass largeTest = ScannedTestClass.builder()
                .className("HugeRegressionTest")
                .filePath("src/test/java/HugeRegressionTest.java")
                .source("x".repeat(400))
                .lineCount(350)
                .methodCount(20)
                .maxMethodLines(10)
                .build();
        ScannedPageObject largePage = ScannedPageObject.builder()
                .className("HugePage")
                .filePath("src/main/java/com/flowiq/pages/HugePage.java")
                .source("x".repeat(300))
                .lineCount(250)
                .publicMethodCount(5)
                .build();

        MaintenanceContext context = MaintenanceContext.builder()
                .testClass(largeTest)
                .pageObject(largePage)
                .dataSourcesSummary("unit-test")
                .build();

        assertThat(new OversizedTestClassAnalyzer(config).analyze(context))
                .anyMatch(f -> f.getTitle().equals("Large Test Class"));
        assertThat(new OversizedPageObjectAnalyzer(config).analyze(context))
                .anyMatch(f -> f.getTitle().equals("Large Page Object"));
    }

    @Test(groups = "unit")
    public void testComplexityAnalyzerShouldDetectLongMethodsAndFlakyCandidates() {
        TestMaintenanceAgentConfig config = testConfig();
        ScannedTestClass complexTest = ScannedTestClass.builder()
                .className("ComplexTest")
                .filePath("src/test/java/ComplexTest.java")
                .source("class ComplexTest {}")
                .lineCount(100)
                .methodCount(5)
                .maxMethodLines(55)
                .build();

        List<TestExecutionRecord> allure = List.of(
                TestExecutionRecord.builder()
                        .testKey("com.flowiq.api.imports.ImportsSmokeApiTest.shouldUpload")
                        .className("ImportsSmokeApiTest")
                        .methodName("shouldUpload")
                        .outcome(TestOutcome.FAILED)
                        .build(),
                TestExecutionRecord.builder()
                        .testKey("com.flowiq.api.imports.ImportsSmokeApiTest.shouldUpload")
                        .className("ImportsSmokeApiTest")
                        .methodName("shouldUpload")
                        .outcome(TestOutcome.PASSED)
                        .build());

        MaintenanceContext context = MaintenanceContext.builder()
                .testClass(complexTest)
                .allureRecord(allure.get(0))
                .allureRecord(allure.get(1))
                .dataSourcesSummary("unit-test")
                .build();

        List<MaintenanceFinding> findings = new TestComplexityAnalyzer(config).analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().equals("Long test method detected"));
        assertThat(findings).anyMatch(f -> f.getType() == MaintenanceFindingType.FLAKY);
    }

    @Test(groups = "unit")
    public void namingConventionAnalyzerShouldFlagInvalidNames() {
        ScannedTestClass invalidClass = ScannedTestClass.builder()
                .className("imports_api_check")
                .filePath("src/test/java/imports_api_check.java")
                .source("""
                        class imports_api_check {
                            @Test
                            public void uploadFile() {}
                        }
                        """)
                .lineCount(10)
                .methodCount(1)
                .maxMethodLines(5)
                .build();

        MaintenanceContext context = MaintenanceContext.builder()
                .testClass(invalidClass)
                .dataSourcesSummary("unit-test")
                .build();

        List<MaintenanceFinding> findings = new NamingConventionAnalyzer().analyze(context);

        assertThat(findings).anyMatch(f -> f.getTitle().contains("naming convention"));
    }

    @Test(groups = "unit")
    public void healthScorerShouldCalculateScoreAndCategory() {
        MaintenanceHealthScorer scorer = new MaintenanceHealthScorer();
        List<MaintenanceFinding> findings = List.of(
                MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.DEAD_CODE)
                        .severity(MaintenanceSeverity.CRITICAL)
                        .title("Dead test")
                        .location("LegacyTest")
                        .recommendation("Remove")
                        .priorityRank(1)
                        .build(),
                MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.DUPLICATE)
                        .severity(MaintenanceSeverity.MEDIUM)
                        .title("Duplicate")
                        .location("POST /imports")
                        .recommendation("Merge")
                        .priorityRank(3)
                        .build());

        int score = scorer.score(findings);

        assertThat(score).isLessThan(100);
        assertThat(scorer.categorize(score)).isIn(
                MaintenanceHealthCategory.WARNING, MaintenanceHealthCategory.CRITICAL, MaintenanceHealthCategory.GOOD);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        TestMaintenanceAgentConfig config = testConfig();
        TestMaintenanceResult result = TestMaintenanceResult.builder()
                .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .automationHealthScore(72)
                .healthCategory(MaintenanceHealthCategory.GOOD)
                .findingsCount(2)
                .deadComponents(1)
                .duplicateComponents(1)
                .flakyCandidates(0)
                .finding(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.DEAD_CODE)
                        .severity(MaintenanceSeverity.HIGH)
                        .title("DTO not referenced")
                        .location("OrphanRequest.java")
                        .recommendation("Remove DTO")
                        .priorityRank(2)
                        .build())
                .finding(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.DUPLICATE)
                        .severity(MaintenanceSeverity.MEDIUM)
                        .title("Duplicate test coverage for endpoint")
                        .location("POST /imports/upload")
                        .recommendation("Consolidate tests")
                        .priorityRank(3)
                        .build())
                .technicalDebtSummaryLine("Automation health score: 72/100 (GOOD).")
                .refactoringRecommendationLine("Split large page objects.")
                .topPriorityFixLine("DTO not referenced - OrphanRequest.java: Remove DTO")
                .dataSourcesSummary("unit-test fixtures")
                .build();

        Path reportPath = new TestMaintenanceReportGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Test Maintenance Report");
        assertThat(content).contains("## Automation Health Score");
        assertThat(content).contains("## Technical Debt Summary");
        assertThat(content).contains("## Dead Components");
        assertThat(content).contains("## Duplicate Components");
        assertThat(content).contains("## Flaky Candidates");
        assertThat(content).contains("## Refactoring Recommendations");
        assertThat(content).contains("## Top Priority Fixes");
    }

    @Test(groups = "unit")
    public void agentShouldRunFullPipelineWithTestConfig() {
        TestMaintenanceAgentConfig config = testConfig();
        TestMaintenanceResult result = new TestMaintenanceAgent(config).run();

        assertThat(result.getAutomationHealthScore()).isBetween(0, 100);
        assertThat(result.getHealthCategory()).isNotNull();
        assertThat(result.getFindingsCount()).isGreaterThanOrEqualTo(0);
        assertThat(result.getTechnicalDebtSummary()).isNotEmpty();
        assertThat(result.getTopPriorityFixes()).isNotNull();
        assertThat(new MaintenanceAnalyzerPipeline().analyze(
                new MaintenanceInventoryScanner(config).scan())).isNotNull();
    }
}
