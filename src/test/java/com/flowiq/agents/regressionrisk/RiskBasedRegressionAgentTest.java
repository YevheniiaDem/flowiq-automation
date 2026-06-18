package com.flowiq.agents.regressionrisk;

import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.regressionrisk.analyzer.BusinessCriticalityAnalyzer;
import com.flowiq.agents.regressionrisk.analyzer.ChangeImpactAnalyzer;
import com.flowiq.agents.regressionrisk.config.RegressionRiskAgentConfig;
import com.flowiq.agents.regressionrisk.model.AffectedTests;
import com.flowiq.agents.regressionrisk.model.ModuleChangeImpact;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;
import com.flowiq.agents.regressionrisk.model.ReleaseChangeContext;
import com.flowiq.agents.regressionrisk.model.RiskBasedRegressionResult;
import com.flowiq.agents.regressionrisk.report.RegressionRiskReportGenerator;
import com.flowiq.agents.regressionrisk.scanner.ReleaseChangeScanner;
import com.flowiq.agents.regressionrisk.scorer.RegressionRiskScorer;
import com.flowiq.agents.regressionrisk.selector.RegressionSelector;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RiskBasedRegressionAgentTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/regression-risk-test.properties")
    interface TestRegressionRiskConfig extends RegressionRiskAgentConfig {
    }

    private static RegressionRiskAgentConfig testConfig() {
        return ConfigFactory.create(TestRegressionRiskConfig.class);
    }

    @Test(groups = "unit")
    public void releaseChangeScannerShouldDetectBackendAndFrontendModules() {
        RegressionRiskAgentConfig config = testConfig();
        List<String> changedFiles = new ReleaseChangeScanner(config).loadChangedFiles();
        ReleaseChangeContext context = new ReleaseChangeScanner(config).scan(changedFiles, List.of());

        assertThat(changedFiles).hasSize(4);
        assertThat(context.getBackendModules()).contains("tasks", "imports");
        assertThat(context.getFrontendModules()).contains("dashboard");
    }

    @Test(groups = "unit")
    public void changeImpactAnalyzerShouldMapAffectedTestSuites() {
        ReleaseChangeContext context = ReleaseChangeContext.builder()
                .backendModule("tasks")
                .apiChange(ApiChange.endpoint(ChangeType.ADDED_ENDPOINT, "POST", "/tasks", "New task endpoint"))
                .build();

        Map<String, ChangeImpactAnalyzer.ModuleImpactDraft> impacts =
                new ChangeImpactAnalyzer().analyze(context);

        assertThat(impacts).containsKey("tasks");
        AffectedTests tests = impacts.get("tasks").getAllAffectedTests();
        assertThat(tests.getSmokeTests()).contains("TasksSmokeApiTest");
        assertThat(tests.getContractTests()).contains("TasksContractTest");
        assertThat(tests.getRegressionTests()).contains("TasksRegressionTest");
        assertThat(tests.getUiTests()).contains("TasksUiSmokeTest");
    }

    @Test(groups = "unit")
    public void businessCriticalityAnalyzerShouldEscalateBreakingChanges() {
        BusinessCriticalityAnalyzer analyzer = new BusinessCriticalityAnalyzer();

        GapSeverity tasksRisk = analyzer.analyze("tasks", true, false, List.of(
                ApiChange.builder()
                        .type(ChangeType.MODIFIED_RESPONSE_SCHEMA)
                        .method("GET")
                        .path("/tasks")
                        .description("Response schema changed")
                        .breaking(true)
                        .build()));
        GapSeverity importsRisk = analyzer.analyze("imports", true, false, List.of(
                ApiChange.endpoint(ChangeType.ADDED_ENDPOINT, "POST", "/imports/upload", "New upload")));

        assertThat(tasksRisk).isEqualTo(GapSeverity.CRITICAL);
        assertThat(importsRisk).isIn(GapSeverity.MEDIUM, GapSeverity.HIGH);
    }

    @Test(groups = "unit")
    public void regressionSelectorShouldSelectHighRiskSuites() {
        RegressionRiskAgentConfig config = testConfig();
        ChangeImpactAnalyzer.ModuleImpactDraft draft = new ChangeImpactAnalyzer.ModuleImpactDraft("tasks");
        draft.setBackendChanged(true);
        draft.setAllAffectedTests(AffectedTests.builder()
                .smokeTest("TasksSmokeApiTest")
                .contractTest("TasksContractTest")
                .regressionTest("TasksRegressionTest")
                .uiTest("TasksUiSmokeTest")
                .build());

        ModuleChangeImpact plan = new RegressionSelector(config).select(draft, GapSeverity.HIGH);

        assertThat(plan.getSelectedTests().getSmokeTests()).isNotEmpty();
        assertThat(plan.getSelectedTests().getContractTests()).isNotEmpty();
        assertThat(plan.getSelectedTests().getRegressionTests()).isNotEmpty();
        assertThat(plan.getEstimatedExecutionMinutes()).isGreaterThan(0);
        assertThat(plan.getRecommendedRegressionScope()).contains("HIGH");
    }

    @Test(groups = "unit")
    public void regressionRiskScorerShouldRecommendPartialRegression() {
        RegressionRiskAgentConfig config = testConfig();
        List<ModuleChangeImpact> plans = List.of(
                ModuleChangeImpact.builder()
                        .module("tasks")
                        .risk(GapSeverity.HIGH)
                        .selectedTests(AffectedTests.builder().smokeTest("TasksSmokeApiTest").build())
                        .estimatedExecutionMinutes(2)
                        .build(),
                ModuleChangeImpact.builder()
                        .module("imports")
                        .risk(GapSeverity.MEDIUM)
                        .selectedTests(AffectedTests.builder().regressionTest("ImportsRegressionTest").build())
                        .estimatedExecutionMinutes(8)
                        .build());

        RegressionScopeRecommendation recommendation = new RegressionRiskScorer(config).recommend(plans, List.of());

        assertThat(recommendation).isIn(
                RegressionScopeRecommendation.PARTIAL_REGRESSION,
                RegressionScopeRecommendation.FULL_REGRESSION);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        RegressionRiskAgentConfig config = testConfig();
        RiskBasedRegressionResult result = RiskBasedRegressionResult.builder()
                .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .recommendation(RegressionScopeRecommendation.PARTIAL_REGRESSION)
                .modulesAnalyzed(1)
                .totalSelectedTestClasses(3)
                .estimatedTotalExecutionMinutes(13)
                .modulePlan(ModuleChangeImpact.builder()
                        .module("tasks")
                        .backendChanged(true)
                        .risk(GapSeverity.HIGH)
                        .allAffectedTests(AffectedTests.builder()
                                .smokeTest("TasksSmokeApiTest")
                                .contractTest("TasksContractTest")
                                .regressionTest("TasksRegressionTest")
                                .build())
                        .selectedTests(AffectedTests.builder()
                                .smokeTest("TasksSmokeApiTest")
                                .regressionTest("TasksRegressionTest")
                                .build())
                        .recommendedRegressionScope("HIGH — smoke + regression")
                        .estimatedExecutionMinutes(10)
                        .build())
                .summaryLine("Recommended release validation: PARTIAL REGRESSION.")
                .dataSourcesSummary("test-fixtures")
                .build();

        Path reportPath = new RegressionRiskReportGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Risk-Based Regression Plan");
        assertThat(content).contains("PARTIAL REGRESSION");
        assertThat(content).contains("**Changed Module**");
        assertThat(content).contains("**Risk**");
        assertThat(content).contains("**Affected Tests**");
        assertThat(content).contains("**Recommended Regression Scope**");
        assertThat(content).contains("**Estimated Execution Time**");
    }

    @Test(groups = "unit")
    public void agentShouldRunWithManifestAndApiChanges() {
        RegressionRiskAgentConfig config = testConfig();
        List<String> changedFiles = new ReleaseChangeScanner(config).loadChangedFiles();
        List<ApiChange> apiChanges = List.of(
                ApiChange.endpoint(ChangeType.ADDED_ENDPOINT, "POST", "/imports/upload",
                        "New CSV upload endpoint"),
                ApiChange.endpoint(ChangeType.MODIFIED_RESPONSE_SCHEMA, "GET", "/tasks/{id}",
                        "Task response schema updated"));

        ReleaseChangeContext context = new ReleaseChangeScanner(config).scan(changedFiles, apiChanges);
        RiskBasedRegressionResult result = new RiskBasedRegressionAgent(config)
                .run(context, "regression-risk test fixtures");

        assertThat(result.getModulesAnalyzed()).isGreaterThan(0);
        assertThat(result.getRecommendation()).isIn(
                RegressionScopeRecommendation.FULL_REGRESSION,
                RegressionScopeRecommendation.PARTIAL_REGRESSION,
                RegressionScopeRecommendation.SMOKE_ONLY);
        assertThat(result.getModulePlans()).isNotEmpty();
        assertThat(result.getModulePlans().stream().map(ModuleChangeImpact::getModule))
                .contains("tasks", "imports", "dashboard");
        assertThat(result.getTotalSelectedTestClasses()).isGreaterThan(0);
        assertThat(result.getEstimatedTotalExecutionMinutes()).isGreaterThan(0);
    }
}
