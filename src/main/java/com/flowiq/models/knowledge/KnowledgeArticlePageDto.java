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
public class KnowledgeArticlePageDto {

    private List<KnowledgeArticleSummaryDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
