package com.flowiq.agents.flaky.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/flaky-test-investigation.properties",
        "system:properties",
        "system:env"
})
public interface FlakyTestAgentConfig extends Config {

    @Key("agent.flaky.allure.results.directories")
    @DefaultValue("target/allure-results")
    String allureResultsDirectories();

    @Key("agent.flaky.allure.history.directory")
    @DefaultValue("docs/test-history/allure")
    String allureHistoryDirectory();

    @Key("agent.flaky.surefire.directory")
    @DefaultValue("target/surefire-reports")
    String surefireDirectory();

    @Key("agent.flaky.surefire.history.directory")
    @DefaultValue("docs/test-history/surefire")
    String surefireHistoryDirectory();

    @Key("agent.flaky.ci.history.directory")
    @DefaultValue("docs/ci-history")
    String ciHistoryDirectory();

    @Key("agent.flaky.log.directory")
    @DefaultValue("target/logs")
    String logDirectory();

    @Key("agent.flaky.report.output.path")
    @DefaultValue("docs/ai-reports/flaky-tests-report.md")
    String reportOutputPath();

    @Key("agent.flaky.top.n")
    @DefaultValue("20")
    int topN();

    @Key("agent.flaky.min.runs")
    @DefaultValue("2")
    int minRuns();

    @Key("agent.flaky.github.repository")
    String githubRepository();

    @Key("agent.flaky.github.workflows")
    @DefaultValue("ui-smoke.yml,api-smoke.yml,nightly-regression.yml,pr-validation.yml")
    String githubWorkflows();

    @Key("agent.flaky.use.gh.cli")
    @DefaultValue("true")
    boolean useGhCli();

    @Key("agent.flaky.llm.provider")
    @DefaultValue("none")
    String llmProvider();
}
