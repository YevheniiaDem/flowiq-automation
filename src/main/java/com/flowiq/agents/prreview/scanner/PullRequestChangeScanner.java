package com.flowiq.agents.prreview.scanner;

import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.prreview.config.PullRequestReviewAgentConfig;
import com.flowiq.agents.prreview.model.PrChangedArtifact;
import com.flowiq.agents.prreview.model.PrChangedArtifactType;
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
public class PullRequestChangeScanner {

    private static final Pattern CONTROLLER = Pattern.compile("([A-Za-z0-9]+)Controller\\.java$");
    private static final Pattern SERVICE = Pattern.compile("([A-Za-z0-9]+)Service\\.java$");
    private static final Pattern REPOSITORY = Pattern.compile("([A-Za-z0-9]+)Repository\\.java$");
    private static final Pattern DTO = Pattern.compile("([A-Za-z0-9]+)(Dto|Request|Response)\\.java$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PAGE = Pattern.compile("([A-Za-z0-9]+)Page\\.java$");
    private static final Pattern SCHEMA = Pattern.compile("schemas/.+\\.schema\\.json$|schemas/.+-schema\\.json$",
            Pattern.CASE_INSENSITIVE);

    private final PullRequestReviewAgentConfig config;
    private final SourceInventoryScanner inventoryScanner;

    public PullRequestChangeScanner(PullRequestReviewAgentConfig config) {
        this.config = config;
        this.inventoryScanner = new SourceInventoryScanner(config);
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

    public List<PrChangedArtifact> scan(List<String> changedFiles, List<ApiChange> apiChanges) {
        Map<String, PrChangedArtifact.PrChangedArtifactBuilder> artifacts = new LinkedHashMap<>();

        for (String file : changedFiles) {
            scanFilePath(file, artifacts);
        }
        for (ApiChange change : apiChanges) {
            scanApiChange(change, artifacts);
        }

        List<PrChangedArtifact> result = artifacts.values().stream()
                .map(PrChangedArtifact.PrChangedArtifactBuilder::build)
                .toList();
        log.info("Identified {} PR artifact(s) for review", result.size());
        return result;
    }

    private void scanFilePath(String file, Map<String, PrChangedArtifact.PrChangedArtifactBuilder> artifacts) {
        String normalized = file.replace('\\', '/');
        String module = moduleFromFilePath(normalized);
        String content = inventoryScanner.readFileContent(normalized);

        Matcher controller = CONTROLLER.matcher(normalized);
        if (controller.find()) {
            String name = controller.group(1);
            merge(artifacts, module + "-controller", artifactBuilder(name + "Controller", name + " Controller",
                    PrChangedArtifactType.CONTROLLER, module, normalized, content,
                    "Controller modified: " + name));
            return;
        }
        Matcher service = SERVICE.matcher(normalized);
        if (service.find()) {
            String name = service.group(1);
            merge(artifacts, module + "-service", artifactBuilder(name + "Service", name + " Service",
                    PrChangedArtifactType.SERVICE, module, normalized, content,
                    "Service modified: " + name));
            return;
        }
        Matcher repository = REPOSITORY.matcher(normalized);
        if (repository.find()) {
            String name = repository.group(1);
            merge(artifacts, module + "-repository", artifactBuilder(name + "Repository", name + " Repository",
                    PrChangedArtifactType.REPOSITORY, module, normalized, content,
                    "Repository modified: " + name));
            return;
        }
        Matcher dto = DTO.matcher(normalized);
        if (dto.find()) {
            String schemaName = dto.group(1) + dto.group(2);
            merge(artifacts, module + "-dto-" + schemaName.toLowerCase(Locale.ROOT),
                    artifactBuilder(schemaName, schemaName + " DTO", PrChangedArtifactType.DTO, module, normalized,
                            content, "DTO modified: " + schemaName).schemaName(schemaName));
            return;
        }
        Matcher page = PAGE.matcher(normalized);
        if (page.find()) {
            String name = page.group(1);
            merge(artifacts, module + "-page-" + name.toLowerCase(Locale.ROOT),
                    artifactBuilder(name + "Page", name + " Page", PrChangedArtifactType.PAGE, module, normalized,
                            content, "Page modified: " + name));
            return;
        }
        if (SCHEMA.matcher(normalized).find()) {
            merge(artifacts, normalized, PrChangedArtifact.builder()
                    .artifactId(normalized)
                    .name(Path.of(normalized).getFileName().toString())
                    .type(PrChangedArtifactType.SCHEMA)
                    .module(module != null ? module : "schemas")
                    .filePath(normalized)
                    .sourceContent(content)
                    .description("Schema modified"));
            return;
        }
        if (normalized.contains("/test/") || normalized.endsWith("Test.java") || normalized.endsWith("IT.java")) {
            merge(artifacts, normalized, PrChangedArtifact.builder()
                    .artifactId(normalized)
                    .name(Path.of(normalized).getFileName().toString())
                    .type(PrChangedArtifactType.TEST)
                    .module(module != null ? module : inferModuleFromTest(normalized))
                    .filePath(normalized)
                    .sourceContent(content)
                    .description("Test modified"));
        }
    }

    private void scanApiChange(ApiChange change, Map<String, PrChangedArtifact.PrChangedArtifactBuilder> artifacts) {
        if (change.getType() == ChangeType.BREAKING_CHANGE) {
            return;
        }
        if (change.getPath() != null && change.getMethod() != null) {
            String module = extractModule(change.getPath());
            String id = module + "-" + change.getMethod() + "-" + change.getPath().replace("/", "-");
            boolean added = change.getType() == ChangeType.ADDED_ENDPOINT;
            merge(artifacts, id, PrChangedArtifact.builder()
                    .artifactId(id)
                    .name(change.getMethod() + " " + change.getPath())
                    .type(PrChangedArtifactType.ENDPOINT)
                    .module(module)
                    .httpMethod(change.getMethod())
                    .endpointPath(change.getPath())
                    .description(change.getDescription())
                    .newlyAdded(added));
            return;
        }
        if (change.getSchema() != null) {
            String module = moduleFromSchema(change.getSchema());
            String id = module + "-schema-" + change.getSchema().toLowerCase(Locale.ROOT);
            merge(artifacts, id, PrChangedArtifact.builder()
                    .artifactId(id)
                    .name(change.getSchema() + " schema")
                    .type(PrChangedArtifactType.DTO)
                    .module(module)
                    .schemaName(change.getSchema())
                    .description(change.getDescription())
                    .newlyAdded(change.getType() == ChangeType.MODIFIED_REQUEST_SCHEMA
                            || change.getType() == ChangeType.MODIFIED_RESPONSE_SCHEMA
                            || change.getType() == ChangeType.ADDED_REQUIRED_FIELD));
        }
    }

    private static PrChangedArtifact.PrChangedArtifactBuilder artifactBuilder(String artifactId,
                                                                              String name,
                                                                              PrChangedArtifactType type,
                                                                              String module,
                                                                              String filePath,
                                                                              String content,
                                                                              String description) {
        return PrChangedArtifact.builder()
                .artifactId(artifactId)
                .name(name)
                .type(type)
                .module(module != null ? module : "default")
                .filePath(filePath)
                .sourceContent(content)
                .description(description);
    }

    private static void merge(Map<String, PrChangedArtifact.PrChangedArtifactBuilder> artifacts,
                              String id,
                              PrChangedArtifact.PrChangedArtifactBuilder incoming) {
        artifacts.merge(id, incoming, (existing, added) -> {
            PrChangedArtifact built = added.build();
            if (built.getFilePath() != null) {
                existing.filePath(built.getFilePath());
            }
            if (built.getSourceContent() != null && !built.getSourceContent().isBlank()) {
                existing.sourceContent(built.getSourceContent());
            }
            built.getRelatedFiles().forEach(existing::relatedFile);
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
                return List.of();
            }
            String output = new String(process.getInputStream().readAllBytes());
            return output.lines().map(String::trim).filter(line -> !line.isBlank()).toList();
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

    private static String inferModuleFromTest(String normalized) {
        Matcher segment = Pattern.compile("/(api|ui|integration)/([^/]+)/").matcher(normalized);
        if (segment.find()) {
            return segment.group(2);
        }
        return "default";
    }

    private static String extractModule(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "default";
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        int slash = normalized.indexOf('/');
        return slash >= 0 ? normalized.substring(0, slash) : normalized;
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
        if (normalized.contains("import")) {
            return "imports";
        }
        int cut = normalized.indexOf("request");
        if (cut < 0) {
            cut = normalized.indexOf("response");
        }
        return cut > 0 ? normalized.substring(0, cut).replaceAll("-+$", "") : normalized;
    }
}
