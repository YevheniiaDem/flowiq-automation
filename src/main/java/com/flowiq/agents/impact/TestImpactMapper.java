package com.flowiq.agents.impact;

import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ImpactMatrixEntry;
import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.model.TestSuiteType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class TestImpactMapper {

    private final Properties mappingProperties;

    public TestImpactMapper(AgentConfig agentConfig) {
        this.mappingProperties = loadMappingProperties(agentConfig.testMappingFile());
    }

    public Map<TestSuiteType, List<String>> mapChanges(List<ApiChange> changes) {
        Map<TestSuiteType, Set<String>> affected = new LinkedHashMap<>();
        for (TestSuiteType suite : TestSuiteType.values()) {
            affected.put(suite, new LinkedHashSet<>());
        }

        for (ApiChange change : changes) {
            if (change.getType() == com.flowiq.agents.model.ChangeType.BREAKING_CHANGE) {
                continue;
            }
            String domain = extractDomain(change.getPath());
            addMappedTests(affected, domain);
        }

        return affected.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> List.copyOf(e.getValue()),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    public List<ImpactMatrixEntry> buildMatrix(List<ApiChange> changes) {
        Map<String, List<ApiChange>> byEndpoint = new LinkedHashMap<>();
        for (ApiChange change : changes) {
            if (change.getType() == com.flowiq.agents.model.ChangeType.BREAKING_CHANGE) {
                continue;
            }
            String key = endpointKey(change);
            byEndpoint.computeIfAbsent(key, k -> new ArrayList<>()).add(change);
        }

        List<ImpactMatrixEntry> matrix = new ArrayList<>();
        for (Map.Entry<String, List<ApiChange>> entry : byEndpoint.entrySet()) {
            String domain = extractDomain(entry.getValue().get(0).getPath());
            matrix.add(ImpactMatrixEntry.builder()
                    .apiPath(entry.getValue().get(0).getPath())
                    .httpMethod(entry.getValue().get(0).getMethod())
                    .changes(entry.getValue())
                    .contractTests(singleOrEmpty(domain, "contract"))
                    .smokeTests(singleOrEmpty(domain, "smoke"))
                    .regressionTests(singleOrEmpty(domain, "regression"))
                    .uiTests(singleOrEmpty(domain, "ui"))
                    .riskLevel(RiskLevel.fromChanges(entry.getValue()))
                    .build());
        }
        return matrix;
    }

    public List<String> recommendedActions(List<ApiChange> changes, Map<TestSuiteType, List<String>> affectedTests) {
        List<String> actions = new ArrayList<>();
        if (changes.isEmpty()) {
            actions.add("No API changes detected. Continue with the existing test suites.");
            return actions;
        }

        boolean hasBreaking = changes.stream().anyMatch(ApiChange::isBreaking);
        if (hasBreaking) {
            actions.add("Review breaking changes with the backend team before merging.");
            actions.add("Update JSON Schema contract files under src/test/resources/schemas/.");
        }

        if (!affectedTests.getOrDefault(TestSuiteType.CONTRACT, List.of()).isEmpty()) {
            actions.add("Run contract suite: mvn test -Pcontract");
        }
        if (!affectedTests.getOrDefault(TestSuiteType.SMOKE, List.of()).isEmpty()) {
            actions.add("Run API smoke suite: mvn test -Papi-smoke");
        }
        if (!affectedTests.getOrDefault(TestSuiteType.REGRESSION, List.of()).isEmpty()) {
            actions.add("Run API regression suite: mvn test -Papi-regression");
        }
        if (!affectedTests.getOrDefault(TestSuiteType.UI, List.of()).isEmpty()) {
            actions.add("Run UI smoke suite: mvn test -Pui-smoke");
        }

        if (changes.stream().anyMatch(c -> c.getType() == com.flowiq.agents.model.ChangeType.ADDED_ENDPOINT)) {
            actions.add("Add coverage for new endpoints in services, contract schemas, and regression tests.");
        }
        if (changes.stream().anyMatch(c -> c.getType() == com.flowiq.agents.model.ChangeType.REMOVED_ENDPOINT)) {
            actions.add("Remove or deprecate tests and client code for removed endpoints.");
        }

        return actions;
    }

    private void addMappedTests(Map<TestSuiteType, Set<String>> affected, String domain) {
        addIfPresent(affected.get(TestSuiteType.CONTRACT), lookup(domain, "contract"));
        addIfPresent(affected.get(TestSuiteType.SMOKE), lookup(domain, "smoke"));
        addIfPresent(affected.get(TestSuiteType.REGRESSION), lookup(domain, "regression"));
        addIfPresent(affected.get(TestSuiteType.UI), lookup(domain, "ui"));
    }

    private void addIfPresent(Set<String> target, String testClass) {
        if (testClass != null && !testClass.isBlank()) {
            target.add(testClass);
        }
    }

    private List<String> singleOrEmpty(String domain, String suite) {
        String test = lookup(domain, suite);
        return test == null || test.isBlank() ? List.of() : List.of(test);
    }

    private String lookup(String domain, String suite) {
        String key = "domain." + domain + "." + suite;
        String value = mappingProperties.getProperty(key);
        if (value == null || value.isBlank()) {
            value = mappingProperties.getProperty("domain.default." + suite, "");
        }
        return value;
    }

    public static String extractDomain(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "default";
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        int slash = normalized.indexOf('/');
        String segment = slash >= 0 ? normalized.substring(0, slash) : normalized;
        return segment.isBlank() ? "default" : segment;
    }

    private static String endpointKey(ApiChange change) {
        String method = change.getMethod() != null ? change.getMethod() : "SCHEMA";
        String path = change.getPath() != null ? change.getPath() : change.getSchema();
        return method + " " + path;
    }

    private Properties loadMappingProperties(String location) {
        Properties properties = new Properties();
        try (InputStream input = openMappingStream(location)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            log.warn("Failed to load test impact mapping from {}: {}", location, e.getMessage());
        }
        return properties;
    }

    private InputStream openMappingStream(String location) throws IOException {
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
