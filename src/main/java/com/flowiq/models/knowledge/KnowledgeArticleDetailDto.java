package com.flowiq.models.knowledge;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticleDetailDto {

    private Long id;
    private String slug;
    private String title;
    private String category;
    private String content;
    private String summary;
    private String impact;
    private List<String> tags;
    private LocalDate publishedAt;
    private LocalDateTime updatedAt;
    private int readingTimeMinutes;
    private List<KnowledgeArticleSummaryDto> relatedArticles;

}
