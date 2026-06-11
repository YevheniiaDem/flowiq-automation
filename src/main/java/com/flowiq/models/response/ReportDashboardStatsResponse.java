package com.flowiq.models.response;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDashboardStatsResponse {

    private long generatedReports;
    private long reportsThisMonth;
    private LocalDateTime lastGeneratedAt;
    private String mostUsedReportType;
}
