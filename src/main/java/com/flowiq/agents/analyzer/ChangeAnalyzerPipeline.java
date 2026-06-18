package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ChangeAnalyzerPipeline {

    private final List<ChangeAnalyzer> analyzers;

    public ChangeAnalyzerPipeline(List<ChangeAnalyzer> analyzers) {
        this.analyzers = List.copyOf(analyzers);
    }

    public static ChangeAnalyzerPipeline defaultPipeline() {
        return new ChangeAnalyzerPipeline(List.of(
                new EndpointChangeAnalyzer(),
                new RequestDtoChangeAnalyzer(),
                new ResponseDtoChangeAnalyzer(),
                new EnumChangeAnalyzer(),
                new StatusCodeChangeAnalyzer(),
                new RequiredFieldChangeAnalyzer()
        ));
    }

    public List<ApiChange> analyze(JsonNode previousSpec, JsonNode currentSpec) {
        List<ApiChange> allChanges = new ArrayList<>();
        for (ChangeAnalyzer analyzer : analyzers) {
            List<ApiChange> detected = analyzer.analyze(previousSpec, currentSpec);
            log.debug("{} detected {} change(s)", analyzer.name(), detected.size());
            allChanges.addAll(detected);
        }
        allChanges.addAll(deriveBreakingChanges(allChanges));
        return List.copyOf(new LinkedHashSet<>(allChanges));
    }

    private List<ApiChange> deriveBreakingChanges(List<ApiChange> changes) {
        List<ApiChange> breaking = new ArrayList<>();
        for (ApiChange change : changes) {
            if (change.isBreaking()) {
                breaking.add(ApiChange.builder()
                        .type(ChangeType.BREAKING_CHANGE)
                        .path(change.getPath())
                        .method(change.getMethod())
                        .schema(change.getSchema())
                        .field(change.getField())
                        .description("Breaking: " + change.getDescription())
                        .breaking(true)
                        .build());
            }
        }
        return breaking;
    }
}
