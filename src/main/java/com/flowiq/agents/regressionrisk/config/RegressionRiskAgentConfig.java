package com.flowiq.agents.regressionrisk.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/regression-risk.properties",
        "classpath:config/api-change-detection.properties",
        "classpath:config/test-gap-analysis.properties",
        "system:properties",
        "system:env"
})
public interface RegressionRiskAgentConfig extends Config {

    @Key("agent.regressionrisk.report.output.path")
    @DefaultValue("docs/ai-reports/regression-risk-report.md")
    String reportOutputPath();

    @Key("agent.regressionrisk.git.base.ref")
    @DefaultValue("main")
    String gitBaseRef();

    @Key("agent.regressionrisk.use.git.diff")
    @DefaultValue("true")
    boolean useGitDiff();

    @Key("agent.regressionrisk.changed.files.manifest")
    String changedFilesManifest();

    @Key("agent.regressionrisk.openapi.snapshot")
    String openApiSnapshot();

    @Key("agent.regressionrisk.minutes.smoke")
    @DefaultValue("2")
    int minutesSmoke();

    @Key("agent.regressionrisk.minutes.contract")
    @DefaultValue("3")
    int minutesContract();

    @Key("agent.regressionrisk.minutes.regression")
    @DefaultValue("8")
    int minutesRegression();

    @Key("agent.regressionrisk.minutes.ui")
    @DefaultValue("5")
    int minutesUi();

    @Key("agent.regressionrisk.full.regression.critical.modules")
    @DefaultValue("1")
    int fullRegressionCriticalModules();

    @Key("agent.regressionrisk.partial.regression.high.modules")
    @DefaultValue("2")
    int partialRegressionHighModules();

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();
}
