package com.flowiq.services;

import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiResponse;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.constants.SchemaPaths;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.LoginRequest;
import com.flowiq.models.request.RegisterRequest;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.models.response.UserResponse;
import com.flowiq.validation.JsonSchemaValidator;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthService extends BaseApiService {

    @Step("Login as {request.email}")
    public AuthResponse login(LoginRequest request) {
        ApiResponse response = postPublic(ApiEndpoints.AUTH_LOGIN, request);
        BaseResponseSpecification.validate(response, 200);
        JsonSchemaValidator.validate(response, SchemaPaths.AUTH_LOGIN_LEGACY);
        AuthResponse authResponse = response.as(AuthResponse.class);
        TokenManager.save(authResponse);
        log.info("Authenticated as: {}", authResponse.getUser().getEmail());
        return authResponse;
    }

    @Step("Login as {email}")
    public AuthResponse login(String email, String password) {
        return login(TestDataFactory.loginRequest(email, password));
    }

    @Step("Register user {request.email}")
    public AuthResponse register(RegisterRequest request) {
        ApiResponse response = postPublic(ApiEndpoints.AUTH_REGISTER, request);
        BaseResponseSpecification.validate(response, 201);
        AuthResponse authResponse = response.as(AuthResponse.class);
        TokenManager.save(authResponse);
        return authResponse;
    }

    @Step("Get current authenticated user")
    public UserResponse getCurrentUser() {
        return getOk(ApiEndpoints.AUTH_ME, UserResponse.class);
    }

    @Step("Logout current user")
    public void logout() {
        if (TokenManager.isAuthenticated()) {
            BaseResponseSpecification.validate(post(ApiEndpoints.AUTH_LOGOUT), 204);
        }
        TokenManager.clear();
    }

    @Step("Attempt login as {request.email} (no token persistence)")
    public ApiCallResult<AuthResponse> attemptLogin(LoginRequest request) {
        return fetchPublic(ApiEndpoints.AUTH_LOGIN, request, AuthResponse.class);
    }

    @Step("Attempt register user {request.email} (no token persistence)")
    public ApiCallResult<AuthResponse> attemptRegister(RegisterRequest request) {
        return ApiCallResult.from(postPublic(ApiEndpoints.AUTH_REGISTER, request), AuthResponse.class);
    }

    @Step("Fetch login result as {request.email}")
    public ApiCallResult<AuthResponse> fetchLogin(LoginRequest request) {
        return fetchPublic(ApiEndpoints.AUTH_LOGIN, request, AuthResponse.class);
    }

    @Step("Fetch current user without authentication")
    public ApiCallResult<UserResponse> fetchCurrentUserUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.AUTH_ME, UserResponse.class);
    }

    @Step("Fetch /auth/me (unchecked)")
    public ApiCallResult<UserResponse> fetchMe() {
        return fetch(ApiEndpoints.AUTH_ME, UserResponse.class);
    }
}
