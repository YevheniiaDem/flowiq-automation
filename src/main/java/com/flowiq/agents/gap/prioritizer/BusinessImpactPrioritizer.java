package com.flowiq.agents.gap.prioritizer;

import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.model.GapType;
import com.flowiq.agents.gap.model.TestGap;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class BusinessImpactPrioritizer {

    private final Map<String, GapSeverity> moduleImpact;
    private final Set<String> uiExpectedModules;

    public BusinessImpactPrioritizer(TestGapAgentConfig config) {
        Properties properties = loadProperties(config.businessImpactFile());
        this.moduleImpact = parseModuleImpact(properties);
        this.uiExpectedModules = parseUiExpected(properties);
    }

    public GapSeverity businessImpactFor(String module) {
        return moduleImpact.getOrDefault(module, GapSeverity.MEDIUM);
    }

    public boolean uiExpected(String module) {
        return uiExpectedModules.contains(module);
    }

    public Set<String> uiExpectedModules() {
        return Set.copyOf(uiExpectedModules);
    }

    public Map<String, GapSeverity> moduleImpacts() {
        return Map.copyOf(moduleImpact);
    }

    public TestGap prioritize(TestGap gap) {
        GapSeverity business = businessImpactFor(gap.getModule());
        GapSeverity severity = escalate(gap.getType(), business, gap.getModule());
        return TestGap.builder()
                .type(gap.getType())
                .severity(severity)
                .module(gap.getModule())
                .path(gap.getPath())
                .method(gap.getMethod())
                .description(gap.getDescription())
                .recommendedTest(gap.getRecommendedTest())
                .build();
    }

    private GapSeverity escalate(GapType type, GapSeverity business, String module) {
        return switch (type) {
            case NO_TEST_COVERAGE -> business == GapSeverity.CRITICAL ? GapSeverity.CRITICAL : GapSeverity.HIGH;
            case MISSING_CONTRACT_COVERAGE -> business.ordinal() <= GapSeverity.HIGH.ordinal()
                    ? GapSeverity.HIGH : GapSeverity.MEDIUM;
            case MISSING_REGRESSION_COVERAGE, MISSING_AUTHORIZATION_CHECK, MISSING_NEGATIVE_SCENARIO ->
                    business == GapSeverity.CRITICAL ? GapSeverity.HIGH : GapSeverity.MEDIUM;
            case MISSING_UPDATE_TEST, MISSING_DELETE_TEST -> GapSeverity.MEDIUM;
            case MISSING_SMOKE_COVERAGE -> GapSeverity.MEDIUM;
            case MISSING_UI_COVERAGE -> uiExpected(module) ? GapSeverity.LOW : GapSeverity.LOW;
        };
    }

    private Map<String, GapSeverity> parseModuleImpact(Properties properties) {
        Map<String, GapSeverity> impact = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith("module.") || !key.endsWith(".business-impact")) {
                continue;
            }
            String module = key.substring("module.".length(), key.length() - ".business-impact".length());
            impact.put(module, GapSeverity.valueOf(properties.getProperty(key).trim().toUpperCase()));
        }
        return impact;
    }

    private Set<String> parseUiExpected(Properties properties) {
        String value = properties.getProperty("module.ui.expected", "");
        Set<String> modules = new HashSet<>();
        if (value != null && !value.isBlank()) {
            Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .forEach(modules::add);
        }
        return modules;
    }

    private Properties loadProperties(String location) {
        Properties properties = new Properties();
        try (InputStream input = openStream(location)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            log.warn("Failed to load business impact config from {}: {}", location, e.getMessage());
        }
        return properties;
    }

    private InputStream openStream(String location) throws IOException {
        if (location.startsWith("classpath:")) {
            String resource = location.substring("classpath:".length());
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (stream == null) {
                throw new IOException("Classpath resource not found: " + resource);
            }
            return stream;
        }
        Path path = Paths.get(location);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return Files.newInputStream(path);
    }
}
