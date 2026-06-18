package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioPriority;
import com.flowiq.agents.generator.model.ScenarioRisk;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;

import java.util.ArrayList;
import java.util.List;

public class NegativeScenarioGenerator extends AbstractScenarioGenerator {

    @Override
    public ScenarioGeneratorType type() {
        return ScenarioGeneratorType.NEGATIVE;
    }

    @Override
    public List<TestScenario> generate(EndpointTestContext context) {
        if (!isUncovered(context, ScenarioType.NEGATIVE)) {
            return List.of();
        }
        String method = context.getOperation().method();
        String path = context.getNormalizedPath();
        List<TestScenario> scenarios = new ArrayList<>();

        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            scenarios.add(TestScenario.builder()
                    .id(scenarioId(context, ScenarioType.NEGATIVE, "missing-required"))
                    .title("Validation error — missing required request fields for " + method + " " + path)
                    .type(ScenarioType.NEGATIVE)
                    .module(context.getModule())
                    .endpoint(path)
                    .httpMethod(method)
                    .precondition(context.isRequiresAuth() ? "Valid auth token available" : "No auth required")
                    .step("Build request body omitting one required field at a time")
                    .step("Send " + method + " " + path)
                    .expectedResult("HTTP 400 Bad Request with field-level validation message")
                    .priority(ScenarioPriority.P2)
                    .risk(ScenarioRisk.MEDIUM)
                    .build());

            scenarios.add(TestScenario.builder()
                    .id(scenarioId(context, ScenarioType.NEGATIVE, "invalid-types"))
                    .title("Validation error — invalid field types for " + method + " " + path)
                    .type(ScenarioType.NEGATIVE)
                    .module(context.getModule())
                    .endpoint(path)
                    .httpMethod(method)
                    .precondition("Valid auth if required")
                    .step("Send request with invalid types (string instead of number, malformed date)")
                    .expectedResult("HTTP 400 Bad Request; no partial persistence")
                    .priority(ScenarioPriority.P3)
                    .risk(ScenarioRisk.LOW)
                    .build());
        }

        if (path.contains("{")) {
            scenarios.add(TestScenario.builder()
                    .id(scenarioId(context, ScenarioType.NEGATIVE, "not-found"))
                    .title("Resource not found — " + method + " " + path)
                    .type(ScenarioType.NEGATIVE)
                    .module(context.getModule())
                    .endpoint(path)
                    .httpMethod(method)
                    .precondition("Authenticated user if required")
                    .step("Replace path parameter with non-existent ID (e.g. 999999999)")
                    .step("Send " + method + " request")
                    .expectedResult("HTTP 404 Not Found with consistent error envelope")
                    .priority(ScenarioPriority.P2)
                    .risk(ScenarioRisk.MEDIUM)
                    .build());
        }

        return scenarios;
    }
}
