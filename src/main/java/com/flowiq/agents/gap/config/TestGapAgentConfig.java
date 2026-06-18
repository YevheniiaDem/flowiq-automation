package com.flowiq.agents.gap.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/test-gap-analysis.properties",
        "classpath:config/api-change-detection.properties",
        "system:properties",
        "system:env"
})
public interface TestGapAgentConfig extends Config {

    @Key("agent.gap.test.source.directory")
    @DefaultValue("src/test/java")
    String testSourceDirectory();

    @Key("agent.gap.report.output.path")
    @DefaultValue("docs/ai-reports/test-gap-analysis.md")
    String reportOutputPath();

    @Key("agent.gap.business.impact.file")
    @DefaultValue("classpath:config/business-impact.properties")
    String businessImpactFile();

    @Key("agent.gap.openapi.snapshot")
    String openApiSnapshot();

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();

    @Key("agent.llm.provider")
    @DefaultValue("none")
    String llmProvider();
}
