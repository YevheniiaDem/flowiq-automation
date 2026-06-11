package com.flowiq.models.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDashboardSnapshotDto {

    private List<KnowledgeArticleSummaryDto> popularArticles;
    private List<KnowledgeArticleSummaryDto> recentlyUpdated;
    private List<KnowledgeArticleSummaryDto> recommendedForYou;
    private List<KnowledgeArticleSummaryDto> latestLegalChanges;
}
