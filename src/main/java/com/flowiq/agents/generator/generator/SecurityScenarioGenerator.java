package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioPriority;
import com.flowiq.agents.generator.model.ScenarioRisk;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;

import java.util.ArrayList;
import java.util.List;

public class SecurityScenarioGenerator extends AbstractScenarioGenerator {

    @Override
    public ScenarioGeneratorType type() {
        return ScenarioGeneratorType.SECURITY;
    }

    @Override
    public List<TestScenario> generate(EndpointTestContext context) {
        if (!isUncovered(context, ScenarioType.SECURITY)) {
            return List.of();
        }
        String method = context.getOperation().method();
        String path = context.getNormalizedPath();
        List<TestScenario> scenarios = new ArrayList<>();

        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            scenarios.add(TestScenario.builder()
                    .id(scenarioId(context, ScenarioType.SECURITY, "injection"))
                    .title("Security — injection payload rejected for " + method + " " + path)
                    .type(ScenarioType.SECURITY)
                    .module(context.getModule())
                    .endpoint(path)
                    .httpMethod(method)
                    .precondition("Valid auth if endpoint is secured")
                    .step("Inject SQL/XSS probe strings into string fields (e.g. `' OR 1=1 --`, `<script>`)")
                    .step("Send " + method + " request")
                    .expectedResult("HTTP 400 validation error; payload not executed or persisted")
                    .priority(ScenarioPriority.P2)
                    .risk(ScenarioRisk.HIGH)
                    .build());
        }

        scenarios.add(TestScenario.builder()
                .id(scenarioId(context, ScenarioType.SECURITY, "idor"))
                .title("Security — cross-user data access (IDOR) on " + method + " " + path)
                .type(ScenarioType.SECURITY)
                .module(context.getModule())
                .endpoint(path)
                .httpMethod(method)
                .precondition("Two test users (User A and User B) with distinct data")
                .precondition("User A is authenticated")
                .step("Attempt to access User B's resource ID via " + method + " " + path)
                .expectedResult("HTTP 403 Forbidden or 404 Not Found; no data leakage")
                .priority(path.contains("{") ? ScenarioPriority.P1 : ScenarioPriority.P3)
                .risk(path.contains("{") ? ScenarioRisk.CRITICAL : ScenarioRisk.MEDIUM)
                .build());

        return scenarios;
    }
}
