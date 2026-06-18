package com.flowiq.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.analyzer.ChangeAnalyzerPipeline;
import com.flowiq.agents.analyzer.EndpointChangeAnalyzer;
import com.flowiq.agents.analyzer.RequiredFieldChangeAnalyzer;
import com.flowiq.agents.impact.TestImpactMapper;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.report.ApiChangeReportGenerator;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiChangeDetectionAgentTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test(groups = "unit")
    public void endpointAnalyzerShouldDetectAddedAndRemovedEndpoints() throws Exception {
        JsonNode previousSpec = readFixture("agents/previous-openapi.json");
        JsonNode currentSpec = readFixture("agents/current-openapi.json");

        assertThat(OpenApiNavigator.getOperations(previousSpec)).hasSize(2);
        assertThat(OpenApiNavigator.getOperations(currentSpec)).hasSize(2);

        List<ApiChange> changes = new EndpointChangeAnalyzer().analyze(previousSpec, currentSpec);

        assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED_ENDPOINT && "/tasks".equals(c.getPath()));
        assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.REMOVED_ENDPOINT && "/transactions".equals(c.getPath()));
    }

    @Test(groups = "unit")
    public void requiredFieldAnalyzerShouldDetectAddedRequiredFields() throws Exception {
        JsonNode previousSpec = readFixture("agents/previous-openapi.json");
        JsonNode currentSpec = readFixture("agents/current-openapi.json");

        List<ApiChange> changes = new RequiredFieldChangeAnalyzer().analyze(previousSpec, currentSpec);

        assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED_REQUIRED_FIELD
                        && "LoginRequest".equals(c.getSchema())
                        && "rememberMe".equals(c.getField()));
        assertThat(changes).anyMatch(c ->
                c.getType() == ChangeType.ADDED_REQUIRED_FIELD
                        && "AuthResponse".equals(c.getSchema())
                        && "expiresAt".equals(c.getField()));
    }

    @Test(groups = "unit")
    public void pipelineShouldDetectBreakingChangesAndEnumUpdates() throws Exception {
        JsonNode previousSpec = readFixture("agents/previous-openapi.json");
        JsonNode currentSpec = readFixture("agents/current-openapi.json");

        List<ApiChange> changes = ChangeAnalyzerPipeline.defaultPipeline().analyze(previousSpec, currentSpec);

        assertThat(changes).anyMatch(c -> c.getType() == ChangeType.ENUM_VALUE_ADDED);
        assertThat(changes).anyMatch(c -> c.getType() == ChangeType.STATUS_CODE_ADDED);
        assertThat(changes).anyMatch(ApiChange::isBreaking);
    }

    @Test(groups = "unit")
    public void impactMapperShouldMapAuthChangesToContractAndSmokeTests() throws Exception {
        JsonNode previousSpec = readFixture("agents/previous-openapi.json");
        JsonNode currentSpec = readFixture("agents/current-openapi.json");
        var config = ConfigFactory.create(com.flowiq.agents.config.AgentConfig.class);
        TestImpactMapper mapper = new TestImpactMapper(config);

        List<ApiChange> changes = ChangeAnalyzerPipeline.defaultPipeline().analyze(previousSpec, currentSpec);
        Map<TestSuiteType, List<String>> affected = mapper.mapChanges(changes);

        assertThat(affected.get(TestSuiteType.CONTRACT)).contains("AuthContractTest");
        assertThat(affected.get(TestSuiteType.SMOKE)).contains("AuthSmokeApiTest");
        assertThat(affected.get(TestSuiteType.REGRESSION)).contains("TasksRegressionTest");
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldProduceMarkdownSections() throws Exception {
        JsonNode previousSpec = readFixture("agents/previous-openapi.json");
        JsonNode currentSpec = readFixture("agents/current-openapi.json");
        var config = ConfigFactory.create(com.flowiq.agents.config.AgentConfig.class);
        TestImpactMapper mapper = new TestImpactMapper(config);
        List<ApiChange> changes = ChangeAnalyzerPipeline.defaultPipeline().analyze(previousSpec, currentSpec);

        var result = com.flowiq.agents.model.AnalysisResult.builder()
                .analyzedAt(Instant.parse("2026-06-17T10:00:00Z"))
                .changes(changes)
                .riskLevel(com.flowiq.agents.model.RiskLevel.MEDIUM)
                .affectedTests(mapper.mapChanges(changes))
                .recommendedActions(mapper.recommendedActions(changes, mapper.mapChanges(changes)))
                .baselineMissing(false)
                .build();

        var generator = new ApiChangeReportGenerator(config);
        var matrix = mapper.buildMatrix(changes);
        var reportPath = generator.generate(result, matrix);

        String content = java.nio.file.Files.readString(reportPath);
        assertThat(content).contains("# API Change Report");
        assertThat(content).contains("## Changes");
        assertThat(content).contains("## Risk Level");
        assertThat(content).contains("## Affected Tests");
        assertThat(content).contains("## Recommended Actions");
        assertThat(content).contains("**MEDIUM**");
    }

    @Test(groups = "unit")
    public void domainExtractionShouldUseFirstPathSegment() {
        assertThat(TestImpactMapper.extractDomain("/auth/login")).isEqualTo("auth");
        assertThat(TestImpactMapper.extractDomain("/business-guide/articles")).isEqualTo("business-guide");
    }

    private static JsonNode readFixture(String resource) throws Exception {
        try (InputStream input = ApiChangeDetectionAgentTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).as("fixture %s", resource).isNotNull();
            return MAPPER.readTree(input);
        }
    }
}
