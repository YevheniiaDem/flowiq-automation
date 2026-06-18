package com.flowiq.agents.architecture.inventory;

import com.flowiq.agents.architecture.config.ArchitectureDriftAgentConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
class SourceCodeInventoryLoader {

    private static final Pattern DTO_NAME = Pattern.compile(
            "(Request|Response|Dto)$", Pattern.CASE_INSENSITIVE);

    private final ArchitectureDriftAgentConfig config;

    SourceCodeInventoryLoader(ArchitectureDriftAgentConfig config) {
        this.config = config;
    }

    SourceCodeInventory load() {
        Path backendRoot = resolvePath(config.backendSourceDirectory());
        Path frontendRoot = resolvePath(config.frontendSourceDirectory());
        Path dtoRoot = resolvePath(config.dtoSourceDirectory());
        Path schemaRoot = resolvePath(config.schemaDirectory());

        List<SourceArtifact> services = scanArtifacts(backendRoot, "Service.java", "Service", true);
        List<SourceArtifact> controllers = scanControllers(resolvePath("src/main/java"), "Controller.java");
        List<SourceArtifact> pages = scanArtifacts(frontendRoot, "Page.java", "Page", false);
        List<SourceArtifact> dtos = scanDtos(dtoRoot);
        List<Path> schemas = listSchemas(schemaRoot);

        return new SourceCodeInventory(services, controllers, pages, dtos, schemas);
    }

    private List<SourceArtifact> scanControllers(Path root, String suffix) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        List<SourceArtifact> controllers = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    .forEach(path -> {
                        String name = path.getFileName().toString().replace(".java", "");
                        controllers.add(new SourceArtifact(
                                name,
                                moduleFromClassName(name.replace("Controller", "")),
                                relativize(path)));
                    });
        } catch (IOException e) {
            log.warn("Failed to scan controllers under {}: {}", root, e.getMessage());
        }
        return controllers;
    }

    private List<SourceArtifact> scanArtifacts(Path root, String suffix, String stripSuffix, boolean skipBase) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        List<SourceArtifact> artifacts = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String name = fileName.replace(".java", "");
                        if (skipBase && (name.startsWith("Base") || name.equals("Abstract" + stripSuffix))) {
                            return;
                        }
                        if (!skipBase && (name.startsWith("Base") || name.equals("Abstract" + stripSuffix)
                                || name.equals("Pages"))) {
                            return;
                        }
                        String module = moduleFromClassName(name.replace(stripSuffix, ""));
                        artifacts.add(new SourceArtifact(name, module, relativize(path)));
                    });
        } catch (IOException e) {
            log.warn("Failed to scan artifacts under {}: {}", root, e.getMessage());
        }
        return artifacts;
    }

    private List<SourceArtifact> scanDtos(Path root) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        List<SourceArtifact> dtos = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .forEach(path -> {
                        String name = path.getFileName().toString().replace(".java", "");
                        if (!DTO_NAME.matcher(name).find()) {
                            return;
                        }
                        dtos.add(new SourceArtifact(name, moduleFromDtoPath(path), relativize(path)));
                    });
        } catch (IOException e) {
            log.warn("Failed to scan DTOs under {}: {}", root, e.getMessage());
        }
        return dtos;
    }

    private List<Path> listSchemas(Path root) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".json");
                    })
                    .toList();
        } catch (IOException e) {
            log.warn("Failed to list schemas under {}: {}", root, e.getMessage());
            return List.of();
        }
    }

    private static String moduleFromDtoPath(Path path) {
        String normalized = path.toString().replace('\\', '/');
        for (String segment : List.of("/models/tasks/", "/models/notifications/", "/models/forecasts/",
                "/models/knowledge/", "/models/request/", "/models/response/")) {
            if (normalized.contains(segment)) {
                return segment.replace("/models/", "").replace("/", "");
            }
        }
        return "shared";
    }

    private static String moduleFromClassName(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    private static String relativize(Path path) {
        return Paths.get(System.getProperty("user.dir")).relativize(path.toAbsolutePath().normalize()).toString()
                .replace('\\', '/');
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
