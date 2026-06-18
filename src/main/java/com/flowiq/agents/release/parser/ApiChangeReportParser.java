package com.flowiq.agents.release.parser;

import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.release.config.ReleaseRiskAgentConfig;
import com.flowiq.agents.release.model.ApiChangeReportInsight;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ApiChangeReportParser {

    private static final Pattern RISK_LEVEL = Pattern.compile("## Risk Level\\s+\\*\\*(LOW|MEDIUM|HIGH)\\*\\*");
    private static final Pattern CHANGE_LINE = Pattern.compile("- \\*\\*[^:]+\\*\\*: (.+?)(?: _\\(breaking\\)_)?$",
            Pattern.MULTILINE);
    private static final Pattern LIST_ITEM = Pattern.compile("- ([A-Za-z0-9]+Test)");

    private final ReleaseRiskAgentConfig config;

    public ApiChangeReportParser(ReleaseRiskAgentConfig config) {
        this.config = config;
    }

    public ApiChangeReportInsight parse() {
        Path path = resolvePath(config.apiChangeReportPath());
        if (!Files.isRegularFile(path)) {
            log.warn("API change report not found: {}", path);
            return ApiChangeReportInsight.builder()
                    .reportFound(false)
                    .riskLevel(RiskLevel.LOW)
                    .summary("API change report not available")
                    .build();
        }
        try {
            return parseContent(Files.readString(path));
        } catch (IOException e) {
            log.warn("Failed to read API change report {}: {}", path, e.getMessage());
            return ApiChangeReportInsight.builder()
                    .reportFound(false)
                    .riskLevel(RiskLevel.LOW)
                    .summary("Failed to read API change report")
                    .build();
        }
    }

    public ApiChangeReportInsight parseContent(String content) {
        RiskLevel riskLevel = extractRiskLevel(content);
        List<String> changes = new ArrayList<>();
        List<String> breaking = new ArrayList<>();

        Matcher changeMatcher = CHANGE_LINE.matcher(content);
        while (changeMatcher.find()) {
            String line = changeMatcher.group(0);
            String description = changeMatcher.group(1).trim();
            changes.add(description);
            if (line.contains("_(breaking)_")) {
                breaking.add(description);
            }
        }

        List<String> contractTests = extractContractTests(content);

        String summary = changes.isEmpty()
                ? "No API changes detected"
                : changes.size() + " change(s), " + breaking.size() + " breaking, risk " + riskLevel;

        return ApiChangeReportInsight.builder()
                .reportFound(true)
                .riskLevel(riskLevel)
                .totalChanges(changes.size())
                .breakingChanges(breaking.size())
                .breakingChangeDescriptions(breaking)
                .affectedContractTests(contractTests)
                .summary(summary)
                .build();
    }

    private static RiskLevel extractRiskLevel(String content) {
        Matcher matcher = RISK_LEVEL.matcher(content);
        if (!matcher.find()) {
            return RiskLevel.LOW;
        }
        return RiskLevel.valueOf(matcher.group(1));
    }

    private static List<String> extractContractTests(String content) {
        List<String> tests = new ArrayList<>();
        int sectionStart = content.indexOf("### Contract Tests");
        if (sectionStart < 0) {
            return tests;
        }
        String section = content.substring(sectionStart + "### Contract Tests".length());
        int nextHeading = section.indexOf("\n###");
        if (nextHeading >= 0) {
            section = section.substring(0, nextHeading);
        }
        Matcher item = LIST_ITEM.matcher(section);
        while (item.find()) {
            tests.add(item.group(1));
        }
        return tests;
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
