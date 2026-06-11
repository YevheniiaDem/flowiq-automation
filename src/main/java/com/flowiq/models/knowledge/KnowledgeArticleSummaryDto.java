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
public class KnowledgeArticleSummaryDto {

    private Long id;
    private String slug;
    private String title;
    private String category;
    private String summary;
    private List<String> tags;
    private LocalDate publishedAt;
    private LocalDateTime updatedAt;
    private int readingTimeMinutes;
    private String highlight;
    private String impact;



    private static int estimateReadingTime(String content) {
        if (content == null || content.isBlank()) {
            return 1;
        }
        int words = content.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(words / 200.0));
    }

    private static String buildHighlight(String title, String summary, String query) {
        String normalized = query.trim().toLowerCase();
        String source = title != null ? title : "";
        if (summary != null && summary.toLowerCase().contains(normalized)) {
            source = summary;
        }
        int index = source.toLowerCase().indexOf(normalized);
        if (index < 0) {
            return summary != null && !summary.isBlank() ? summary : title;
        }
        int start = Math.max(0, index - 40);
        int end = Math.min(source.length(), index + normalized.length() + 60);
        String excerpt = source.substring(start, end).trim();
        if (start > 0) {
            excerpt = "…" + excerpt;
        }
        if (end < source.length()) {
            excerpt = excerpt + "…";
        }
        return excerpt;
    }
}
