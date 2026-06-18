package com.flowiq.agents.architecture.inventory;

import com.flowiq.agents.architecture.config.ArchitectureDriftAgentConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
class TestInventoryLoader {

    private final ArchitectureDriftAgentConfig config;

    TestInventoryLoader(ArchitectureDriftAgentConfig config) {
        this.config = config;
    }

    TestInventory load() {
        Path root = resolvePath(config.testSourceDirectory());
        Set<String> contract = new LinkedHashSet<>();
        Set<String> regression = new LinkedHashSet<>();
        Set<String> smoke = new LinkedHashSet<>();
        Set<String> ui = new LinkedHashSet<>();
        if (!Files.isDirectory(root)) {
            return new TestInventory(contract, regression, smoke, ui);
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("Test.java"))
                    .forEach(path -> classify(path.getFileName().toString(), contract, regression, smoke, ui));
        } catch (IOException e) {
            log.warn("Failed to scan tests under {}: {}", root, e.getMessage());
        }
        return new TestInventory(Set.copyOf(contract), Set.copyOf(regression), Set.copyOf(smoke), Set.copyOf(ui));
    }

    private static void classify(String fileName,
                                 Set<String> contract,
                                 Set<String> regression,
                                 Set<String> smoke,
                                 Set<String> ui) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        String className = fileName.replace(".java", "");
        if (lower.contains("contract")) {
            contract.add(className);
        }
        if (lower.contains("regression")) {
            regression.add(className);
        }
        if (lower.contains("smoke") && !lower.contains("regression")) {
            smoke.add(className);
        }
        if (lower.contains("uismoke") || (lower.contains("ui") && lower.contains("smoke"))) {
            ui.add(className);
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
