package com.flowiq.providers;

import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.LoginRequest;
import org.testng.annotations.DataProvider;

public final class AuthDataProvider {

    private AuthDataProvider() {
    }

    @DataProvider(name = "invalidCredentials")
    public static Object[][] invalidCredentials() {
        return new Object[][]{
                {TestDataFactory.loginRequest("", "password")},
                {TestDataFactory.loginRequest("user@flowiq.ai", "")},
                {TestDataFactory.loginRequest("not-an-email", "password")},
                {TestDataFactory.randomLoginRequest()}
        };
    }
}
