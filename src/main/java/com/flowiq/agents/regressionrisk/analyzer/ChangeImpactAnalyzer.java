package com.flowiq.agents.regressionrisk.analyzer;

import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.regressionrisk.model.AffectedTests;
import com.flowiq.agents.regressionrisk.model.ReleaseChangeContext;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ChangeImpactAnalyzer {

    private final Properties mappingProperties;

    public ChangeImpactAnalyzer() {
        this(loadMappingProperties(ConfigFactory.create(AgentConfig.class).testMappingFile()));
    }

    public ChangeImpactAnalyzer(Properties mappingProperties) {
        this.mappingProperties = mappingProperties;
    }

    public Map<String, ModuleImpactDraft> analyze(ReleaseChangeContext context) {
        Map<String, ModuleImpactDraft> impacts = new LinkedHashMap<>();

        for (String module : context.getBackendModules()) {
            impacts.computeIfAbsent(module, ModuleImpactDraft::new).setBackendChanged(true);
        }
        for (String module : context.getFrontendModules()) {
            impacts.computeIfAbsent(module, ModuleImpactDraft::new).setFrontendChanged(true);
        }

        for (ApiChange change : context.getApiChanges()) {
            String module = extractModule(change);
            ModuleImpactDraft draft = impacts.computeIfAbsent(module, ModuleImpactDraft::new);
            draft.setBackendChanged(true);
            draft.getApiChanges().add(change);
        }

        for (Map.Entry<String, ModuleImpactDraft> entry : impacts.entrySet()) {
            entry.getValue().setAllAffectedTests(mapTestsForModule(entry.getKey()));
        }

        log.info("Change impact analysis identified {} affected module(s)", impacts.size());
        return impacts;
    }

    private AffectedTests mapTestsForModule(String module) {
        return AffectedTests.builder()
                .smokeTests(nonBlank(lookup(module, "smoke")))
                .contractTests(nonBlank(lookup(module, "contract")))
                .regressionTests(nonBlank(lookup(module, "regression")))
                .uiTests(nonBlank(lookup(module, "ui")))
                .build();
    }

    private static List<String> nonBlank(String value) {
        return value == null || value.isBlank() ? List.of() : List.of(value);
    }

    private String lookup(String module, String suite) {
        String key = "domain." + module + "." + suite;
        String value = mappingProperties.getProperty(key);
        if (value == null || value.isBlank()) {
            value = mappingProperties.getProperty("domain.default." + suite, "");
        }
        return value;
    }

    private static String extractModule(ApiChange change) {
        if (change.getPath() != null && !change.getPath().isBlank()) {
            String normalized = change.getPath().startsWith("/")
                    ? change.getPath().substring(1) : change.getPath();
            int slash = normalized.indexOf('/');
            return slash >= 0 ? normalized.substring(0, slash) : normalized;
        }
        return "default";
    }

    private static Properties loadMappingProperties(String location) {
        Properties properties = new Properties();
        try (InputStream input = openStream(location)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            log.warn("Failed to load test impact mapping from {}: {}", location, e.getMessage());
        }
        return properties;
    }

    private static InputStream openStream(String location) throws IOException {
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

    public static class ModuleImpactDraft {
        private final String module;
        private boolean backendChanged;
        private boolean frontendChanged;
        private final List<ApiChange> apiChanges = new ArrayList<>();
        private AffectedTests allAffectedTests = AffectedTests.builder().build();

        public ModuleImpactDraft(String module) {
            this.module = module;
        }

        public String getModule() {
            return module;
        }

        public boolean isBackendChanged() {
            return backendChanged;
        }

        public void setBackendChanged(boolean backendChanged) {
            this.backendChanged = backendChanged;
        }

        public boolean isFrontendChanged() {
            return frontendChanged;
        }

        public void setFrontendChanged(boolean frontendChanged) {
            this.frontendChanged = frontendChanged;
        }

        public List<ApiChange> getApiChanges() {
            return apiChanges;
        }

        public AffectedTests getAllAffectedTests() {
            return allAffectedTests;
        }

        public void setAllAffectedTests(AffectedTests allAffectedTests) {
            this.allAffectedTests = allAffectedTests;
        }
    }
}
