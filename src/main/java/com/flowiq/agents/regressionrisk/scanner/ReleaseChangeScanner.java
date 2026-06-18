package com.flowiq.agents.regressionrisk.scanner;

import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.regressionrisk.config.RegressionRiskAgentConfig;
import com.flowiq.agents.regressionrisk.model.ReleaseChangeContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ReleaseChangeScanner {

    private static final Pattern BACKEND_MODULE = Pattern.compile("/flowiq/([^/]+)/");
    private static final Pattern CONTROLLER = Pattern.compile("([A-Za-z0-9]+)Controller\\.java$");
    private static final Pattern SERVICE = Pattern.compile("([A-Za-z0-9]+)Service\\.java$");
    private static final Pattern PAGE = Pattern.compile("([A-Za-z0-9]+)Page\\.java$");
    private static final Pattern FRONTEND_PATH = Pattern.compile("(?i)(/frontend/|/ui/|\\.tsx$|\\.vue$|\\.jsx$)");

    private final RegressionRiskAgentConfig config;

    public ReleaseChangeScanner(RegressionRiskAgentConfig config) {
        this.config = config;
    }

    public ReleaseChangeContext scan(List<String> changedFiles, List<ApiChange> apiChanges) {
        Set<String> backendModules = new LinkedHashSet<>();
        Set<String> frontendModules = new LinkedHashSet<>();

        for (String file : changedFiles) {
            scanFile(file, backendModules, frontendModules);
        }

        Map<String, List<ApiChange>> apiByModule = groupApiChanges(apiChanges);
        apiByModule.keySet().forEach(backendModules::add);

        log.info("Release change scan: {} file(s), {} backend module(s), {} frontend module(s), {} API change(s)",
                changedFiles.size(), backendModules.size(), frontendModules.size(), apiChanges.size());

        return ReleaseChangeContext.builder()
                .changedFiles(changedFiles)
                .apiChanges(apiChanges)
                .backendModules(Set.copyOf(backendModules))
                .frontendModules(Set.copyOf(frontendModules))
                .build();
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

    private void scanFile(String file, Set<String> backendModules, Set<String> frontendModules) {
        String normalized = file.replace('\\', '/');
        Matcher backend = BACKEND_MODULE.matcher(normalized);
        if (backend.find() && isBackendPath(normalized)) {
            backendModules.add(backend.group(1));
        }
        Matcher controller = CONTROLLER.matcher(normalized);
        if (controller.find()) {
            backendModules.add(moduleFromName(controller.group(1)));
        }
        Matcher service = SERVICE.matcher(normalized);
        if (service.find()) {
            backendModules.add(moduleFromName(service.group(1)));
        }
        Matcher page = PAGE.matcher(normalized);
        if (page.find()) {
            frontendModules.add(moduleFromName(page.group(1)));
        }
        if (FRONTEND_PATH.matcher(normalized).find()) {
            String module = inferFrontendModule(normalized);
            if (module != null) {
                frontendModules.add(module);
            }
        }
    }

    private static boolean isBackendPath(String path) {
        return path.contains("/src/main/java/") && !path.contains("/pages/");
    }

    private static String inferFrontendModule(String path) {
        for (String segment : path.split("/")) {
            if (segment.endsWith("Page.java")) {
                return moduleFromName(segment.replace("Page.java", ""));
            }
        }
        int flowiq = path.indexOf("/flowiq/");
        if (flowiq >= 0) {
            String rest = path.substring(flowiq + "/flowiq/".length());
            int slash = rest.indexOf('/');
            if (slash > 0) {
                return rest.substring(0, slash);
            }
        }
        return null;
    }

    private static Map<String, List<ApiChange>> groupApiChanges(List<ApiChange> apiChanges) {
        Map<String, List<ApiChange>> grouped = new LinkedHashMap<>();
        for (ApiChange change : apiChanges) {
            if (change.getType() == ChangeType.BREAKING_CHANGE) {
                continue;
            }
            String module = extractModule(change);
            grouped.computeIfAbsent(module, k -> new ArrayList<>()).add(change);
        }
        return grouped;
    }

    private static String extractModule(ApiChange change) {
        if (change.getPath() != null && !change.getPath().isBlank()) {
            return moduleFromPath(change.getPath());
        }
        if (change.getSchema() != null) {
            return moduleFromSchema(change.getSchema());
        }
        return "default";
    }

    private static String moduleFromPath(String path) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        int slash = normalized.indexOf('/');
        return slash >= 0 ? normalized.substring(0, slash) : normalized;
    }

    private static String moduleFromSchema(String schema) {
        String normalized = schema.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
        if (normalized.contains("task")) {
            return "tasks";
        }
        if (normalized.contains("auth")) {
            return "auth";
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

    private static String moduleFromName(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
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
}
