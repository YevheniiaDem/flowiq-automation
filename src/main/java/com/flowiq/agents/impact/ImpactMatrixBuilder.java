package com.flowiq.agents.impact;

import com.flowiq.agents.model.AnalysisResult;
import com.flowiq.agents.model.ImpactMatrixEntry;
import com.flowiq.agents.model.TestSuiteType;

import java.util.List;
import java.util.Map;

public class ImpactMatrixBuilder {

    private final TestImpactMapper testImpactMapper;

    public ImpactMatrixBuilder(TestImpactMapper testImpactMapper) {
        this.testImpactMapper = testImpactMapper;
    }

    public List<ImpactMatrixEntry> build(AnalysisResult result) {
        return testImpactMapper.buildMatrix(result.getChanges());
    }

    public Map<TestSuiteType, List<String>> affectedTests(AnalysisResult result) {
        return testImpactMapper.mapChanges(result.getChanges());
    }
}
