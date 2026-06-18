package com.flowiq.agents.maintenance.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/test-maintenance.properties",
        "classpath:config/api-change-detection.properties",
        "classpath:config/test-gap-analysis.properties",
        "system:properties",
        "system:env"
})
public interface TestMaintenanceAgentConfig extends Config {

    @Key("agent.maintenance.report.output.path")
    @DefaultValue("docs/ai-reports/test-maintenance-report.md")
    String reportOutputPath();

    @Key("agent.maintenance.test.source.directory")
    @DefaultValue("src/test/java")
    String testSourceDirectory();

    @Key("agent.maintenance.main.source.directory")
    @DefaultValue("src/main/java")
    String mainSourceDirectory();

    @Key("agent.maintenance.page.object.directory")
    @DefaultValue("src/main/java/com/flowiq/pages")
    String pageObjectDirectory();

    @Key("agent.maintenance.dto.source.directory")
    @DefaultValue("src/main/java/com/flowiq/models")
    String dtoSourceDirectory();

    @Key("agent.maintenance.schema.directory")
    @DefaultValue("src/test/resources/schemas")
    String schemaDirectory();

    @Key("agent.maintenance.openapi.snapshot")
    String openApiSnapshot();

    @Key("agent.maintenance.allure.results.directories")
    @DefaultValue("target/allure-results,docs/test-history/allure")
    String allureResultsDirectories();

    @Key("agent.maintenance.oversized.test.class.lines")
    @DefaultValue("300")
    int oversizedTestClassLines();

    @Key("agent.maintenance.oversized.page.object.lines")
    @DefaultValue("200")
    int oversizedPageObjectLines();

    @Key("agent.maintenance.long.method.lines")
    @DefaultValue("40")
    int longMethodLines();

    @Key("agent.maintenance.low.cohesion.method.count")
    @DefaultValue("15")
    int lowCohesionMethodCount();

    @Key("agent.maintenance.flaky.failure.threshold")
    @DefaultValue("0.3")
    double flakyFailureThreshold();

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();
}
