package com.flowiq.agents.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/api-change-detection.properties",
        "system:properties",
        "system:env"
})
public interface AgentConfig extends Config {

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.snapshot.directory")
    @DefaultValue("docs/api-snapshots")
    String snapshotDirectory();

    @Key("agent.snapshot.filename")
    @DefaultValue("openapi-snapshot.json")
    String snapshotFilename();

    @Key("agent.report.output.path")
    @DefaultValue("docs/ai-reports/api-change-report.md")
    String reportOutputPath();

    @Key("agent.test.mapping.file")
    @DefaultValue("classpath:config/test-impact-mapping.properties")
    String testMappingFile();

    @Key("agent.llm.provider")
    @DefaultValue("none")
    String llmProvider();

    @Key("agent.llm.openai.api.key")
    String openAiApiKey();

    @Key("agent.llm.openai.model")
    @DefaultValue("gpt-4o")
    String openAiModel();

    @Key("agent.llm.claude.api.key")
    String claudeApiKey();

    @Key("agent.llm.claude.model")
    @DefaultValue("claude-sonnet-4-20250514")
    String claudeModel();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();

    @Key("agent.save.snapshot.on.run")
    @DefaultValue("false")
    boolean saveSnapshotOnRun();

    @Key("agent.fail.on.breaking.changes")
    @DefaultValue("false")
    boolean failOnBreakingChanges();
}
