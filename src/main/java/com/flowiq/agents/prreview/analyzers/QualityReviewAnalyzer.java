package com.flowiq.agents.prreview.analyzers;

import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.prreview.model.PrChangedArtifact;
import com.flowiq.agents.prreview.model.PrChangedArtifactType;
import com.flowiq.agents.prreview.model.PrReviewArea;
import com.flowiq.agents.prreview.model.PrReviewCategory;
import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewSeverity;
import com.flowiq.agents.prreview.scanner.PrReviewContext;
import com.flowiq.agents.prreview.scanner.SourceInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QualityReviewAnalyzer implements PrReviewAnalyzer {

    private static final Pattern DEAD_REFERENCE = Pattern.compile(
            "(?i)(import com\\.flowiq\\.[\\w.]+|new [A-Z][A-Za-z0-9_]+\\(|extends [A-Z][A-Za-z0-9_]+)");
    private static final Pattern CLASS_NAME = Pattern.compile("class\\s+([A-Za-z0-9_]+)");

    @Override
    public String name() {
        return "QualityReviewAnalyzer";
    }

    @Override
    public List<PrReviewFinding> analyze(PrReviewContext context) {
        List<PrReviewFinding> findings = new ArrayList<>();
        findings.addAll(findDuplicatedTests(context.getTestReferences()));
        findings.addAll(findDuplicatedDtos(context.getSourceInventory(), context.getChangedArtifacts()));
        findings.addAll(findDeadCodeReferences(context));
        return findings;
    }

    private List<PrReviewFinding> findDuplicatedTests(List<ScannedTestReference> tests) {
        Map<String, Set<String>> pathToClasses = new HashMap<>();
        for (ScannedTestReference test : tests) {
            if (test.getPath() == null || test.getPath().isBlank()) {
                continue;
            }
            String key = test.getMethod() + " " + test.getPath();
            pathToClasses.computeIfAbsent(key, ignored -> new HashSet<>()).add(test.getClassName());
        }

        List<PrReviewFinding> findings = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : pathToClasses.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.QUALITY_REVIEW)
                    .area(PrReviewArea.QA)
                    .severity(PrReviewSeverity.MEDIUM)
                    .title("Duplicated test coverage for the same endpoint")
                    .location(entry.getKey())
                    .recommendation("Consolidate overlapping tests in classes: "
                            + String.join(", ", entry.getValue()) + ".")
                    .blocking(false)
                    .build());
        }
        return findings;
    }

    private List<PrReviewFinding> findDuplicatedDtos(SourceInventory inventory,
                                                     List<PrChangedArtifact> artifacts) {
        Map<String, List<String>> dtoLocations = new HashMap<>();
        for (String dtoFile : inventory.getDtoFiles()) {
            String className = extractSimpleName(dtoFile);
            dtoLocations.computeIfAbsent(className.toLowerCase(Locale.ROOT), ignored -> new ArrayList<>())
                    .add(dtoFile);
        }
        for (PrChangedArtifact artifact : artifacts) {
            if (artifact.getType() == PrChangedArtifactType.DTO && artifact.getSchemaName() != null) {
                dtoLocations.computeIfAbsent(artifact.getSchemaName().toLowerCase(Locale.ROOT),
                                ignored -> new ArrayList<>())
                        .add(artifact.getFilePath() != null ? artifact.getFilePath() : artifact.getSchemaName());
            }
        }

        List<PrReviewFinding> findings = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : dtoLocations.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.QUALITY_REVIEW)
                    .area(PrReviewArea.ARCHITECTURE)
                    .severity(PrReviewSeverity.MEDIUM)
                    .title("Duplicated DTO definition detected")
                    .location(entry.getKey())
                    .recommendation("Merge duplicate DTOs and keep a single schema-backed model: "
                            + String.join(", ", entry.getValue()) + ".")
                    .blocking(false)
                    .build());
        }
        return findings;
    }

    private List<PrReviewFinding> findDeadCodeReferences(PrReviewContext context) {
        Set<String> knownClasses = new HashSet<>(context.getSourceInventory().getServiceClassNames());
        knownClasses.addAll(context.getSourceInventory().getPageClassNames());
        knownClasses.addAll(context.getSourceInventory().getDtoClassNames());

        List<PrReviewFinding> findings = new ArrayList<>();
        for (String changedFile : context.getChangedFiles()) {
            if (!changedFile.endsWith(".java")) {
                continue;
            }
            String content = context.getChangedArtifacts().stream()
                    .filter(a -> changedFile.equals(a.getFilePath()))
                    .map(PrChangedArtifact::getSourceContent)
                    .filter(c -> c != null && !c.isBlank())
                    .findFirst()
                    .orElse("");

            if (content.isBlank()) {
                continue;
            }
            Matcher matcher = DEAD_REFERENCE.matcher(content);
            while (matcher.find()) {
                String token = matcher.group();
                String referenced = extractReferencedClass(token);
                if (referenced == null || referenced.endsWith("Test") || knownClasses.contains(referenced)) {
                    continue;
                }
                if (referenced.endsWith("Controller") || referenced.endsWith("Service")
                        || referenced.endsWith("Page") || referenced.endsWith("Request")
                        || referenced.endsWith("Response")) {
                    boolean exists = context.getSourceInventory().getServiceFiles().stream()
                                    .anyMatch(f -> f.contains(referenced))
                            || context.getSourceInventory().getPageObjectFiles().stream()
                                    .anyMatch(f -> f.contains(referenced))
                            || context.getSourceInventory().getDtoFiles().stream()
                                    .anyMatch(f -> f.contains(referenced));
                    if (!exists) {
                        findings.add(PrReviewFinding.builder()
                                .category(PrReviewCategory.QUALITY_REVIEW)
                                .area(PrReviewArea.ARCHITECTURE)
                                .severity(PrReviewSeverity.LOW)
                                .title("Possible dead code reference")
                                .location(changedFile + " -> " + referenced)
                                .recommendation("Verify " + referenced + " still exists or remove stale reference.")
                                .blocking(false)
                                .build());
                    }
                }
            }
        }
        return findings;
    }

    private static String extractReferencedClass(String token) {
        if (token.startsWith("import ")) {
            String fqcn = token.substring("import ".length()).trim();
            int lastDot = fqcn.lastIndexOf('.');
            return lastDot >= 0 ? fqcn.substring(lastDot + 1) : fqcn;
        }
        if (token.startsWith("new ")) {
            return token.substring(4).replace("(", "").trim();
        }
        if (token.startsWith("extends ")) {
            return token.substring("extends ".length()).trim();
        }
        return null;
    }

    private static String extractSimpleName(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        return fileName.replace(".java", "");
    }
}
