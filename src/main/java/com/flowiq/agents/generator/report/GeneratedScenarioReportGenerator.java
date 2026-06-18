package com.flowiq.agents.generator.report;

import com.flowiq.agents.generator.config.SmartTestGeneratorConfig;
import com.flowiq.agents.generator.model.ScenarioGenerationResult;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class GeneratedScenarioReportGenerator {

    private final SmartTestGeneratorConfig config;

    public GeneratedScenarioReportGenerator(SmartTestGeneratorConfig config) {
        this.config = config;
    }

    public Path generate(ScenarioGenerationResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Generated test scenarios report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(ScenarioGenerationResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Generated Test Scenarios\n\n");
        md.append("_Implementation-ready QA scenarios — no executable code included_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getGeneratedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Summary\n\n");
        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Endpoints analyzed | ").append(result.getEndpointsAnalyzed()).append(" |\n");
        md.append("| JSON Schemas loaded | ").append(result.getSchemasLoaded()).append(" |\n");
        md.append("| Existing test references | ").append(result.getExistingTestReferences()).append(" |\n");
        md.append("| **Scenarios generated** | **").append(result.getScenarios().size()).append("** |\n");
        md.append("\n");

        md.append("### Scenarios by type\n\n");
        for (ScenarioType type : ScenarioType.values()) {
            long count = result.getScenariosByType().getOrDefault(type, 0L);
            md.append("- **").append(type).append(":** ").append(count).append("\n");
        }
        md.append("\n");

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        Map<ScenarioType, List<TestScenario>> byType = result.getScenarios().stream()
                .collect(Collectors.groupingBy(TestScenario::getType));

        for (ScenarioType type : ScenarioType.values()) {
            List<TestScenario> scenarios = byType.getOrDefault(type, List.of());
            if (scenarios.isEmpty()) {
                continue;
            }
            md.append("## ").append(type.name()).append(" Scenarios\n\n");
            scenarios.sort(Comparator.comparing(TestScenario::getPriority).thenComparing(TestScenario::getRisk));
            int index = 1;
            for (TestScenario scenario : scenarios) {
                md.append("### ").append(index++).append(". ").append(scenario.getTitle()).append("\n\n");
                md.append("| Attribute | Value |\n");
                md.append("|-----------|-------|\n");
                md.append("| **ID** | `").append(scenario.getId()).append("` |\n");
                md.append("| **Endpoint** | `").append(scenario.getHttpMethod())
                        .append(" ").append(scenario.getEndpoint()).append("` |\n");
                md.append("| **Module** | ").append(scenario.getModule()).append(" |\n");
                md.append("| **Priority** | ").append(scenario.getPriority()).append(" |\n");
                md.append("| **Risk** | ").append(scenario.getRisk()).append(" |\n");
                md.append("\n");

                md.append("**Preconditions**\n\n");
                scenario.getPreconditions().forEach(p -> md.append("- ").append(p).append("\n"));
                md.append("\n");

                md.append("**Steps**\n\n");
                int step = 1;
                for (String s : scenario.getSteps()) {
                    md.append(step++).append(". ").append(s).append("\n");
                }
                md.append("\n");

                md.append("**Expected Result**\n\n");
                md.append(scenario.getExpectedResult()).append("\n\n");
                md.append("---\n\n");
            }
        }

        if (result.getScenarios().isEmpty()) {
            md.append("_No uncovered scenarios identified — existing test suite appears comprehensive._\n\n");
        }

        md.append("## Implementation Notes\n\n");
        md.append("- Map each scenario ID to a TestNG/Allure test case and `@Story` annotation.\n");
        md.append("- Prioritize P1 scenarios in contract and smoke suites first.\n");
        md.append("- Re-run `SmartTestGeneratorAgent` after adding tests to refresh uncovered scenarios.\n\n");

        return md.toString();
    }

    private Path resolveReportPath() {
        Path path = Paths.get(config.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
