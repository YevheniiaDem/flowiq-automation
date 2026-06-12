package com.flowiq.api.regression.base;

import com.flowiq.auth.TokenManager;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.RegisterRequest;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.models.response.TransactionResponse;
import com.flowiq.support.TestCleanupManager;

/**
 * Base class for API regression tests in {@code com.flowiq.api.regression}.
 */
public abstract class BaseRegressionApiTest extends com.flowiq.base.BaseRegressionApiTest {

    protected static final long INVALID_ID = 9_999_999_999L;

    protected RegisterRequest registerSecondaryUser() {
        RegisterRequest request = TestDataFactory.randomRegisterRequest();
        var result = authService.attemptRegister(request);
        if (result.getStatusCode() != 201) {
            throw new IllegalStateException("Failed to register secondary user: HTTP " + result.getStatusCode());
        }
        return request;
    }

    protected void loginAsSecondaryUser(RegisterRequest registerRequest) {
        TokenManager.clear();
        authService.login(TestDataFactory.loginRequest(registerRequest.getEmail(), registerRequest.getPassword()));
    }

    protected void restoreDefaultUserSession() {
        TokenManager.clear();
        loginAsDefaultUser();
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
