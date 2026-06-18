package com.flowiq.agents.prreview.analyzers;

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
import com.flowiq.agents.prreview.scanner.SourceInventoryScanner;
import org.aeonbits.owner.ConfigFactory;

import java.util.ArrayList;
import java.util.List;

public class ApiReviewAnalyzer implements PrReviewAnalyzer {

    private final PullRequestReviewAgentConfig config;

    public ApiReviewAnalyzer() {
        this(ConfigFactory.create(PullRequestReviewAgentConfig.class));
    }

    public ApiReviewAnalyzer(PullRequestReviewAgentConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "ApiReviewAnalyzer";
    }

    @Override
    public List<PrReviewFinding> analyze(PrReviewContext context) {
        List<PrReviewFinding> findings = new ArrayList<>();
        List<ScannedTestReference> tests = context.getTestReferences();
        SourceInventory inventory = context.getSourceInventory();

        for (PrChangedArtifact artifact : context.getChangedArtifacts()) {
            if (artifact.getType() == PrChangedArtifactType.ENDPOINT) {
                findings.addAll(reviewEndpoint(artifact, tests));
            }
            if (artifact.getType() == PrChangedArtifactType.DTO && artifact.getSchemaName() != null) {
                findings.addAll(reviewDtoSchema(artifact, inventory, context.getChangedFiles()));
            }
        }
        return findings;
    }

    private List<PrReviewFinding> reviewEndpoint(PrChangedArtifact endpoint,
                                                 List<ScannedTestReference> tests) {
        List<PrReviewFinding> findings = new ArrayList<>();
        List<ScannedTestReference> endpointTests = tests.stream()
                .filter(t -> EndpointMatcher.matches(t.getPath(), endpoint.getEndpointPath()))
                .filter(t -> EndpointMatcher.methodMatches(t.getMethod(), endpoint.getHttpMethod()))
                .toList();

        boolean contract = hasSuite(endpointTests, TestSuiteType.CONTRACT)
                || hasSuite(filterModule(tests, endpoint.getModule()), TestSuiteType.CONTRACT);
        boolean auth = endpointTests.stream().anyMatch(ScannedTestReference::isAuthorizationCheck)
                || filterModule(tests, endpoint.getModule()).stream()
                .anyMatch(ScannedTestReference::isAuthorizationCheck);
        boolean negative = endpointTests.stream().anyMatch(ScannedTestReference::isNegativeScenario)
                || filterModule(tests, endpoint.getModule()).stream()
                .anyMatch(ScannedTestReference::isNegativeScenario);

        String location = endpoint.getHttpMethod() + " " + endpoint.getEndpointPath();

        if (!contract) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.API_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(endpoint.isNewlyAdded() ? PrReviewSeverity.CRITICAL : PrReviewSeverity.HIGH)
                    .title("Endpoint without contract test coverage")
                    .location(location)
                    .recommendation("Add contract tests asserting request/response schema for "
                            + location + " before merge.")
                    .blocking(config.rejectIfNoContractOnNewEndpoint() && endpoint.isNewlyAdded())
                    .build());
        }
        if (!auth) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.API_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(PrReviewSeverity.HIGH)
                    .title("Endpoint without authorization test coverage")
                    .location(location)
                    .recommendation("Add unauthorized/forbidden scenarios for " + location + ".")
                    .blocking(false)
                    .build());
        }
        if (!negative) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.API_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(PrReviewSeverity.MEDIUM)
                    .title("Endpoint without negative test coverage")
                    .location(location)
                    .recommendation("Add validation/error-path tests (400/404/422) for " + location + ".")
                    .blocking(false)
                    .build());
        }
        return findings;
    }

    private List<PrReviewFinding> reviewDtoSchema(PrChangedArtifact dto,
                                                  SourceInventory inventory,
                                                  List<String> changedFiles) {
        List<PrReviewFinding> findings = new ArrayList<>();
        String schemaName = dto.getSchemaName();
        boolean schemaExists = SourceInventoryScanner.schemaExistsForDto(schemaName, inventory);
        boolean schemaChanged = SourceInventoryScanner.schemaChangedForDto(schemaName, changedFiles);

        if (!schemaExists || (!schemaChanged && dto.getFilePath() != null)) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.API_REVIEW)
                    .area(PrReviewArea.ARCHITECTURE)
                    .severity(PrReviewSeverity.HIGH)
                    .title("DTO changed without schema update")
                    .location(dto.getFilePath() != null ? dto.getFilePath() : schemaName)
                    .recommendation("Update JSON schema under src/test/resources/schemas for " + schemaName
                            + " to match DTO changes.")
                    .blocking(false)
                    .build());
        }
        return findings;
    }

    private static List<ScannedTestReference> filterModule(List<ScannedTestReference> tests, String module) {
        return tests.stream().filter(t -> module.equals(t.getModule())).toList();
    }

    private static boolean hasSuite(List<ScannedTestReference> tests, TestSuiteType suite) {
        return tests.stream().anyMatch(t -> t.getSuites().contains(suite));
    }
}
