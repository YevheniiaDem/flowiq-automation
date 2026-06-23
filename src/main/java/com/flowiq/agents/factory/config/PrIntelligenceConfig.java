package com.flowiq.agents.factory.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/pr-intelligence.properties",
        "system:properties",
        "system:env"
})
public interface PrIntelligenceConfig extends Config {

    @Key("agent.pr.intelligence.report.output.path")
    @DefaultValue("docs/ai-reports/pr-intelligence-report.md")
    String reportOutputPath();

    @Key("agent.pr.intelligence.continue.on.failure")
    @DefaultValue("true")
    boolean continueOnFailure();
}
