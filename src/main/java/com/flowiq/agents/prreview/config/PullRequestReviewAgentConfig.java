package com.flowiq.agents.prreview.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:config/pr-review.properties",
        "classpath:config/api-change-detection.properties",
        "classpath:config/test-gap-analysis.properties",
        "system:properties",
        "system:env"
})
public interface PullRequestReviewAgentConfig extends Config {

    @Key("agent.prreview.report.output.path")
    @DefaultValue("docs/ai-reports/pr-review-report.md")
    String reportOutputPath();

    @Key("agent.prreview.git.base.ref")
    @DefaultValue("main")
    String gitBaseRef();

    @Key("agent.prreview.use.git.diff")
    @DefaultValue("true")
    boolean useGitDiff();

    @Key("agent.prreview.changed.files.manifest")
    String changedFilesManifest();

    @Key("agent.prreview.openapi.snapshot")
    String openApiSnapshot();

    @Key("agent.prreview.main.source.directory")
    @DefaultValue("src/main/java")
    String mainSourceDirectory();

    @Key("agent.prreview.test.source.directory")
    @DefaultValue("src/test/java")
    String testSourceDirectory();

    @Key("agent.prreview.schema.directory")
    @DefaultValue("src/test/resources/schemas")
    String schemaDirectory();

    @Key("agent.prreview.page.object.directory")
    @DefaultValue("src/main/java/com/flowiq/pages")
    String pageObjectDirectory();

    @Key("agent.prreview.reject.if.no.contract.on.new.endpoint")
    @DefaultValue("true")
    boolean rejectIfNoContractOnNewEndpoint();

    @Key("agent.prreview.reject.if.no.regression")
    @DefaultValue("true")
    boolean rejectIfNoRegression();

    @Key("agent.openapi.url")
    String openApiUrl();

    @Key("agent.openapi.docs.path")
    @DefaultValue("/v3/api-docs")
    String openApiDocsPath();

    @Key("agent.fetch.timeout.ms")
    @DefaultValue("30000")
    int fetchTimeoutMs();
}
