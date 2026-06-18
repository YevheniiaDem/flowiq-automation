package com.flowiq.agents.generator.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/smart-test-generator.properties",
        "classpath:config/api-change-detection.properties",
        "classpath:config/test-gap-analysis.properties",
        "system:properties",
        "system:env"
})
public interface SmartTestGeneratorConfig extends Config {

    @Key("agent.generator.report.output.path")
    @DefaultValue("docs/ai-reports/generated-test-scenarios.md")
    String reportOutputPath();

    @Key("agent.generator.schema.directory")
    @DefaultValue("src/test/resources/schemas")
    String schemaDirectory();

    @Key("agent.generator.test.source.directory")
    @DefaultValue("src/test/java")
    String testSourceDirectory();

    @Key("agent.generator.openapi.snapshot")
    String openApiSnapshot();

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();

    @Key("agent.generator.max.scenarios.per.endpoint")
    @DefaultValue("8")
    int maxScenariosPerEndpoint();
}
