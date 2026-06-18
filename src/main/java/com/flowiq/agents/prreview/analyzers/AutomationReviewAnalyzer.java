package com.flowiq.agents.prreview.analyzers;

import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.EndpointMatcher;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.prreview.config.PullRequestReviewAgentConfig;
import com.flowiq.agents.prreview.model.PrChangedArtifact;
import com.flowiq.agents.prreview.model.PrChangedArtifactType;
import com.flowiq.agents.prreview.model.PrReviewArea;
import com.flowiq.agents.prreview.model.PrReviewCategory;
import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewSeverity;
import com.flowiq.agents.prreview.scanner.PrReviewContext;
import com.flowiq.agents.prreview.scanner.SourceInventory;
import org.aeonbits.owner.ConfigFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AutomationReviewAnalyzer implements PrReviewAnalyzer {

    private final PullRequestReviewAgentConfig config;
    private final BusinessImpactPrioritizer prioritizer;

    public AutomationReviewAnalyzer() {
        PullRequestReviewAgentConfig cfg = ConfigFactory.create(PullRequestReviewAgentConfig.class);
        this.config = cfg;
        this.prioritizer = new BusinessImpactPrioritizer(
                ConfigFactory.create(com.flowiq.agents.gap.config.TestGapAgentConfig.class));
    }

    public AutomationReviewAnalyzer(PullRequestReviewAgentConfig config, BusinessImpactPrioritizer prioritizer) {
        this.config = config;
        this.prioritizer = prioritizer;
    }

    @Override
    public String name() {
        return "AutomationReviewAnalyzer";
    }

    @Override
    public List<PrReviewFinding> analyze(PrReviewContext context) {
        List<PrReviewFinding> findings = new ArrayList<>();
        List<ScannedTestReference> tests = context.getTestReferences();
        SourceInventory inventory = context.getSourceInventory();

        for (PrChangedArtifact artifact : context.getChangedArtifacts()) {
            if (artifact.getType() == PrChangedArtifactType.ENDPOINT) {
                findings.addAll(reviewEndpointAutomation(artifact, tests));
            }
            if (artifact.getType() == PrChangedArtifactType.PAGE) {
                findings.addAll(reviewPageAutomation(artifact, tests, inventory));
            }
        }
        return findings;
    }

    private List<PrReviewFinding> reviewEndpointAutomation(PrChangedArtifact endpoint,
                                                           List<ScannedTestReference> tests) {
        List<PrReviewFinding> findings = new ArrayList<>();
        List<ScannedTestReference> endpointTests = tests.stream()
                .filter(t -> EndpointMatcher.matches(t.getPath(), endpoint.getEndpointPath()))
                .filter(t -> EndpointMatcher.methodMatches(t.getMethod(), endpoint.getHttpMethod()))
                .toList();
        List<ScannedTestReference> moduleTests = tests.stream()
                .filter(t -> endpoint.getModule().equals(t.getModule()))
                .toList();

        boolean smoke = hasSuite(endpointTests, TestSuiteType.SMOKE)
                || hasSuite(moduleTests, TestSuiteType.SMOKE);
        boolean regression = hasSuite(endpointTests, TestSuiteType.REGRESSION)
                || hasSuite(moduleTests, TestSuiteType.REGRESSION);

        String location = endpoint.getHttpMethod() + " " + endpoint.getEndpointPath();

        if (!smoke) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.AUTOMATION_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(PrReviewSeverity.HIGH)
                    .title("Endpoint without smoke test coverage")
                    .location(location)
                    .recommendation("Add smoke API test for " + location + " in the module "
                            + endpoint.getModule() + ".")
                    .blocking(false)
                    .build());
        }
        if (!regression) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.AUTOMATION_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(config.rejectIfNoRegression() && endpoint.isNewlyAdded()
                            ? PrReviewSeverity.CRITICAL : PrReviewSeverity.HIGH)
                    .title("Endpoint without regression test coverage")
                    .location(location)
                    .recommendation("Add regression suite coverage for " + location + ".")
                    .blocking(config.rejectIfNoRegression() && endpoint.isNewlyAdded())
                    .build());
        }
        return findings;
    }

    private List<PrReviewFinding> reviewPageAutomation(PrChangedArtifact page,
                                                       List<ScannedTestReference> tests,
                                                       SourceInventory inventory) {
        List<PrReviewFinding> findings = new ArrayList<>();
        String pageClass = page.getName().replace(" Page", "Page");
        boolean hasPageObject = inventory.getPageClassNames().contains(pageClass)
                || inventory.getPageObjectFiles().stream()
                .anyMatch(file -> file.toLowerCase(Locale.ROOT).contains(pageClass.toLowerCase(Locale.ROOT)));

        List<ScannedTestReference> moduleTests = tests.stream()
                .filter(t -> page.getModule().equals(t.getModule()))
                .toList();
        boolean uiSmoke = hasSuite(moduleTests, TestSuiteType.UI);

        String location = page.getFilePath() != null ? page.getFilePath() : pageClass;

        if (!uiSmoke && prioritizer.uiExpected(page.getModule())) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.AUTOMATION_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(PrReviewSeverity.HIGH)
                    .title("Page without UI smoke test coverage")
                    .location(location)
                    .recommendation("Add UI smoke test for " + pageClass + " under ui/" + page.getModule() + ".")
                    .blocking(false)
                    .build());
        }
        if (!hasPageObject) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.AUTOMATION_REVIEW)
                    .area(PrReviewArea.ARCHITECTURE)
                    .severity(PrReviewSeverity.MEDIUM)
                    .title("Page change without dedicated page object")
                    .location(location)
                    .recommendation("Create or update page object " + pageClass
                            + " in com.flowiq.pages with stable locators.")
                    .blocking(false)
                    .build());
        }
        return findings;
    }

    private static boolean hasSuite(List<ScannedTestReference> tests, TestSuiteType suite) {
        return tests.stream().anyMatch(t -> t.getSuites().contains(suite));
    }
}
