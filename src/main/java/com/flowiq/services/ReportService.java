package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiResponse;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.request.GenerateReportRequest;
import com.flowiq.models.response.ReportJobResponse;
import com.flowiq.models.response.ReportListResponse;
import com.flowiq.models.response.ReportPreviewResponse;
import io.qameta.allure.Step;

import java.util.Map;

public class ReportService extends BaseApiService {

    @Step("List generated reports")
    public ReportListResponse list() {
        return getOk(ApiEndpoints.REPORTS, ReportListResponse.class);
    }

    @Step("Preview report")
    public ReportPreviewResponse preview(Map<String, ?> queryParams) {
        return getOk(ApiEndpoints.REPORTS_PREVIEW, queryParams, ReportPreviewResponse.class);
    }

    @Step("Generate report")
    public ReportJobResponse generate(GenerateReportRequest request) {
        return postCreated(ApiEndpoints.REPORTS_GENERATE, request, ReportJobResponse.class);
    }

    @Step("Get report job by id {id}")
    public ReportJobResponse getById(long id) {
        return getOk(ApiEndpoints.REPORT_BY_ID.replace("{id}", String.valueOf(id)), ReportJobResponse.class);
    }

    @Step("Download report file {id}")
    public ApiResponse download(long id) {
        return get(ApiEndpoints.REPORT_DOWNLOAD.replace("{id}", String.valueOf(id)));
    }

    @Step("Fetch reports list (unchecked)")
    public ApiCallResult<ReportListResponse> fetchList() {
        return fetch(ApiEndpoints.REPORTS, ReportListResponse.class);
    }

    @Step("Fetch reports without authentication")
    public ApiCallResult<ReportListResponse> fetchListUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.REPORTS, ReportListResponse.class);
    }

    @Step("Attempt generate report")
    public ApiCallResult<ReportJobResponse> attemptGenerate(GenerateReportRequest request) {
        return attemptPost(ApiEndpoints.REPORTS_GENERATE, request, ReportJobResponse.class);
    }

    @Step("Fetch report preview (unchecked)")
    public ApiCallResult<ReportPreviewResponse> fetchPreview(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.REPORTS_PREVIEW, queryParams, ReportPreviewResponse.class);
    }

    @Step("Fetch report preview without authentication")
    public ApiCallResult<ReportPreviewResponse> fetchPreviewUnauthorized(Map<String, ?> queryParams) {
        return fetchUnauthenticated(ApiEndpoints.REPORTS_PREVIEW, queryParams, ReportPreviewResponse.class);
    }

    @Step("Attempt download report {id}")
    public ApiCallResult<Void> attemptDownload(long id) {
        return ApiCallResult.from(download(id));
    }

    @Step("Fetch report by id {id} (unchecked)")
    public ApiCallResult<ReportJobResponse> fetchById(long id) {
        return attemptGet(ApiEndpoints.REPORT_BY_ID.replace("{id}", String.valueOf(id)), ReportJobResponse.class);
    }
}
