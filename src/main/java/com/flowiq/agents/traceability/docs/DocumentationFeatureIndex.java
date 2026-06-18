package com.flowiq.agents.traceability.docs;

import com.flowiq.agents.traceability.config.TraceabilityAgentConfig;
import com.flowiq.agents.traceability.model.BusinessFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class DocumentationFeatureIndex {

    private final Path docsRoot;
    private final List<DocFeatureExtractor> extractors;

    public DocumentationFeatureIndex(TraceabilityAgentConfig config) {
        this.docsRoot = resolvePath(config.docsDirectory());
        this.extractors = List.of(
                new RegressionDocExtractor(),
                new ContractDocExtractor(),
                new UiSmokeDocExtractor()
        );
    }

    public List<BusinessFeature> index() {
        Map<String, BusinessFeature.BusinessFeatureBuilder> merged = new LinkedHashMap<>();
        for (DocFeatureExtractor extractor : extractors) {
            Path docFile = docsRoot.resolve(extractor.docFileName());
            if (!Files.isRegularFile(docFile)) {
                log.warn("Documentation file not found: {}", docFile);
                continue;
            }
            try {
                String content = Files.readString(docFile);
                for (BusinessFeature feature : extractor.extract(content)) {
                    merged.compute(feature.getModule(), (module, builder) -> {
                        if (builder == null) {
                            var created = BusinessFeature.builder()
                                    .module(feature.getModule())
                                    .displayName(feature.getDisplayName())
                                    .description(feature.getDescription());
                            feature.getDocSources().forEach(created::docSource);
                            return created;
                        }
                        feature.getDocSources().forEach(builder::docSource);
                        return builder;
                    });
                }
            } catch (IOException e) {
                log.warn("Failed to read {}: {}", docFile, e.getMessage());
            }
        }
        List<BusinessFeature> features = merged.values().stream()
                .map(BusinessFeature.BusinessFeatureBuilder::build)
                .toList();
        log.info("Indexed {} business feature(s) from documentation", features.size());
        return features;
    }

    public List<String> listDocSources() {
        List<String> sources = new ArrayList<>();
        if (!Files.isDirectory(docsRoot)) {
            return sources;
        }
        try (Stream<Path> files = Files.list(docsRoot)) {
            files.filter(p -> p.toString().endsWith(".md"))
                    .forEach(p -> sources.add(p.getFileName().toString()));
        } catch (IOException e) {
            log.warn("Failed to list docs directory: {}", e.getMessage());
        }
        return sources;
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
