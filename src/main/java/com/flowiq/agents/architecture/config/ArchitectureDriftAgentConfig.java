package com.flowiq.agents.architecture.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/architecture-drift.properties",
        "classpath:config/api-change-detection.properties",
        "classpath:config/test-gap-analysis.properties",
        "system:properties",
        "system:env"
})
public interface ArchitectureDriftAgentConfig extends Config {

    @Key("agent.architecture.report.output.path")
    @DefaultValue("docs/ai-reports/architecture-drift-report.md")
    String reportOutputPath();

    @Key("agent.architecture.docs.directory")
    @DefaultValue("docs")
    String docsDirectory();

    @Key("agent.architecture.adr.directory")
    @DefaultValue("docs/adr")
    String adrDirectory();

    @Key("agent.architecture.openapi.snapshot")
    String openApiSnapshot();

    @Key("agent.architecture.backend.source.directory")
    @DefaultValue("src/main/java/com/flowiq/services")
    String backendSourceDirectory();

    @Key("agent.architecture.frontend.source.directory")
    @DefaultValue("src/main/java/com/flowiq/pages")
    String frontendSourceDirectory();

    @Key("agent.architecture.dto.source.directory")
    @DefaultValue("src/main/java/com/flowiq/models")
    String dtoSourceDirectory();

    @Key("agent.architecture.schema.directory")
    @DefaultValue("src/test/resources/schemas")
    String schemaDirectory();

    @Key("agent.architecture.test.source.directory")
    @DefaultValue("src/test/java")
    String testSourceDirectory();

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();
}
