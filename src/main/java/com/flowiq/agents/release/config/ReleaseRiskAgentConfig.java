package com.flowiq.agents.release.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/release-risk-assessment.properties",
        "system:properties",
        "system:env"
})
public interface ReleaseRiskAgentConfig extends Config {

    @Key("agent.release.report.output.path")
    @DefaultValue("docs/ai-reports/release-readiness-report.md")
    String reportOutputPath();

    @Key("agent.release.surefire.directory")
    @DefaultValue("target/surefire-reports")
    String surefireDirectory();

    @Key("agent.release.surefire.history.directory")
    @DefaultValue("docs/test-history/surefire")
    String surefireHistoryDirectory();

    @Key("agent.release.allure.results.directories")
    @DefaultValue("target/allure-results")
    String allureResultsDirectories();

    @Key("agent.release.flaky.report.path")
    @DefaultValue("docs/ai-reports/flaky-tests-report.md")
    String flakyReportPath();

    @Key("agent.release.api.change.report.path")
    @DefaultValue("docs/ai-reports/api-change-report.md")
    String apiChangeReportPath();

    @Key("agent.release.score.green.max")
    @DefaultValue("25")
    int greenScoreMax();

    @Key("agent.release.score.yellow.max")
    @DefaultValue("60")
    int yellowScoreMax();

    @Key("agent.release.weight.regression")
    @DefaultValue("35")
    int weightRegression();

    @Key("agent.release.weight.smoke")
    @DefaultValue("30")
    int weightSmoke();

    @Key("agent.release.weight.contract")
    @DefaultValue("20")
    int weightContract();

    @Key("agent.release.weight.flaky")
    @DefaultValue("10")
    int weightFlaky();

    @Key("agent.release.weight.api.change")
    @DefaultValue("5")
    int weightApiChange();

    @Key("agent.release.gate.regression.min.pass.rate")
    @DefaultValue("90")
    double regressionMinPassRate();

    @Key("agent.release.gate.smoke.min.pass.rate")
    @DefaultValue("95")
    double smokeMinPassRate();

    @Key("agent.release.gate.contract.min.pass.rate")
    @DefaultValue("100")
    double contractMinPassRate();
}
