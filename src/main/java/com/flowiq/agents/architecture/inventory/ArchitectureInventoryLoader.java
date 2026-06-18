package com.flowiq.agents.architecture.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.architecture.config.ArchitectureDriftAgentConfig;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.openapi.OpenApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class ArchitectureInventoryLoader {

    private final ArchitectureDriftAgentConfig config;
    private final ObjectMapper objectMapper;
    private final DocumentationInventoryLoader documentationLoader;
    private final SourceCodeInventoryLoader sourceCodeLoader;
    private final TestInventoryLoader testInventoryLoader;

    public ArchitectureInventoryLoader(ArchitectureDriftAgentConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.documentationLoader = new DocumentationInventoryLoader(config);
        this.sourceCodeLoader = new SourceCodeInventoryLoader(config);
        this.testInventoryLoader = new TestInventoryLoader(config);
    }

    public ArchitectureContext load() {
        List<ApiEndpointRef> openApiEndpoints = loadOpenApiEndpoints();
        DocumentationInventory documentation = documentationLoader.load();
        SourceCodeInventory source = sourceCodeLoader.load();
        TestInventory tests = testInventoryLoader.load();

        return ArchitectureContext.builder()
                .openApiEndpoints(openApiEndpoints)
                .documentedEndpoints(documentation.getEndpoints())
                .documentedModules(documentation.getModules())
                .services(source.getServices())
                .controllers(source.getControllers())
                .pages(source.getPages())
                .dtos(source.getDtos())
                .schemaFiles(source.getSchemaFiles())
                .contractTestClasses(tests.getContractTestClasses())
                .regressionTestClasses(tests.getRegressionTestClasses())
                .smokeTestClasses(tests.getSmokeTestClasses())
                .uiTestClasses(tests.getUiTestClasses())
                .build();
    }

    public String summarizeSources() {
        return String.join("; ",
                "Docs: " + resolvePath(config.docsDirectory()),
                "ADR: " + resolvePath(config.adrDirectory()),
                "OpenAPI: " + (config.openApiSnapshot() == null || config.openApiSnapshot().isBlank()
                        ? "live" : config.openApiSnapshot()),
                "Backend: " + resolvePath(config.backendSourceDirectory()),
                "Frontend: " + resolvePath(config.frontendSourceDirectory()),
                "Schemas: " + resolvePath(config.schemaDirectory()));
    }

    private List<ApiEndpointRef> loadOpenApiEndpoints() {
        try {
            JsonNode spec = loadOpenApiSpec();
            List<ApiEndpointRef> endpoints = new ArrayList<>();
            for (OpenApiOperation operation : OpenApiNavigator.getOperations(spec)) {
                endpoints.add(new ApiEndpointRef(
                        operation.method(),
                        operation.path(),
                        "OpenAPI"));
            }
            return endpoints;
        } catch (Exception e) {
            log.warn("OpenAPI inventory skipped: {}", e.getMessage());
            return List.of();
        }
    }

    private JsonNode loadOpenApiSpec() throws IOException {
        String snapshot = config.openApiSnapshot();
        if (snapshot != null && !snapshot.isBlank()) {
            Path path = resolvePath(snapshot);
            return objectMapper.readTree(Files.readString(path));
        }
        AgentConfig agentConfig = ConfigFactory.create(AgentConfig.class);
        return new OpenApiFetcher(agentConfig, objectMapper).fetchCurrentSpec();
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
