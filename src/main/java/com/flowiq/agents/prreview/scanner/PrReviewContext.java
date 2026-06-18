package com.flowiq.agents.prreview.scanner;

import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.prreview.model.PrChangedArtifact;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class PrReviewContext {
    List<String> changedFiles;
    List<ApiChange> apiChanges;
    @Singular("artifact")
    List<PrChangedArtifact> changedArtifacts;
    List<ScannedTestReference> testReferences;
    SourceInventory sourceInventory;
    String dataSourcesSummary;
}
