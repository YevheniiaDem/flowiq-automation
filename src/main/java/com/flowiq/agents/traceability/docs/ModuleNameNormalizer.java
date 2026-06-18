package com.flowiq.agents.traceability.docs;

public final class ModuleNameNormalizer {

    private ModuleNameNormalizer() {
    }

    public static String toSlug(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "default";
        }
        return displayName.trim()
                .replace("AI Accountant", "ai-accountant")
                .replace("Business Guide", "business-guide")
                .toLowerCase()
                .replace(' ', '-');
    }

    public static String toDisplayName(String module) {
        if (module == null || module.isBlank()) {
            return "Unknown";
        }
        return switch (module) {
            case "ai-accountant" -> "AI Accountant";
            case "business-guide" -> "Business Guide";
            default -> {
                String[] parts = module.split("-");
                StringBuilder sb = new StringBuilder();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        if (!sb.isEmpty()) {
                            sb.append(' ');
                        }
                        sb.append(Character.toUpperCase(part.charAt(0)));
                        if (part.length() > 1) {
                            sb.append(part.substring(1));
                        }
                    }
                }
                yield sb.toString();
            }
        };
    }
}
