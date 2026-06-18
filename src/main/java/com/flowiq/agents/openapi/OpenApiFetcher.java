package com.flowiq.agents.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.config.ConfigManager;
import com.flowiq.config.EnvironmentConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class OpenApiFetcher {

    private final AgentConfig agentConfig;
    private final EnvironmentConfig environmentConfig;
    private final ObjectMapper objectMapper;

    public OpenApiFetcher(AgentConfig agentConfig, ObjectMapper objectMapper) {
        this.agentConfig = agentConfig;
        this.environmentConfig = ConfigManager.getConfig();
        this.objectMapper = objectMapper;
    }

    public JsonNode fetchCurrentSpec() {
        String url = resolveOpenApiUrl();
        log.info("Fetching OpenAPI specification from {}", url);

        Response response = RestAssured.given()
                .relaxedHTTPSValidation()
                .header("Accept", "application/json")
                .config(RestAssured.config()
                        .httpClient(io.restassured.config.HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", agentConfig.fetchTimeoutMs())
                                .setParam("http.socket.timeout", agentConfig.fetchTimeoutMs())))
                .when()
                .get(url)
                .then()
                .extract()
                .response();

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to fetch OpenAPI spec from " + url + ": HTTP " + response.statusCode());
        }

        try {
            String body = new String(response.asByteArray(), StandardCharsets.UTF_8);
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAPI spec from " + url, e);
        }
    }

    public String resolveOpenApiUrl() {
        String configuredUrl = agentConfig.openApiUrl();
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return appendDocsPathIfNeeded(configuredUrl.trim());
        }

        String apiUrl = environmentConfig.apiUrl();
        String baseUrl = apiUrl.endsWith("/api")
                ? apiUrl.substring(0, apiUrl.length() - 4)
                : apiUrl.replaceAll("/+$", "");

        return baseUrl + normalizeDocsPath(agentConfig.openApiDocsPath());
    }

    private String appendDocsPathIfNeeded(String url) {
        String docsPath = normalizeDocsPath(agentConfig.openApiDocsPath());
        if (url.endsWith(docsPath) || url.contains(docsPath + "?")) {
            return url;
        }
        return url.replaceAll("/+$", "") + docsPath;
    }

    private static String normalizeDocsPath(String docsPath) {
        if (docsPath == null || docsPath.isBlank()) {
            return "/v3/api-docs";
        }
        return docsPath.startsWith("/") ? docsPath : "/" + docsPath;
    }
}
