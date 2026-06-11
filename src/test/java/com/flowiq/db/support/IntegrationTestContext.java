package com.flowiq.db.support;

import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiClientFactory;
import com.flowiq.db.cleanup.DatabaseCleaner;
import com.flowiq.db.container.PostgresTestContainer;
import com.flowiq.db.seeder.TestDataSeeder;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.services.AuthService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class IntegrationTestContext {

    private static volatile Boolean backendSharesTestDatabase;

    private IntegrationTestContext() {
    }

    public static boolean isBackendConnectedToTestDatabase() {
        if (backendSharesTestDatabase != null) {
            return backendSharesTestDatabase;
        }
        synchronized (IntegrationTestContext.class) {
            if (backendSharesTestDatabase != null) {
                return backendSharesTestDatabase;
            }
            backendSharesTestDatabase = probeBackendDatabase();
            return backendSharesTestDatabase;
        }
    }

    private static boolean probeBackendDatabase() {
        try {
            PostgresTestContainer postgres = PostgresTestContainer.getInstance();
            DatabaseCleaner.clean(postgres.dataSource());
            var probeUser = TestDataSeeder.seedUser(postgres.dataSource());

            TokenManager.clear();
            ApiClientFactory.baseSpec();
            AuthService authService = new AuthService();
            AuthResponse response = authService.login(probeUser.toLoginRequest());
            authService.logout();
            ApiClientFactory.reset();
            TokenManager.clear();
            DatabaseCleaner.clean(postgres.dataSource());

            boolean connected = response != null && response.getToken() != null;
            log.info("Backend shares Testcontainer database: {}", connected);
            return connected;
        } catch (Exception e) {
            log.warn("Backend is not connected to Testcontainer database: {}", e.getMessage());
            return false;
        }
    }
}
