package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioPriority;
import com.flowiq.agents.generator.model.ScenarioRisk;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;

import java.util.ArrayList;
import java.util.List;

public class PositiveScenarioGenerator extends AbstractScenarioGenerator {

    @Override
    public ScenarioGeneratorType type() {
        return ScenarioGeneratorType.POSITIVE;
    }

    @Override
    public List<TestScenario> generate(EndpointTestContext context) {
        if (!isUncovered(context, ScenarioType.POSITIVE)) {
            return List.of();
        }
        String method = context.getOperation().method();
        String path = context.getNormalizedPath();
        List<TestScenario> scenarios = new ArrayList<>();

        scenarios.add(TestScenario.builder()
                .id(scenarioId(context, ScenarioType.POSITIVE, "happy-path"))
                .title("Happy path — " + method + " " + path)
                .type(ScenarioType.POSITIVE)
                .module(context.getModule())
                .endpoint(path)
                .httpMethod(method)
                .precondition("Valid test user credentials are available in the target environment")
                .precondition(context.isRequiresAuth()
                        ? "User is authenticated with a valid JWT token"
                        : "Endpoint is publicly accessible")
                .precondition("Backend API is healthy and test data prerequisites are seeded")
                .step("Send " + method + " request to " + path + " with valid payload and headers")
                .step("Capture response status, body, and response time")
                .expectedResult(successStatus(method) + " with response body matching OpenAPI contract"
                        + (context.getResponseSchema() != null
                        ? " and JSON Schema `" + context.getResponseSchema().getFilePath() + "`"
                        : ""))
                .priority(priorityFor(context))
                .risk(riskFor(context))
                .build());

        if (context.getResponseSchema() != null && !context.getResponseSchema().getRequiredFields().isEmpty()) {
            scenarios.add(TestScenario.builder()
                    .id(scenarioId(context, ScenarioType.POSITIVE, "required-fields"))
                    .title("Contract validation — required response fields for " + method + " " + path)
                    .type(ScenarioType.POSITIVE)
                    .module(context.getModule())
                    .endpoint(path)
                    .httpMethod(method)
                    .precondition("Same as happy path")
                    .step("Execute " + method + " " + path + " with valid input")
                    .step("Assert all required fields are present: "
                            + String.join(", ", context.getResponseSchema().getRequiredFields()))
                    .expectedResult("All required fields populated with correct types; no nulls on required keys")
                    .priority(ScenarioPriority.P2)
                    .risk(ScenarioRisk.MEDIUM)
                    .build());
        }
        return scenarios;
    }

    private static String successStatus(String method) {
        return "POST".equals(method) ? "HTTP 201 Created" : "HTTP 200 OK";
    }

    private static ScenarioPriority priorityFor(EndpointTestContext context) {
        return "auth".equals(context.getModule()) || "transactions".equals(context.getModule())
                ? ScenarioPriority.P1 : ScenarioPriority.P2;
    }

    private static ScenarioRisk riskFor(EndpointTestContext context) {
        return "auth".equals(context.getModule()) ? ScenarioRisk.CRITICAL
                : "transactions".equals(context.getModule()) ? ScenarioRisk.HIGH : ScenarioRisk.MEDIUM;
    }
}
