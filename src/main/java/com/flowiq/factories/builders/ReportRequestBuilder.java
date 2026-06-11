package com.flowiq.factories.builders;

import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.GenerateReportRequest;

public final class ReportRequestBuilder {

    private final GenerateReportRequest request;

    private ReportRequestBuilder(GenerateReportRequest request) {
        this.request = request;
    }

    public static ReportRequestBuilder profitAndLossPdf() {
        return new ReportRequestBuilder(TestDataFactory.validReportRequest());
    }

    public ReportRequestBuilder type(GenerateReportRequest.ReportType reportType) {
        request.setReportType(reportType);
        return this;
    }

    public ReportRequestBuilder format(GenerateReportRequest.Format format) {
        request.setFormat(format);
        return this;
    }

    public ReportRequestBuilder periodPreset(String periodPreset) {
        request.setPeriodPreset(periodPreset);
        return this;
    }

    public GenerateReportRequest build() {
        return request;
    }
}
