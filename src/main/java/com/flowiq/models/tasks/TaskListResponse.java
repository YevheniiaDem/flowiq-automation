package com.flowiq.models.tasks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListResponse {

    private List<TaskResponse> today;
    private List<TaskResponse> upcoming;
    private List<TaskResponse> overdue;
    private List<TaskResponse> completed;
}
