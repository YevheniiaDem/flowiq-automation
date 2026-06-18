package com.flowiq.agents.architecture.inventory;

import java.util.Locale;

public final class EndpointNormalizer {

    private EndpointNormalizer() {
    }

    public static String normalize(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.trim();
        if (normalized.startsWith("`")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("`")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.startsWith("/api/")) {
            normalized = normalized.substring(4);
        } else if (normalized.equals("/api")) {
            normalized = "/";
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    public static String moduleFromPath(String path) {
        String normalized = normalize(path);
        if ("/".equals(normalized)) {
            return "default";
        }
        String withoutSlash = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        int slash = withoutSlash.indexOf('/');
        return slash >= 0 ? withoutSlash.substring(0, slash) : withoutSlash;
    }
}
