package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.response.ImportJobResponse;
import com.flowiq.models.response.ImportListResponse;
import io.qameta.allure.Step;

import java.io.File;

public class ImportService extends BaseApiService {

    @Step("Upload CSV import file")
    public ImportJobResponse upload(File file) {
        return BaseResponseSpecification.extractCreated(
                postMultipart(ApiEndpoints.IMPORTS_UPLOAD, file),
                ImportJobResponse.class);
    }

    @Step("List import jobs")
    public ImportListResponse list() {
        return getOk(ApiEndpoints.IMPORTS, ImportListResponse.class);
    }

    @Step("Get import job by id {id}")
    public ImportJobResponse getById(long id) {
        return getOk(ApiEndpoints.withPathParam(ApiEndpoints.IMPORT_BY_ID, "id", id), ImportJobResponse.class);
    }

    @Step("Fetch import jobs (unchecked)")
    public ApiCallResult<ImportListResponse> fetchList() {
        return fetch(ApiEndpoints.IMPORTS, ImportListResponse.class);
    }

    @Step("Fetch import jobs without authentication")
    public ApiCallResult<ImportListResponse> fetchListUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.IMPORTS, ImportListResponse.class);
    }

    @Step("Attempt upload import file")
    public ApiCallResult<ImportJobResponse> attemptUpload(File file) {
        return ApiCallResult.from(postMultipart(ApiEndpoints.IMPORTS_UPLOAD, file), ImportJobResponse.class);
    }

    @Step("Fetch import job by id {id} (unchecked)")
    public ApiCallResult<ImportJobResponse> fetchById(long id) {
        return attemptGet(ApiEndpoints.withPathParam(ApiEndpoints.IMPORT_BY_ID, "id", id), ImportJobResponse.class);
    }
}
