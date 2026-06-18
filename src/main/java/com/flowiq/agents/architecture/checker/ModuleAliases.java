package com.flowiq.agents.architecture.checker;

import java.util.Locale;
import java.util.Map;

final class ModuleAliases {

    private ModuleAliases() {
    }

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("import", "imports"),
            Map.entry("task", "tasks"),
            Map.entry("transaction", "transactions"),
            Map.entry("notification", "notifications"),
            Map.entry("forecast", "forecasts"),
            Map.entry("report", "reports"),
            Map.entry("analytics", "analytics"),
            Map.entry("auth", "auth"),
            Map.entry("dashboard", "dashboard"),
            Map.entry("aiaccountant", "ai-accountant"),
            Map.entry("a-i-accountant", "ai-accountant"),
            Map.entry("businessguide", "business-guide"),
            Map.entry("business-guide", "business-guide")
    );

    static String normalize(String module) {
        if (module == null || module.isBlank()) {
            return "default";
        }
        String lower = module.toLowerCase(Locale.ROOT);
        return ALIASES.getOrDefault(lower, lower);
    }

    static String moduleToken(String module) {
        return normalize(module).replace("-", "");
    }
}
