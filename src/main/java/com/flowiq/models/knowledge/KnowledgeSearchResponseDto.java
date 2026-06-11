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
public class KnowledgeSearchResponseDto {

    private String query;
    private KnowledgeArticleSummaryDto primaryArticle;
    private List<KnowledgeArticleSummaryDto> results;
    private List<KnowledgeArticleSummaryDto> relatedArticles;
    private String quickSummary;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
