package com.flowiq.agents.rootcause.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/root-cause-analysis.properties",
        "system:properties",
        "system:env"
})
public interface RootCauseAgentConfig extends Config {

    @Key("agent.rootcause.report.output.path")
    @DefaultValue("docs/ai-reports/root-cause-analysis.md")
    String reportOutputPath();

    @Key("agent.rootcause.allure.results.directories")
    @DefaultValue("target/allure-results")
    String allureResultsDirectories();

    @Key("agent.rootcause.surefire.directory")
    @DefaultValue("target/surefire-reports")
    String surefireDirectory();

    @Key("agent.rootcause.screenshot.directory")
    @DefaultValue("target/screenshots")
    String screenshotDirectory();

    @Key("agent.rootcause.trace.directory")
    @DefaultValue("target/traces")
    String traceDirectory();

    @Key("agent.rootcause.video.directory")
    @DefaultValue("target/videos")
    String videoDirectory();

    @Key("agent.rootcause.backend.log.directory")
    @DefaultValue("target/logs")
    String backendLogDirectory();

    @Key("agent.rootcause.top.n")
    @DefaultValue("25")
    int topN();
}
