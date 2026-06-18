package com.flowiq.agents.architecture.inventory;

import com.flowiq.agents.architecture.config.ArchitectureDriftAgentConfig;
import com.flowiq.agents.traceability.docs.ModuleNameNormalizer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
class DocumentationInventoryLoader {

    private static final Pattern CONTRACT_ROW = Pattern.compile(
            "\\|\\s*([A-Za-z][A-Za-z0-9 \\-]+?)\\s*\\|\\s*(GET|POST|PUT|PATCH|DELETE)\\s*\\|\\s*(`?)(/[/\\w\\-{}]+)\\3",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern INLINE_ENDPOINT = Pattern.compile(
            "(GET|POST|PUT|PATCH|DELETE)\\s+(`?)(/[/\\w\\-{}]+)\\2",
            Pattern.CASE_INSENSITIVE);

    private final ArchitectureDriftAgentConfig config;

    DocumentationInventoryLoader(ArchitectureDriftAgentConfig config) {
        this.config = config;
    }

    DocumentationInventory load() {
        List<ApiEndpointRef> endpoints = new ArrayList<>();
        Set<String> modules = new LinkedHashSet<>();
        loadDirectory(resolvePath(config.docsDirectory()), endpoints, modules);
        loadDirectory(resolvePath(config.adrDirectory()), endpoints, modules);
        log.info("Indexed {} documented endpoint(s) from docs/ADR", endpoints.size());
        return new DocumentationInventory(List.copyOf(endpoints), Set.copyOf(modules));
    }

    private void loadDirectory(Path root, List<ApiEndpointRef> endpoints, Set<String> modules) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                    .forEach(path -> parseMarkdown(path, endpoints, modules));
        } catch (IOException e) {
            log.warn("Failed to walk documentation directory {}: {}", root, e.getMessage());
        }
    }

    private void parseMarkdown(Path file, List<ApiEndpointRef> endpoints, Set<String> modules) {
        try {
            String content = Files.readString(file);
            String source = file.getFileName().toString();
            Matcher contractMatcher = CONTRACT_ROW.matcher(content);
            while (contractMatcher.find()) {
                modules.add(ModuleNameNormalizer.toSlug(contractMatcher.group(1).trim()));
                endpoints.add(new ApiEndpointRef(
                        contractMatcher.group(2),
                        contractMatcher.group(4),
                        source));
            }
            Matcher inlineMatcher = INLINE_ENDPOINT.matcher(content);
            while (inlineMatcher.find()) {
                String path = inlineMatcher.group(3);
                endpoints.add(new ApiEndpointRef(inlineMatcher.group(1), path, source));
                modules.add(EndpointNormalizer.moduleFromPath(path));
            }
            extractModuleRows(content, modules, source);
        } catch (IOException e) {
            log.debug("Skipping doc file {}: {}", file, e.getMessage());
        }
    }

    private static void extractModuleRows(String content, Set<String> modules, String source) {
        Pattern moduleRow = Pattern.compile(
                "\\|\\s*([A-Za-z][A-Za-z0-9 \\-]+?)\\s*\\|\\s*`?[A-Za-z]+(?:Regression|Contract|Smoke)?Test`?");
        Matcher matcher = moduleRow.matcher(content);
        while (matcher.find()) {
            String name = matcher.group(1).trim();
            if ("Module".equalsIgnoreCase(name) || "Domain".equalsIgnoreCase(name) || "Total".equalsIgnoreCase(name)) {
                continue;
            }
            modules.add(ModuleNameNormalizer.toSlug(name));
        }
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
