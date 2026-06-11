package com.flowiq.models.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GenerateReportRequest {

    private ReportType reportType;
    private Format format;
    private String periodPreset;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public enum ReportType {
        PROFIT_AND_LOSS,
        CASH_FLOW,
        REVENUE_SUMMARY,
        EXPENSE_SUMMARY,
        TAX_SUMMARY,
        FOP_SUMMARY
    }

    public enum Format {
        PDF,
        CSV,
        EXCEL
    }
}
