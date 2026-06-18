package com.flowiq.agents.gap.scanner;

import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.constants.ApiEndpoints;
import lombok.extern.slf4j.Slf4j;

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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class TestSourceScanner {

    private static final Pattern HTTP_PATH = Pattern.compile(
            "(GET|POST|PUT|PATCH|DELETE)\\s+(/[\\w\\-./{}]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern API_ENDPOINT_CONST = Pattern.compile("ApiEndpoints\\.([A-Z0-9_]+)");
    private static final Pattern NEGATIVE_HINT = Pattern.compile(
            "(?i)(unauthorized|invalid|not\\s*found|validation|attempt|forbidden|401|404|422|bad\\s*request)");
    private static final Pattern AUTH_HINT = Pattern.compile(
            "(?i)(unauthorized|authorization|unauthenticated|without\\s*token|401)");

    private final Path testSourceRoot;
    private final Map<String, String> endpointConstants;

    public TestSourceScanner(TestGapAgentConfig config) {
        this.testSourceRoot = resolveTestSourceRoot(config.testSourceDirectory());
        this.endpointConstants = loadApiEndpointConstants();
    }

    public List<ScannedTestReference> scan() {
        List<ScannedTestReference> references = new ArrayList<>();
        if (!Files.exists(testSourceRoot)) {
            log.warn("Test source directory not found: {}", testSourceRoot);
            return references;
        }

        try (Stream<Path> files = Files.walk(testSourceRoot)) {
            files.filter(path -> path.toString().endsWith("Test.java") || path.toString().endsWith("IT.java"))
                    .forEach(path -> references.addAll(parseFile(path)));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan test sources at " + testSourceRoot, e);
        }

        log.info("Scanned {} test references from {}", references.size(), testSourceRoot);
        return references;
    }

    private List<ScannedTestReference> parseFile(Path file) {
        String source;
        try {
            source = Files.readString(file);
        } catch (IOException e) {
            log.warn("Skipping unreadable test file {}: {}", file, e.getMessage());
            return List.of();
        }

        String className = file.getFileName().toString().replace(".java", "");
        Set<TestSuiteType> suites = TestSuiteClassifier.classify(className, source);
        if (suites.isEmpty()) {
            return List.of();
        }

        String module = inferModule(file, className, source);
        boolean negativeFile = NEGATIVE_HINT.matcher(source).find();
        boolean authFile = AUTH_HINT.matcher(source).find();

        Set<String> keys = new LinkedHashSet<>();
        List<ScannedTestReference> references = new ArrayList<>();

        Matcher httpMatcher = HTTP_PATH.matcher(source);
        while (httpMatcher.find()) {
            keys.add(httpMatcher.group(1).toUpperCase() + " " + normalizePath(httpMatcher.group(2)));
        }

        Matcher constMatcher = API_ENDPOINT_CONST.matcher(source);
        while (constMatcher.find()) {
            String path = endpointConstants.get(constMatcher.group(1));
            if (path != null) {
                keys.add("* " + normalizePath(path));
            }
        }

        for (String key : keys) {
            String method;
            String path;
            if (key.startsWith("* ")) {
                method = "*";
                path = key.substring(2);
            } else {
                int space = key.indexOf(' ');
                method = key.substring(0, space);
                path = key.substring(space + 1);
            }
            references.add(ScannedTestReference.builder()
                    .className(className)
                    .module(module)
                    .suites(suites)
                    .method(method)
                    .path(path)
                    .negativeScenario(negativeFile)
                    .authorizationCheck(authFile)
                    .sourceHint(file.toString())
                    .build());
        }

        if (references.isEmpty()) {
            references.add(ScannedTestReference.builder()
                    .className(className)
                    .module(module)
                    .suites(suites)
                    .method("*")
                    .path("/" + module)
                    .negativeScenario(negativeFile)
                    .authorizationCheck(authFile)
                    .sourceHint(file.toString())
                    .build());
        }

        return references;
    }

    private static String inferModule(Path file, String className, String source) {
        String path = file.toString().replace('\\', '/');
        for (String segment : List.of("auth", "transactions", "tasks", "reports", "forecasts",
                "analytics", "notifications", "imports", "dashboard", "businessguide",
                "business-guide", "aiaccountant", "ai-accountant")) {
            if (path.contains("/" + segment + "/")) {
                return segment.replace("businessguide", "business-guide")
                        .replace("aiaccountant", "ai-accountant");
            }
        }
        Matcher groups = Pattern.compile("groups\\s*=\\s*\\{[^}]*\"([a-z-]+)\"").matcher(source);
        if (groups.find()) {
            return groups.group(1);
        }
        return className.replaceAll("(Contract|Smoke|Regression|Ui|Api|Test).*", "")
                .replaceAll("(.)([A-Z])", "$1-$2")
                .toLowerCase();
    }

    public static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.trim();
        if (normalized.startsWith("/api/")) {
            normalized = normalized.substring(4);
        } else if (normalized.equals("/api")) {
            normalized = "/";
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

    private Path resolveTestSourceRoot(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
