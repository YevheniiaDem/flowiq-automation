package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.request.CreateTransactionRequest;
import com.flowiq.models.request.UpdateTransactionRequest;
import com.flowiq.models.response.TransactionPageResponse;
import com.flowiq.models.response.TransactionResponse;
import com.flowiq.models.response.TransactionSummaryResponse;
import io.qameta.allure.Step;

import java.util.HashMap;
import java.util.Map;

public class TransactionService extends BaseApiService {

    @Step("List transactions")
    public TransactionPageResponse list(Map<String, ?> queryParams) {
        return getOk(ApiEndpoints.TRANSACTIONS, queryParams, TransactionPageResponse.class);
    }

    @Step("List transactions (default pagination)")
    public TransactionPageResponse list() {
        return list(Map.of("page", 0, "size", 10));
    }

    @Step("Get transaction summary")
    public TransactionSummaryResponse getSummary(Map<String, ?> queryParams) {
        return getOk(ApiEndpoints.TRANSACTIONS_SUMMARY, queryParams, TransactionSummaryResponse.class);
    }

    @Step("Get transaction by id {id}")
    public TransactionResponse getById(long id) {
        return getOk(ApiEndpoints.TRANSACTION_BY_ID.replace("{id}", String.valueOf(id)), TransactionResponse.class);
    }

    @Step("Create transaction")
    public TransactionResponse create(CreateTransactionRequest request) {
        return postCreated(ApiEndpoints.TRANSACTIONS, request, TransactionResponse.class);
    }

    @Step("Update transaction {id}")
    public TransactionResponse update(long id, UpdateTransactionRequest request) {
        return BaseResponseSpecification.extractOk(
                put(ApiEndpoints.TRANSACTION_BY_ID.replace("{id}", String.valueOf(id)), request),
                TransactionResponse.class);
    }

    @Step("Delete transaction {id}")
    public void deleteById(long id) {
        deleteNoContent(ApiEndpoints.TRANSACTION_BY_ID.replace("{id}", String.valueOf(id)));
    }

    @Step("Search transactions: {search}")
    public TransactionPageResponse search(String search) {
        Map<String, Object> params = new HashMap<>();
        params.put("search", search);
        params.put("page", 0);
        params.put("size", 10);
        return list(params);
    }

    @Step("Fetch transactions list (unchecked)")
    public ApiCallResult<TransactionPageResponse> fetchList(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.TRANSACTIONS, queryParams, TransactionPageResponse.class);
    }

    @Step("Fetch transactions list (unchecked, default pagination)")
    public ApiCallResult<TransactionPageResponse> fetchList() {
        return fetchList(Map.of("page", 0, "size", 10));
    }

    @Step("Fetch transactions without authentication")
    public ApiCallResult<TransactionPageResponse> fetchListUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.TRANSACTIONS, Map.of("page", 0, "size", 10), TransactionPageResponse.class);
    }

    @Step("Attempt create transaction")
    public ApiCallResult<TransactionResponse> attemptCreate(CreateTransactionRequest request) {
        return attemptPost(ApiEndpoints.TRANSACTIONS, request, TransactionResponse.class);
    }
}
