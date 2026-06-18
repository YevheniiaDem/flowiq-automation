package com.flowiq.agents.prreview.analyzers;

import com.flowiq.agents.prreview.model.PrChangedArtifact;
import com.flowiq.agents.prreview.model.PrChangedArtifactType;
import com.flowiq.agents.prreview.model.PrReviewArea;
import com.flowiq.agents.prreview.model.PrReviewCategory;
import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewSeverity;
import com.flowiq.agents.prreview.scanner.PrReviewContext;
import com.flowiq.agents.prreview.scanner.SourceInventory;
import com.flowiq.agents.prreview.scanner.SourceInventoryScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BackendReviewAnalyzer implements PrReviewAnalyzer {

    private final SourceInventoryScanner inventoryScanner;

    public BackendReviewAnalyzer() {
        this(new SourceInventoryScanner(
                org.aeonbits.owner.ConfigFactory.create(
                        com.flowiq.agents.prreview.config.PullRequestReviewAgentConfig.class)));
    }

    public BackendReviewAnalyzer(SourceInventoryScanner inventoryScanner) {
        this.inventoryScanner = inventoryScanner;
    }

    @Override
    public String name() {
        return "BackendReviewAnalyzer";
    }

    @Override
    public List<PrReviewFinding> analyze(PrReviewContext context) {
        List<PrReviewFinding> findings = new ArrayList<>();
        SourceInventory inventory = context.getSourceInventory();

        for (PrChangedArtifact artifact : context.getChangedArtifacts()) {
            if (artifact.getType() == PrChangedArtifactType.CONTROLLER) {
                findings.addAll(reviewController(artifact, inventory));
            }
            if (artifact.getType() == PrChangedArtifactType.SERVICE) {
                findings.addAll(reviewService(artifact, inventory));
            }
            if (artifact.getType() == PrChangedArtifactType.REPOSITORY) {
                findings.addAll(reviewRepository(artifact, inventory));
            }
        }
        return findings;
    }

    private List<PrReviewFinding> reviewController(PrChangedArtifact controller, SourceInventory inventory) {
        List<PrReviewFinding> findings = new ArrayList<>();
        String baseName = controller.getName().replace("Controller", "");
        String expectedService = baseName + "Service";
        boolean hasService = inventory.getServiceClassNames().contains(expectedService)
                || inventory.getServiceFiles().stream()
                .anyMatch(file -> file.toLowerCase(Locale.ROOT).contains(expectedService.toLowerCase(Locale.ROOT)));

        if (!hasService) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.BACKEND_REVIEW)
                    .area(PrReviewArea.ARCHITECTURE)
                    .severity(PrReviewSeverity.CRITICAL)
                    .title("Controller without backing service")
                    .location(controller.getFilePath() != null ? controller.getFilePath() : controller.getName())
                    .recommendation("Introduce or wire " + expectedService
                            + " to keep controller/service layering consistent.")
                    .blocking(true)
                    .build());
        }
        return findings;
    }

    private List<PrReviewFinding> reviewService(PrChangedArtifact service, SourceInventory inventory) {
        List<PrReviewFinding> findings = new ArrayList<>();
        String className = service.getName().replace(" ", "");
        Set<String> tests = SourceInventoryScanner.testsReferencingClass(className, inventory, inventoryScanner);

        if (tests.isEmpty()) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.BACKEND_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(PrReviewSeverity.HIGH)
                    .title("Service without unit or integration tests")
                    .location(service.getFilePath() != null ? service.getFilePath() : className)
                    .recommendation("Add API or integration tests exercising " + className + " public methods.")
                    .blocking(false)
                    .build());
        }
        return findings;
    }

    private List<PrReviewFinding> reviewRepository(PrChangedArtifact repository, SourceInventory inventory) {
        List<PrReviewFinding> findings = new ArrayList<>();
        String className = Path.of(repository.getFilePath() != null ? repository.getFilePath() : repository.getName())
                .getFileName().toString().replace(".java", "");
        Set<String> tests = SourceInventoryScanner.testsReferencingClass(className, inventory, inventoryScanner);

        if (tests.isEmpty()) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.BACKEND_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(PrReviewSeverity.HIGH)
                    .title("Repository methods without test coverage")
                    .location(repository.getFilePath() != null ? repository.getFilePath() : className)
                    .recommendation("Add repository/integration tests covering persistence paths in "
                            + className + ".")
                    .blocking(false)
                    .build());
        }
        return findings;
    }
}
