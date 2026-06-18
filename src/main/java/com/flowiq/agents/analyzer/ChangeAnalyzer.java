package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;

import java.util.List;

/**
 * Strategy interface for OpenAPI change analyzers.
 */
public interface ChangeAnalyzer {

    String name();

    List<ApiChange> analyze(JsonNode previousSpec, JsonNode currentSpec);
}
