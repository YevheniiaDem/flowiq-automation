package com.flowiq.agents.generator.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.generator.config.SmartTestGeneratorConfig;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class JsonSchemaIndex {

    private final Path schemaRoot;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonSchemaDocument> byModuleResource = new LinkedHashMap<>();

    public JsonSchemaIndex(SmartTestGeneratorConfig config, ObjectMapper objectMapper) {
        this.schemaRoot = resolvePath(config.schemaDirectory());
        this.objectMapper = objectMapper;
        index();
    }

    public List<JsonSchemaDocument> all() {
        return List.copyOf(byModuleResource.values());
    }

    public Optional<JsonSchemaDocument> findForEndpoint(String path, String method) {
        String module = extractModule(path);
        String resource = resourceFromPath(path, method);
        String key = module + "/" + resource;
        if (byModuleResource.containsKey(key)) {
            return Optional.of(byModuleResource.get(key));
        }
        return byModuleResource.values().stream()
                .filter(doc -> module.equals(doc.getModuleHint())
                        && (doc.getResourceHint() == null || path.contains(doc.getResourceHint())))
                .findFirst();
    }

    private void index() {
        if (!Files.isDirectory(schemaRoot)) {
            log.warn("Schema directory not found: {}", schemaRoot);
            return;
        }
        try (Stream<Path> files = Files.walk(schemaRoot)) {
            files.filter(p -> p.toString().endsWith(".json"))
                    .forEach(this::parseSchema);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to index schemas at " + schemaRoot, e);
        }
        log.info("Indexed {} JSON schema(s) from {}", byModuleResource.size(), schemaRoot);
    }

    private void parseSchema(Path file) {
        try {
            JsonNode root = objectMapper.readTree(file.toFile());
            String relative = schemaRoot.relativize(file).toString().replace('\\', '/');
            String moduleHint = relative.contains("/") ? relative.substring(0, relative.indexOf('/')) : "root";
            String resourceHint = fileNameToResourceHint(file.getFileName().toString());

            List<SchemaFieldConstraint> fields = new ArrayList<>();
            List<String> required = new ArrayList<>();
            collectFields(root, "", fields, required);

            JsonSchemaDocument doc = JsonSchemaDocument.builder()
                    .filePath(relative)
                    .title(root.path("title").asText(file.getFileName().toString()))
                    .moduleHint(moduleHint.replace("businessguide", "business-guide"))
                    .resourceHint(resourceHint)
                    .fields(fields)
                    .requiredFields(required)
                    .build();

            byModuleResource.put(moduleHint + "/" + resourceHint, doc);
        } catch (IOException e) {
            log.warn("Skipping schema {}: {}", file, e.getMessage());
        }
    }

    private void collectFields(JsonNode node, String prefix, List<SchemaFieldConstraint> fields,
                               List<String> required) {
        if (node == null) {
            return;
        }
        if (node.has("required")) {
            node.get("required").forEach(r -> required.add(prefix.isBlank() ? r.asText() : prefix + "." + r.asText()));
        }
        if (node.has("properties")) {
            Iterator<String> names = node.get("properties").fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                JsonNode prop = node.get("properties").get(name);
                String fieldPath = prefix.isBlank() ? name : prefix + "." + name;
                fields.add(SchemaFieldConstraint.builder()
                        .field(fieldPath)
                        .type(propType(prop))
                        .required(required.contains(fieldPath) || required.contains(name))
                        .minLength(prop.has("minLength") ? prop.get("minLength").asInt() : null)
                        .maxLength(prop.has("maxLength") ? prop.get("maxLength").asInt() : null)
                        .minimum(prop.has("minimum") ? prop.get("minimum").asInt() : null)
                        .maximum(prop.has("maximum") ? prop.get("maximum").asInt() : null)
                        .format(prop.has("format") ? prop.get("format").asText() : null)
                        .enumValues(enumValues(prop))
                        .build());
            }
        }
        if (node.has("definitions")) {
            Iterator<String> defs = node.get("definitions").fieldNames();
            while (defs.hasNext()) {
                String defName = defs.next();
                collectFields(node.get("definitions").get(defName), prefix, fields, required);
            }
        }
    }

    private static List<String> enumValues(JsonNode prop) {
        if (!prop.has("enum")) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        prop.get("enum").forEach(v -> values.add(v.asText()));
        return values;
    }

    private static String propType(JsonNode prop) {
        if (prop.has("type")) {
            JsonNode type = prop.get("type");
            return type.isArray() ? type.get(0).asText() : type.asText();
        }
        return "object";
    }

    private static String fileNameToResourceHint(String fileName) {
        return fileName
                .replace(".schema.json", "")
                .replace("-response", "")
                .replace("-request", "")
                .replace("_", "-");
    }

    private static String resourceFromPath(String path, String method) {
        String normalized = TestSourceScanner.normalizePath(path);
        if (normalized.endsWith("/summary")) return "summary";
        if (normalized.contains("/grouped")) return "grouped";
        if (normalized.contains("/preview")) return "preview";
        if (normalized.contains("/articles")) return "articles-page";
        if (normalized.contains("/health")) return "health";
        if ("POST".equalsIgnoreCase(method) && normalized.contains("/login")) return "login";
        if ("POST".equalsIgnoreCase(method) && normalized.contains("/register")) return "register";
        if (normalized.contains("/overview")) return "overview";
        return "page";
    }

    private static String extractModule(String path) {
        String normalized = TestSourceScanner.normalizePath(path);
        if (normalized.equals("/") || normalized.isBlank()) return "default";
        String segment = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        int slash = segment.indexOf('/');
        return slash >= 0 ? segment.substring(0, slash) : segment;
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
