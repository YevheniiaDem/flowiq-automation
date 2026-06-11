package com.flowiq.models.tasks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSuggestionResponse {

    private String id;
    private String title;
    private String description;
    private String type;
    private String priority;
    private LocalDate suggestedDueDate;
}
