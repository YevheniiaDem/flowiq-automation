package com.flowiq.agents.traceability.docs;

import com.flowiq.agents.traceability.model.BusinessFeature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContractDocExtractor implements DocFeatureExtractor {

    private static final Pattern DOMAIN_ROW = Pattern.compile(
            "\\|\\s*([A-Za-z][A-Za-z0-9 \\-]+?)\\s*\\|\\s*(GET|POST|PUT|PATCH|DELETE)\\s*\\|");

    @Override
    public String docFileName() {
        return "CONTRACT-COVERAGE.md";
    }

    @Override
    public List<BusinessFeature> extract(String markdown) {
        Map<String, BusinessFeature.BusinessFeatureBuilder> features = new LinkedHashMap<>();
        Matcher matcher = DOMAIN_ROW.matcher(markdown);
        while (matcher.find()) {
            String displayName = matcher.group(1).trim();
            if ("Domain".equalsIgnoreCase(displayName)) {
                continue;
            }
            String module = ModuleNameNormalizer.toSlug(displayName);
            features.computeIfAbsent(module, m -> BusinessFeature.builder()
                    .module(m)
                    .displayName(displayName)
                    .description("Documented in contract coverage"))
                    .docSource(docFileName());
        }
        return features.values().stream()
                .map(BusinessFeature.BusinessFeatureBuilder::build)
                .toList();
    }
}
