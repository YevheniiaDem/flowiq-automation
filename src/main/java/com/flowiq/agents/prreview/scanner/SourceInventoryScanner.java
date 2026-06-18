package com.flowiq.agents.prreview.scanner;

import com.flowiq.agents.prreview.config.PullRequestReviewAgentConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class SourceInventoryScanner {

    private static final Pattern SERVICE = Pattern.compile("([A-Za-z0-9]+)Service\\.java$");
    private static final Pattern CONTROLLER = Pattern.compile("([A-Za-z0-9]+)Controller\\.java$");
    private static final Pattern REPOSITORY = Pattern.compile("([A-Za-z0-9]+)Repository\\.java$");
    private static final Pattern DTO = Pattern.compile("([A-Za-z0-9]+)(Dto|Request|Response)\\.java$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PAGE = Pattern.compile("([A-Za-z0-9]+)Page\\.java$");
    private static final Pattern CLASS_DECL = Pattern.compile("(?:public\\s+)?class\\s+([A-Za-z0-9_]+)");

    private final PullRequestReviewAgentConfig config;

    public SourceInventoryScanner(PullRequestReviewAgentConfig config) {
        this.config = config;
    }

    public SourceInventory scan() {
        SourceInventory.SourceInventoryBuilder builder = SourceInventory.builder();
        walkJava(resolvePath(config.mainSourceDirectory()), builder, false);
        walkJava(resolvePath(config.pageObjectDirectory()), builder, true);
        walkJava(resolvePath(config.testSourceDirectory()), builder, false, true);
        walkSchemas(resolvePath(config.schemaDirectory()), builder);
        SourceInventory inventory = builder.build();
        log.info("Source inventory: {} services, {} pages, {} schemas, {} tests",
                inventory.getServiceFiles().size(),
                inventory.getPageObjectFiles().size(),
                inventory.getSchemaFiles().size(),
                inventory.getTestFiles().size());
        return inventory;
    }

    private void walkJava(Path root, SourceInventory.SourceInventoryBuilder builder, boolean pagesOnly) {
        walkJava(root, builder, pagesOnly, false);
    }

    private void walkJava(Path root,
                          SourceInventory.SourceInventoryBuilder builder,
                          boolean pagesOnly,
                          boolean testsOnly) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> classifyJava(path, builder, pagesOnly, testsOnly));
        } catch (IOException e) {
            log.warn("Failed to walk {}: {}", root, e.getMessage());
        }
    }

    private void classifyJava(Path path,
                              SourceInventory.SourceInventoryBuilder builder,
                              boolean pagesOnly,
                              boolean testsOnly) {
        String normalized = normalize(path);
        if (testsOnly || normalized.contains("/test/") || normalized.endsWith("Test.java")
                || normalized.endsWith("IT.java")) {
            builder.testFile(normalized);
            return;
        }
        if (pagesOnly) {
            registerPage(path, normalized, builder);
            return;
        }

        Matcher service = SERVICE.matcher(normalized);
        if (service.find()) {
            builder.serviceFile(normalized);
            builder.serviceClass(service.group(1) + "Service");
            return;
        }
        Matcher controller = CONTROLLER.matcher(normalized);
        if (controller.find()) {
            builder.controllerFile(normalized);
            return;
        }
        Matcher repository = REPOSITORY.matcher(normalized);
        if (repository.find()) {
            builder.repositoryFile(normalized);
            return;
        }
        Matcher dto = DTO.matcher(normalized);
        if (dto.find()) {
            builder.dtoFile(normalized);
            builder.dtoClass(dto.group(1) + dto.group(2));
            return;
        }
        Matcher page = PAGE.matcher(normalized);
        if (page.find()) {
            registerPage(path, normalized, builder);
        }
    }

    private void registerPage(Path path, String normalized, SourceInventory.SourceInventoryBuilder builder) {
        builder.pageObjectFile(normalized);
        readClassName(path).ifPresent(builder::pageClass);
    }

    private void walkSchemas(Path root, SourceInventory.SourceInventoryBuilder builder) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> builder.schemaFile(normalize(path)));
        } catch (IOException e) {
            log.warn("Failed to walk schemas {}: {}", root, e.getMessage());
        }
    }

    public String readFileContent(String relativePath) {
        Path path = resolvePath(relativePath);
        if (!Files.isRegularFile(path)) {
            return "";
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "";
        }
    }

    public static boolean schemaExistsForDto(String dtoName, SourceInventory inventory) {
        String normalized = dtoName.toLowerCase(Locale.ROOT).replaceAll("([a-z])([A-Z])", "$1-$2");
        normalized = normalized.replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2").toLowerCase(Locale.ROOT);
        String compact = dtoName.toLowerCase(Locale.ROOT);
        for (String schema : inventory.getSchemaFiles()) {
            String file = schema.toLowerCase(Locale.ROOT);
            if (file.contains(compact) || file.contains(normalized.replace('_', '-'))) {
                return true;
            }
        }
        return false;
    }

    public static boolean schemaChangedForDto(String dtoName, List<String> changedFiles) {
        String compact = dtoName.toLowerCase(Locale.ROOT);
        return changedFiles.stream().anyMatch(file -> {
            String lower = file.toLowerCase(Locale.ROOT);
            return lower.contains("schema") && (lower.contains(compact) || lower.contains(kebab(dtoName)));
        });
    }

    public static Set<String> testsReferencingClass(String className, SourceInventory inventory,
                                                    SourceInventoryScanner scanner) {
        Set<String> matches = new LinkedHashSet<>();
        for (String testFile : inventory.getTestFiles()) {
            String content = scanner.readFileContent(testFile);
            if (content.contains(className)) {
                matches.add(testFile);
            }
        }
        return matches;
    }

    public static String kebab(String value) {
        return value.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    private static java.util.Optional<String> readClassName(Path path) {
        try {
            Matcher matcher = CLASS_DECL.matcher(Files.readString(path));
            if (matcher.find()) {
                return java.util.Optional.of(matcher.group(1));
            }
        } catch (IOException ignored) {
            // fall through
        }
        return java.util.Optional.empty();
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }

    private Path resolvePath(String location) {
        Path path = Paths.get(location);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
