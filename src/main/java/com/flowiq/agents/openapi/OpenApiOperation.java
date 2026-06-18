package com.flowiq.agents.openapi;

import com.fasterxml.jackson.databind.JsonNode;

public record OpenApiOperation(String path, String method, JsonNode operation) {
}
