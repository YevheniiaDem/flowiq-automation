package com.flowiq.agents.factory.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/failure-intelligence.properties",
        "system:properties",
        "system:env"
})
public interface FailureIntelligenceConfig extends Config {

    @Key("agent.failure.intelligence.report.output.path")
    @DefaultValue("docs/ai-reports/failure-intelligence-report.md")
    String reportOutputPath();

    @Key("agent.failure.intelligence.continue.on.failure")
    @DefaultValue("true")
    boolean continueOnFailure();
}
