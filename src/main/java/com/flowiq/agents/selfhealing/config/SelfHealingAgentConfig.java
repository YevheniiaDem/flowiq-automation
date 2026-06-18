package com.flowiq.agents.selfhealing.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/self-healing.properties",
        "classpath:config/api-change-detection.properties",
        "system:properties",
        "system:env"
})
public interface SelfHealingAgentConfig extends Config {

    @Key("agent.selfhealing.report.output.path")
    @DefaultValue("docs/ai-reports/self-healing-report.md")
    String reportOutputPath();

    @Key("agent.selfhealing.allure.results.directories")
    @DefaultValue("target/allure-results")
    String allureResultsDirectories();

    @Key("agent.selfhealing.dom.snapshot.directory")
    @DefaultValue("docs/test-history/dom")
    String domSnapshotDirectory();

    @Key("agent.selfhealing.screenshot.directory")
    @DefaultValue("target/screenshots")
    String screenshotDirectory();

    @Key("agent.selfhealing.confidence.high.threshold")
    @DefaultValue("0.85")
    double confidenceHighThreshold();

    @Key("agent.selfhealing.confidence.medium.threshold")
    @DefaultValue("0.60")
    double confidenceMediumThreshold();

    @Key("agent.selfhealing.llm.provider")
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
}
