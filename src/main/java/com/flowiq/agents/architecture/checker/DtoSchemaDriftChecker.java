package com.flowiq.agents.architecture.checker;

import com.flowiq.agents.architecture.inventory.ArchitectureContext;
import com.flowiq.agents.architecture.inventory.SourceArtifact;
import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;
import com.flowiq.agents.architecture.model.DriftIssueType;
import com.flowiq.agents.architecture.model.DriftSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DtoSchemaDriftChecker implements ArchitectureDriftChecker {

    @Override
    public List<ArchitectureDriftIssue> check(ArchitectureContext context) {
        List<ArchitectureDriftIssue> issues = new ArrayList<>();
        List<Path> schemas = context.getSchemaFiles();

        for (SourceArtifact dto : context.getDtos()) {
            if (!hasMatchingSchema(dto.getName(), schemas)) {
                DriftSeverity severity = dto.getName().endsWith("Request") ? DriftSeverity.MEDIUM : DriftSeverity.LOW;
                issues.add(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.DTO_WITHOUT_SCHEMA)
                        .issue("DTO model has no matching JSON schema in test resources")
                        .severity(severity)
                        .location(dto.getRelativePath())
                        .recommendation("Add schema under src/test/resources/schemas/ for " + dto.getName() + ".")
                        .build());
            }
        }
        return issues;
    }

    private static boolean hasMatchingSchema(String dtoName, List<Path> schemas) {
        String stem = dtoName
                .replaceAll("Request$|Response$|Dto$", "")
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase(Locale.ROOT);
        List<String> tokens = Arrays.stream(stem.split("-"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toList());
        if (tokens.isEmpty()) {
            return false;
        }
        String compact = String.join("", tokens);
        return schemas.stream().anyMatch(path -> {
            String file = path.getFileName().toString().toLowerCase(Locale.ROOT);
            if (file.contains(compact)) {
                return true;
            }
            return tokens.stream().filter(t -> t.length() > 3).allMatch(file::contains);
        });
    }
}
