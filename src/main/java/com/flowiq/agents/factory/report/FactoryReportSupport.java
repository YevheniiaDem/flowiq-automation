package com.flowiq.agents.factory.report;

import java.nio.file.Path;
import java.nio.file.Paths;

final class FactoryReportSupport {

    private FactoryReportSupport() {
    }

    static Path resolveReportPath(String configuredPath) {
        Path path = Paths.get(configuredPath);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }

    static String formatLabel(String name) {
        return name.replace('_', ' ');
    }
}
