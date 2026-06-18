package com.flowiq.agents.traceability.docs;

import com.flowiq.agents.traceability.model.BusinessFeature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UiSmokeDocExtractor implements DocFeatureExtractor {

    private static final Pattern PAGE_ROW = Pattern.compile(
            "\\|\\s*([A-Za-z][A-Za-z0-9 ]+?)\\s*\\|\\s*\\d+\\s*\\|");

  private static final Map<String, String> PAGE_TO_MODULE = Map.ofEntries(
            Map.entry("Login", "auth"),
            Map.entry("Dashboard", "dashboard"),
            Map.entry("Transactions", "transactions"),
            Map.entry("Imports", "imports"),
            Map.entry("Reports", "reports"),
            Map.entry("Tasks", "tasks"),
            Map.entry("Notifications", "notifications"),
            Map.entry("Forecasts", "forecasts"),
            Map.entry("Business Guide", "business-guide"),
            Map.entry("AI Accountant", "ai-accountant")
    );

    @Override
    public String docFileName() {
        return "UI-SMOKE-STABILITY.md";
    }

    @Override
    public List<BusinessFeature> extract(String markdown) {
        Map<String, BusinessFeature.BusinessFeatureBuilder> features = new LinkedHashMap<>();
        Matcher matcher = PAGE_ROW.matcher(markdown);
        while (matcher.find()) {
            String page = matcher.group(1).trim();
            if ("Page".equalsIgnoreCase(page) || "Risk".equalsIgnoreCase(page)) {
                continue;
            }
            String module = PAGE_TO_MODULE.getOrDefault(page, ModuleNameNormalizer.toSlug(page));
            features.computeIfAbsent(module, m -> BusinessFeature.builder()
                    .module(m)
                    .displayName(page)
                    .description("Documented in UI smoke stability report"))
                    .docSource(docFileName());
        }
        return features.values().stream()
                .map(BusinessFeature.BusinessFeatureBuilder::build)
                .toList();
    }
}
