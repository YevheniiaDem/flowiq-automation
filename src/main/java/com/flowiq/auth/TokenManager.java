package com.flowiq.auth;

import com.flowiq.models.response.AuthResponse;
import com.flowiq.models.response.UserResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TokenManager {

    private static final ThreadLocal<TokenSession> SESSION = new ThreadLocal<>();

    private TokenManager() {
    }

    public static void save(AuthResponse authResponse) {
        save(authResponse.getToken(), authResponse.getRefreshToken(), authResponse.getUser());
    }

    public static void save(String accessToken, String refreshToken, UserResponse user) {
        SESSION.set(new TokenSession(accessToken, refreshToken, user));
        log.debug("JWT saved for user: {}", user != null ? user.getEmail() : "unknown");
    }

    public static String getAccessToken() {
        TokenSession session = SESSION.get();
        return session != null ? session.accessToken() : null;
    }

    public static String getRefreshToken() {
        TokenSession session = SESSION.get();
        return session != null ? session.refreshToken() : null;
    }

    public static UserResponse getCurrentUser() {
        TokenSession session = SESSION.get();
        return session != null ? session.user() : null;
    }

    public static boolean isAuthenticated() {
        String token = getAccessToken();
        return token != null && !token.isBlank();
    }

    public static void clear() {
        SESSION.remove();
        log.debug("JWT cleared");
    }

    public record TokenSession(String accessToken, String refreshToken, UserResponse user) {
    }
}
