package com.flowiq.agents.review.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/test-review.properties",
        "classpath:config/api-change-detection.properties",
        "classpath:config/test-gap-analysis.properties",
        "system:properties",
        "system:env"
})
public interface TestReviewAgentConfig extends Config {

    @Key("agent.review.report.output.path")
    @DefaultValue("docs/ai-reports/test-review-report.md")
    String reportOutputPath();

    @Key("agent.review.git.base.ref")
    @DefaultValue("main")
    String gitBaseRef();

    @Key("agent.review.use.git.diff")
    @DefaultValue("true")
    boolean useGitDiff();

    @Key("agent.review.changed.files.manifest")
    String changedFilesManifest();

    @Key("agent.review.openapi.snapshot")
    String openApiSnapshot();

    @Key("agent.review.test.source.directory")
    @DefaultValue("src/test/java")
    String testSourceDirectory();

    @Key("agent.review.reject.if.no.regression")
    @DefaultValue("true")
    boolean rejectIfNoRegression();

    @Key("agent.review.reject.if.no.contract.on.new.endpoint")
    @DefaultValue("true")
    boolean rejectIfNoContractOnNewEndpoint();

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();
}
