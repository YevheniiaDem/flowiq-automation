package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioPriority;
import com.flowiq.agents.generator.model.ScenarioRisk;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;
import com.flowiq.agents.generator.schema.JsonSchemaDocument;
import com.flowiq.agents.generator.schema.SchemaFieldConstraint;

import java.util.ArrayList;
import java.util.List;

public class BoundaryScenarioGenerator extends AbstractScenarioGenerator {

    @Override
    public ScenarioGeneratorType type() {
        return ScenarioGeneratorType.BOUNDARY;
    }

    @Override
    public List<TestScenario> generate(EndpointTestContext context) {
        if (!isUncovered(context, ScenarioType.BOUNDARY)) {
            return List.of();
        }
        JsonSchemaDocument schema = context.getResponseSchema();
        if (schema == null || schema.getFields().isEmpty()) {
            return List.of();
        }

        String method = context.getOperation().method();
        String path = context.getNormalizedPath();
        List<TestScenario> scenarios = new ArrayList<>();

        for (SchemaFieldConstraint field : schema.getFields()) {
            if (field.getMinLength() != null) {
                scenarios.add(boundaryScenario(context, method, path, field,
                        "min-length", "length equals minLength (" + field.getMinLength() + ")",
                        ScenarioPriority.P3, ScenarioRisk.LOW));
            }
            if (field.getMaxLength() != null) {
                scenarios.add(boundaryScenario(context, method, path, field,
                        "max-length", "length equals maxLength (" + field.getMaxLength() + ")",
                        ScenarioPriority.P3, ScenarioRisk.LOW));
            }
            if (field.getMinimum() != null) {
                scenarios.add(boundaryScenario(context, method, path, field,
                        "min-value", "value equals minimum (" + field.getMinimum() + ")",
                        ScenarioPriority.P3, ScenarioRisk.LOW));
            }
            if (!field.getEnumValues().isEmpty()) {
                scenarios.add(TestScenario.builder()
                        .id(scenarioId(context, ScenarioType.BOUNDARY, "enum-" + field.getField()))
                        .title("Boundary — each allowed enum value for `" + field.getField() + "` on " + path)
                        .type(ScenarioType.BOUNDARY)
                        .module(context.getModule())
                        .endpoint(path)
                        .httpMethod(method)
                        .precondition("Valid auth and test data setup")
                        .step("For each enum value " + field.getEnumValues() + ", execute " + method + " " + path)
                        .expectedResult("All enum values accepted; response remains contract-compliant")
                        .priority(ScenarioPriority.P3)
                        .risk(ScenarioRisk.LOW)
                        .build());
            }
        }

        if (scenarios.isEmpty()) {
            scenarios.add(TestScenario.builder()
                    .id(scenarioId(context, ScenarioType.BOUNDARY, "pagination"))
                    .title("Boundary — pagination limits for " + method + " " + path)
                    .type(ScenarioType.BOUNDARY)
                    .module(context.getModule())
                    .endpoint(path)
                    .httpMethod(method)
                    .precondition("Dataset with more than one page of results")
                    .step("Request with page=0, size=1 (minimum page size)")
                    .step("Request with maximum allowed page size")
                    .expectedResult("Valid paginated response; empty page handled gracefully at end of dataset")
                    .priority(ScenarioPriority.P3)
                    .risk(ScenarioRisk.LOW)
                    .build());
        }
        return scenarios;
    }

    private TestScenario boundaryScenario(EndpointTestContext context, String method, String path,
                                          SchemaFieldConstraint field, String suffix, String detail,
                                          ScenarioPriority priority, ScenarioRisk risk) {
        return TestScenario.builder()
                .id(scenarioId(context, ScenarioType.BOUNDARY, suffix + "-" + field.getField()))
                .title("Boundary — " + field.getField() + " " + detail + " on " + path)
                .type(ScenarioType.BOUNDARY)
                .module(context.getModule())
                .endpoint(path)
                .httpMethod(method)
                .precondition("Valid auth if required; field `" + field.getField() + "` is settable in request/response")
                .step("Craft payload where `" + field.getField() + "` " + detail)
                .step("Send " + method + " " + path)
                .expectedResult("HTTP 2xx if valid boundary; HTTP 400 if boundary is rejected per contract")
                .priority(priority)
                .risk(risk)
                .build();
    }
}
