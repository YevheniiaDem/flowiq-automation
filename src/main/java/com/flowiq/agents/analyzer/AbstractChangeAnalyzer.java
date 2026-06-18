package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractChangeAnalyzer implements ChangeAnalyzer {

    protected final List<ApiChange> changes = new ArrayList<>();

    protected void reset() {
        changes.clear();
    }

    @Override
    public List<ApiChange> analyze(JsonNode previousSpec, JsonNode currentSpec) {
        reset();
        if (previousSpec == null || currentSpec == null) {
            return List.of();
        }
        doAnalyze(previousSpec, currentSpec);
        return List.copyOf(changes);
    }

    protected abstract void doAnalyze(JsonNode previousSpec, JsonNode currentSpec);
}
