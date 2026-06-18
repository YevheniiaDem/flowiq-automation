package com.flowiq.agents.orchestrator.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/quality-intelligence.properties",
        "system:properties",
        "system:env"
})
public interface QualityIntelligenceConfig extends Config {

    @Key("agent.quality.report.output.path")
    @DefaultValue("docs/ai-reports/quality-intelligence-report.md")
    String reportOutputPath();

    @Key("agent.quality.enabled.agents")
    @DefaultValue("all")
    String enabledAgents();

    @Key("agent.quality.continue.on.failure")
    @DefaultValue("true")
    boolean continueOnFailure();
}
