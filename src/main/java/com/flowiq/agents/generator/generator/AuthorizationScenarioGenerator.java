package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioPriority;
import com.flowiq.agents.generator.model.ScenarioRisk;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationScenarioGenerator extends AbstractScenarioGenerator {

    @Override
    public ScenarioGeneratorType type() {
        return ScenarioGeneratorType.AUTHORIZATION;
    }

    @Override
    public List<TestScenario> generate(EndpointTestContext context) {
        if (!context.isRequiresAuth() || !isUncovered(context, ScenarioType.AUTHORIZATION)) {
            return List.of();
        }
        String method = context.getOperation().method();
        String path = context.getNormalizedPath();
        List<TestScenario> scenarios = new ArrayList<>();

        scenarios.add(TestScenario.builder()
                .id(scenarioId(context, ScenarioType.AUTHORIZATION, "no-token"))
                .title("Unauthorized access — no token for " + method + " " + path)
                .type(ScenarioType.AUTHORIZATION)
                .module(context.getModule())
                .endpoint(path)
                .httpMethod(method)
                .precondition("No Authorization header is sent")
                .step("Send " + method + " " + path + " without JWT")
                .expectedResult("HTTP 401 Unauthorized; no data returned")
                .priority(ScenarioPriority.P1)
                .risk(ScenarioRisk.CRITICAL)
                .build());

        scenarios.add(TestScenario.builder()
                .id(scenarioId(context, ScenarioType.AUTHORIZATION, "expired-token"))
                .title("Unauthorized access — expired token for " + method + " " + path)
                .type(ScenarioType.AUTHORIZATION)
                .module(context.getModule())
                .endpoint(path)
                .httpMethod(method)
                .precondition("Expired or malformed JWT token is available")
                .step("Send request with `Authorization: Bearer <expired-token>`")
                .expectedResult("HTTP 401 Unauthorized")
                .priority(ScenarioPriority.P2)
                .risk(ScenarioRisk.HIGH)
                .build());

        return scenarios;
    }
}
