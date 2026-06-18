package com.flowiq.agents.review.scanner;

import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.review.config.TestReviewAgentConfig;
import com.flowiq.agents.review.model.FeatureChange;
import com.flowiq.agents.review.model.FeatureChangeType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FeatureChangeScanner {

    private static final Pattern CONTROLLER = Pattern.compile("([A-Za-z0-9]+)Controller\\.java$");
    private static final Pattern SERVICE = Pattern.compile("([A-Za-z0-9]+)Service\\.java$");
    private static final Pattern DTO = Pattern.compile("([A-Za-z0-9]+)(Dto|Request|Response)\\.java$",
            Pattern.CASE_INSENSITIVE);

    private final TestReviewAgentConfig config;

    public FeatureChangeScanner(TestReviewAgentConfig config) {
        this.config = config;
    }

    public List<FeatureChange> scan(List<String> changedFiles, List<ApiChange> apiChanges) {
        Map<String, FeatureChange.FeatureChangeBuilder> features = new LinkedHashMap<>();

        for (String file : changedFiles) {
            scanFilePath(file, features);
        }
        for (ApiChange change : apiChanges) {
            scanApiChange(change, features);
        }

        List<FeatureChange> result = features.values().stream()
                .map(FeatureChange.FeatureChangeBuilder::build)
                .toList();
        log.info("Identified {} feature change(s) for PR review", result.size());
        return result;
    }

    public List<String> loadChangedFiles() {
        if (config.changedFilesManifest() != null && !config.changedFilesManifest().isBlank()) {
            return loadManifest(config.changedFilesManifest());
        }
        if (config.useGitDiff()) {
            List<String> gitFiles = loadGitDiff();
            if (!gitFiles.isEmpty()) {
                return gitFiles;
            }
        }
        return List.of();
    }

    private void scanFilePath(String file, Map<String, FeatureChange.FeatureChangeBuilder> features) {
        String normalized = file.replace('\\', '/');
        String pathModule = moduleFromFilePath(normalized);
        Matcher controller = CONTROLLER.matcher(normalized);
        if (controller.find()) {
            String name = controller.group(1);
            String module = pathModule != null ? pathModule : moduleFromName(name);
            merge(features, module + "-controller", FeatureChange.builder()
                    .featureId(module + "-controller")
                    .featureName(name + " Controller")
                    .changeType(FeatureChangeType.CONTROLLER)
                    .module(module)
                    .description("Controller modified: " + name)
                    .changedFile(normalized));
            return;
        }
        Matcher service = SERVICE.matcher(normalized);
        if (service.find()) {
            String name = service.group(1);
            String module = pathModule != null ? pathModule : moduleFromName(name);
            merge(features, module + "-service", FeatureChange.builder()
                    .featureId(module + "-service")
                    .featureName(name + " Service")
                    .changeType(FeatureChangeType.SERVICE)
                    .module(module)
                    .description("Service modified: " + name)
                    .changedFile(normalized));
            return;
        }
        Matcher dto = DTO.matcher(normalized);
        if (dto.find()) {
            String name = dto.group(1) + dto.group(2);
            String module = pathModule != null ? pathModule : moduleFromName(dto.group(1));
            merge(features, module + "-dto-" + name.toLowerCase(Locale.ROOT), FeatureChange.builder()
                    .featureId(module + "-dto-" + name.toLowerCase(Locale.ROOT))
                    .featureName(name + " DTO")
                    .changeType(FeatureChangeType.DTO)
                    .module(module)
                    .schemaName(name)
                    .description("DTO modified: " + name)
                    .changedFile(normalized));
        }
    }

    private void scanApiChange(ApiChange change, Map<String, FeatureChange.FeatureChangeBuilder> features) {
        if (change.getType() == ChangeType.BREAKING_CHANGE) {
            return;
        }
        if (change.getPath() != null && change.getMethod() != null) {
            String module = extractModule(change.getPath());
            String id = module + "-" + change.getMethod() + "-" + change.getPath().replace("/", "-");
            merge(features, id, FeatureChange.builder()
                    .featureId(id)
                    .featureName(change.getMethod() + " " + change.getPath())
                    .changeType(FeatureChangeType.ENDPOINT)
                    .module(module)
                    .httpMethod(change.getMethod())
                    .endpointPath(change.getPath())
                    .description(change.getDescription()));
            return;
        }
        if (change.getSchema() != null) {
            String module = moduleFromSchema(change.getSchema());
            String id = module + "-schema-" + change.getSchema().toLowerCase(Locale.ROOT);
            merge(features, id, FeatureChange.builder()
                    .featureId(id)
                    .featureName(change.getSchema() + " schema")
                    .changeType(FeatureChangeType.DTO)
                    .module(module)
                    .schemaName(change.getSchema())
                    .description(change.getDescription()));
        }
    }

    private static void merge(Map<String, FeatureChange.FeatureChangeBuilder> features,
                            String id,
                            FeatureChange.FeatureChangeBuilder incoming) {
        features.merge(id, incoming, (existing, added) -> {
            FeatureChange built = added.build();
            built.getChangedFiles().forEach(existing::changedFile);
            return existing;
        });
    }

    private List<String> loadGitDiff() {
        try {
            Process process = new ProcessBuilder("git", "diff", "--name-only",
                    config.gitBaseRef() + "...HEAD")
                    .directory(Paths.get(System.getProperty("user.dir")).toFile())
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished || process.exitValue() != 0) {
                log.debug("Git diff unavailable or empty (exit={})", finished ? process.exitValue() : "timeout");
                return List.of();
            }
            String output = new String(process.getInputStream().readAllBytes());
            return output.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .toList();
        } catch (Exception e) {
            log.debug("Git diff failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> loadManifest(String location) {
        Path path = Paths.get(location);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        if (!Files.isRegularFile(path)) {
            log.warn("Changed files manifest not found: {}", path);
            return List.of();
        }
        try {
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            log.warn("Failed to read manifest {}: {}", path, e.getMessage());
            return List.of();
        }
    }

    private static String moduleFromFilePath(String normalized) {
        Matcher packageModule = Pattern.compile("/flowiq/([^/]+)/").matcher(normalized);
        if (packageModule.find()) {
            return packageModule.group(1);
        }
        return null;
    }

    private static String moduleFromName(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    private static String moduleFromSchema(String schema) {
        String normalized = schema.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
        if (normalized.contains("login") || normalized.contains("auth")) {
            return "auth";
        }
        if (normalized.contains("task")) {
            return "tasks";
        }
        if (normalized.contains("transaction")) {
            return "transactions";
        }
        int cut = normalized.indexOf("request");
        if (cut < 0) {
            cut = normalized.indexOf("response");
        }
        return cut > 0 ? normalized.substring(0, cut).replaceAll("-+$", "") : normalized;
    }

    private static String extractModule(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "default";
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        int slash = normalized.indexOf('/');
        return slash >= 0 ? normalized.substring(0, slash) : normalized;
    }
}
