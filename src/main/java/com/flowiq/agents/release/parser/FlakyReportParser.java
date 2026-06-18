package com.flowiq.agents.release.parser;

import com.flowiq.agents.release.config.ReleaseRiskAgentConfig;
import com.flowiq.agents.release.model.FlakyReportInsight;
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
public class FlakyReportParser {

    private static final Pattern FLAKY_COUNT = Pattern.compile("Flaky tests detected \\| (\\d+)");
    private static final Pattern PASS_RATE = Pattern.compile("\\*\\*Pass rate\\*\\* \\| \\*\\*(\\d+\\.?\\d*)%\\*\\*");
    private static final Pattern FLAKINESS = Pattern.compile("\\*\\*Flakiness %\\*\\* \\| \\*\\*(\\d+\\.?\\d*)%\\*\\*");
    private static final Pattern TOP_TEST = Pattern.compile("\\| \\d+ \\| `([^`]+)` \\|");

    private final ReleaseRiskAgentConfig config;

    public FlakyReportParser(ReleaseRiskAgentConfig config) {
        this.config = config;
    }

    public FlakyReportInsight parse() {
        Path path = resolvePath(config.flakyReportPath());
        if (!Files.isRegularFile(path)) {
            log.warn("Flaky report not found: {}", path);
            return FlakyReportInsight.builder()
                    .reportFound(false)
                    .summary("Flaky report not available")
                    .build();
        }
        try {
            return parseContent(Files.readString(path));
        } catch (IOException e) {
            log.warn("Failed to read flaky report {}: {}", path, e.getMessage());
            return FlakyReportInsight.builder()
                    .reportFound(false)
                    .summary("Failed to read flaky report")
                    .build();
        }
    }

    public FlakyReportInsight parseContent(String content) {
        int flakyCount = extractInt(FLAKY_COUNT, content, 0);
        double passRate = extractDouble(PASS_RATE, content, 100.0);
        double flakiness = extractDouble(FLAKINESS, content, 0.0);

        List<String> topTests = new ArrayList<>();
        Matcher matcher = TOP_TEST.matcher(content);
        while (matcher.find() && topTests.size() < 5) {
            topTests.add(matcher.group(1));
        }

        String summary = flakyCount == 0
                ? "No flaky tests detected"
                : flakyCount + " flaky test(s), portfolio flakiness " + flakiness + "%";

        return FlakyReportInsight.builder()
                .reportFound(true)
                .flakyTestCount(flakyCount)
                .portfolioPassRate(passRate)
                .portfolioFlakinessPercent(flakiness)
                .topUnstableTests(topTests)
                .summary(summary)
                .build();
    }

    private static int extractInt(Pattern pattern, String content, int defaultValue) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
    }

    private static double extractDouble(Pattern pattern, String content, double defaultValue) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : defaultValue;
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
