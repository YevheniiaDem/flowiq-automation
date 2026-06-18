package com.flowiq.agents.maintenance.scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.flaky.loader.AllureResultsLoader;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.maintenance.config.TestMaintenanceAgentConfig;
import com.flowiq.agents.maintenance.model.ScannedDto;
import com.flowiq.agents.maintenance.model.ScannedPageObject;
import com.flowiq.agents.maintenance.model.ScannedSchema;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.openapi.OpenApiOperation;
import com.flowiq.constants.ApiEndpoints;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class MaintenanceInventoryScanner {

    private static final Pattern HTTP_PATH = Pattern.compile(
            "(GET|POST|PUT|PATCH|DELETE)\\s+(/[\\w\\-./{}]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern API_ENDPOINT_CONST = Pattern.compile("ApiEndpoints\\.([A-Z0-9_]+)");
    private static final Pattern ASSERTION = Pattern.compile(
            "assertThat\\([^;]+\\)\\.[^;]+;");
    private static final Pattern METHOD_DECL = Pattern.compile(
            "(?:public|protected|private)\\s+[\\w<>,\\[\\]\\s]+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{");
    private static final Pattern CLASS_DECL = Pattern.compile("class\\s+([A-Za-z0-9_]+)");
    private static final Pattern DTO_FILE = Pattern.compile(
            "([A-Za-z0-9]+)(Dto|Request|Response)\\.java$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAGE_FILE = Pattern.compile("([A-Za-z0-9]+)Page\\.java$");

    private final TestMaintenanceAgentConfig config;
    private final ObjectMapper objectMapper;
    private final Map<String, String> endpointConstants;

    public MaintenanceInventoryScanner(TestMaintenanceAgentConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.endpointConstants = loadApiEndpointConstants();
    }

    public MaintenanceContext scan() {
        List<ScannedTestClass> tests = scanTestClasses();
        List<ScannedPageObject> pages = scanPageObjects();
        List<ScannedDto> dtos = scanDtos();
        List<ScannedSchema> schemas = scanSchemas();
        Set<String> openApiEndpoints = loadOpenApiEndpoints();
        List<TestExecutionRecord> allureRecords = loadAllureRecords();
        String combinedSources = buildCombinedSources(tests, pages, dtos);

        return MaintenanceContext.builder()
                .testClasses(tests)
                .pageObjects(pages)
                .dtos(dtos)
                .schemas(schemas)
                .openApiEndpoints(openApiEndpoints)
                .allureRecords(allureRecords)
                .combinedMainAndTestSources(combinedSources)
                .dataSourcesSummary(summarizeSources(tests, pages, dtos, schemas, openApiEndpoints, allureRecords))
                .build();
    }

    public String summarizeSources(List<ScannedTestClass> tests,
                                   List<ScannedPageObject> pages,
                                   List<ScannedDto> dtos,
                                   List<ScannedSchema> schemas,
                                   Set<String> endpoints,
                                   List<TestExecutionRecord> allure) {
        return "Test classes: " + tests.size()
                + "; Page objects: " + pages.size()
                + "; DTOs: " + dtos.size()
                + "; Schemas: " + schemas.size()
                + "; OpenAPI endpoints: " + endpoints.size()
                + "; Allure records: " + allure.size();
    }

    private List<ScannedTestClass> scanTestClasses() {
        List<ScannedTestClass> result = new ArrayList<>();
        Path root = resolvePath(config.testSourceDirectory());
        walkJava(root, path -> {
            if (!isTestFile(path)) {
                return;
            }
            String source = readString(path);
            String className = extractClassName(source, path);
            result.add(ScannedTestClass.builder()
                    .className(className)
                    .filePath(normalize(path))
                    .source(source)
                    .lineCount(countLines(source))
                    .methodCount(countMethods(source))
                    .maxMethodLines(maxMethodLength(source))
                    .endpointKeys(extractEndpointKeys(source))
                    .assertions(extractAssertions(source))
                    .build());
        });
        return result;
    }

    private List<ScannedPageObject> scanPageObjects() {
        List<ScannedPageObject> result = new ArrayList<>();
        Path root = resolvePath(config.pageObjectDirectory());
        walkJava(root, path -> {
            String source = readString(path);
            Matcher page = PAGE_FILE.matcher(path.getFileName().toString());
            if (!page.find() && !source.contains("extends BasePage") && !source.contains("extends AbstractPage")) {
                return;
            }
            String className = extractClassName(source, path);
            if (className.equals("BasePage") || className.equals("AbstractPage") || className.equals("Pages")) {
                return;
            }
            result.add(ScannedPageObject.builder()
                    .className(className)
                    .filePath(normalize(path))
                    .source(source)
                    .lineCount(countLines(source))
                    .publicMethodCount(countPublicMethods(source))
                    .build());
        });
        return result;
    }

    private List<ScannedDto> scanDtos() {
        List<ScannedDto> result = new ArrayList<>();
        Path root = resolvePath(config.dtoSourceDirectory());
        walkJava(root, path -> {
            Matcher dto = DTO_FILE.matcher(path.getFileName().toString());
            if (!dto.find()) {
                return;
            }
            result.add(ScannedDto.builder()
                    .className(path.getFileName().toString().replace(".java", ""))
                    .filePath(normalize(path))
                    .build());
        });
        return result;
    }

    private List<ScannedSchema> scanSchemas() {
        List<ScannedSchema> result = new ArrayList<>();
        Path root = resolvePath(config.schemaDirectory());
        if (!Files.isDirectory(root)) {
            return result;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> result.add(ScannedSchema.builder()
                            .fileName(path.getFileName().toString())
                            .filePath(normalize(path))
                            .build()));
        } catch (IOException e) {
            log.warn("Failed to scan schemas: {}", e.getMessage());
        }
        return result;
    }

    private Set<String> loadOpenApiEndpoints() {
        Set<String> endpoints = new LinkedHashSet<>();
        try {
            JsonNode spec = loadOpenApiSpec();
            for (OpenApiOperation operation : OpenApiNavigator.getOperations(spec)) {
                endpoints.add(operation.method() + " " + normalizeEndpointPath(operation.path()));
            }
        } catch (Exception e) {
            log.warn("OpenAPI endpoint inventory skipped: {}", e.getMessage());
        }
        return endpoints;
    }

    private JsonNode loadOpenApiSpec() throws IOException {
        String snapshot = config.openApiSnapshot();
        if (snapshot != null && !snapshot.isBlank()) {
            Path path = Paths.get(snapshot);
            if (!path.isAbsolute()) {
                path = Paths.get(System.getProperty("user.dir")).resolve(path);
            }
            return objectMapper.readTree(Files.readString(path));
        }
        AgentConfig agentConfig = ConfigFactory.create(AgentConfig.class);
        return new OpenApiFetcher(agentConfig, objectMapper).fetchCurrentSpec();
    }

    private List<TestExecutionRecord> loadAllureRecords() {
        List<TestExecutionRecord> records = new ArrayList<>();
        for (String directory : config.allureResultsDirectories().split(",")) {
            String trimmed = directory.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            Path path = resolvePath(trimmed);
            records.addAll(new AllureResultsLoader(trimmed, path, objectMapper).load());
        }
        return records;
    }

    private static String buildCombinedSources(List<ScannedTestClass> tests,
                                               List<ScannedPageObject> pages,
                                               List<ScannedDto> dtos) {
        StringBuilder combined = new StringBuilder();
        Path mainRoot = Paths.get(System.getProperty("user.dir")).resolve("src/main/java");
        if (Files.isDirectory(mainRoot)) {
            try (Stream<Path> files = Files.walk(mainRoot)) {
                files.filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> combined.append(readString(p)).append('\n'));
            } catch (IOException ignored) {
                // skip
            }
        }
        tests.forEach(t -> combined.append(t.getSource()).append('\n'));
        pages.forEach(p -> combined.append(p.getSource()).append('\n'));
        dtos.forEach(d -> combined.append(d.getClassName()).append('\n'));
        return combined.toString();
    }

    private Set<String> extractEndpointKeys(String source) {
        Set<String> keys = new LinkedHashSet<>();
        Matcher httpMatcher = HTTP_PATH.matcher(source);
        while (httpMatcher.find()) {
            keys.add(httpMatcher.group(1).toUpperCase() + " " + normalizeEndpointPath(httpMatcher.group(2)));
        }
        Matcher constMatcher = API_ENDPOINT_CONST.matcher(source);
        while (constMatcher.find()) {
            String path = endpointConstants.get(constMatcher.group(1));
            if (path != null) {
                keys.add("* " + normalizeEndpointPath(path));
            }
        }
        return keys;
    }

    private static List<String> extractAssertions(String source) {
        List<String> assertions = new ArrayList<>();
        Matcher matcher = ASSERTION.matcher(source);
        while (matcher.find()) {
            String normalized = matcher.group().replaceAll("\\s+", " ").trim();
            assertions.add(normalized);
        }
        return assertions;
    }

    private static int countMethods(String source) {
        int count = 0;
        Matcher matcher = METHOD_DECL.matcher(source);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static int countPublicMethods(String source) {
        Pattern publicMethod = Pattern.compile("public\\s+[\\w<>,\\[\\]\\s]+\\s+\\w+\\s*\\(");
        int count = 0;
        Matcher matcher = publicMethod.matcher(source);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static int maxMethodLength(String source) {
        String[] lines = source.split("\n");
        int max = 0;
        int current = 0;
        int depth = 0;
        for (String line : lines) {
            if (line.contains("{")) {
                depth++;
            }
            if (depth > 0) {
                current++;
            }
            if (line.contains("}")) {
                depth = Math.max(0, depth - 1);
                if (depth == 0) {
                    max = Math.max(max, current);
                    current = 0;
                }
            }
        }
        return max;
    }

    private static int countLines(String source) {
        return source.isBlank() ? 0 : source.split("\n").length;
    }

    private static String extractClassName(String source, Path path) {
        Matcher matcher = CLASS_DECL.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return path.getFileName().toString().replace(".java", "");
    }

    private static String normalizeEndpointPath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.trim();
        if (normalized.startsWith("/api/")) {
            normalized = normalized.substring(4);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.replaceAll("/+$", "");
    }

    private Map<String, String> loadApiEndpointConstants() {
        Map<String, String> constants = new LinkedHashMap<>();
        for (Field field : ApiEndpoints.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                try {
                    constants.put(field.getName(), (String) field.get(null));
                } catch (IllegalAccessException ignored) {
                    // skip
                }
            }
        }
        return constants;
    }

    private void walkJava(Path root, java.util.function.Consumer<Path> consumer) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(consumer);
        } catch (IOException e) {
            log.warn("Failed to walk {}: {}", root, e.getMessage());
        }
    }

    private static boolean isTestFile(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith("Test.java") || name.endsWith("IT.java");
    }

    private static String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "";
        }
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
