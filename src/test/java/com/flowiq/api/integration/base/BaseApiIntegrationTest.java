package com.flowiq.api.integration.base;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.RegisterRequest;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.models.response.TransactionResponse;
import com.flowiq.support.TestCleanupManager;

public abstract class BaseApiIntegrationTest extends BaseRegressionApiTest {

    protected AuthResponse loginAsSecondaryUser(RegisterRequest registerRequest) {
        TokenManager.clear();
        AuthResponse auth = authService.login(
                TestDataFactory.loginRequest(registerRequest.getEmail(), registerRequest.getPassword()));
        return auth;
    }

    protected RegisterRequest registerSecondaryUser() {
        RegisterRequest request = TestDataFactory.randomRegisterRequest();
        var result = authService.attemptRegister(request);
        if (result.getStatusCode() != 201) {
            throw new IllegalStateException("Failed to register secondary user: HTTP " + result.getStatusCode());
        }
        return request;
    }

    protected void restoreDefaultUserSession() {
        TokenManager.clear();
        loginAsDefaultUser();
    }

    protected long createTransactionForIsolationTest() {
        var created = transactionService.create(
                com.flowiq.factories.builders.TransactionRequestBuilder.expense()
                        .uniqueDescription()
                        .today()
                        .build());
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());
        return created.getId();
    }

    protected TransactionResponse createTrackedTransaction() {
        TransactionResponse created = transactionService.create(
                com.flowiq.factories.builders.TransactionRequestBuilder.expense()
                        .uniqueDescription()
                        .today()
                        .build());
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());
        return created;
    }
}
