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
public class TaskSnapshotResponse {

    private long todayCount;
    private long upcomingCount;
    private long overdueCount;
    private List<TaskResponse> todayTasks;
    private List<TaskResponse> upcomingDeadlines;
}
