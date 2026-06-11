package com.flowiq.models.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeCategoryDto {

    private String id;
    private String label;
    private long articleCount;
}
