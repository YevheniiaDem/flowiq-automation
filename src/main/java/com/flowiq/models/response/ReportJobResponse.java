package com.flowiq.models.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportJobResponse {

    private Long id;
    private String reportType;
    private String format;
    private String status;
    private String fileName;
    private Long fileSize;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    private LocalDateTime createdAt;

}
