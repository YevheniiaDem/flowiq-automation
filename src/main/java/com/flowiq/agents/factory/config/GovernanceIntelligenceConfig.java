package com.flowiq.agents.factory.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/governance-intelligence.properties",
        "system:properties",
        "system:env"
})
public interface GovernanceIntelligenceConfig extends Config {

    @Key("agent.governance.intelligence.report.output.path")
    @DefaultValue("docs/ai-reports/governance-intelligence-report.md")
    String reportOutputPath();

    @Key("agent.governance.intelligence.continue.on.failure")
    @DefaultValue("true")
    boolean continueOnFailure();
}
