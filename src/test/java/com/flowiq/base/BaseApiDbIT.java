package com.flowiq.base;

import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiClientFactory;
import com.flowiq.db.support.IntegrationTestContext;
import com.flowiq.services.AuthService;
import com.flowiq.services.ImportService;
import com.flowiq.services.NotificationService;
import com.flowiq.services.ReportService;
import com.flowiq.services.TaskService;
import com.flowiq.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@Slf4j
public abstract class BaseApiDbIT extends BaseDbTest {

    protected AuthService authService;
    protected TransactionService transactionService;
    protected ReportService reportService;
    protected ImportService importService;
    protected TaskService taskService;
    protected NotificationService notificationService;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "prepareIsolatedDatabase")
    public void requireBackendOnTestDatabase() {
        if (!IntegrationTestContext.isBackendConnectedToTestDatabase()) {
            throw new SkipException(
                    "Backend is not connected to the Testcontainer database. "
                            + "Start flowiq-backend with spring.datasource.url="
                            + System.getProperty("flowiq.test.jdbc.url"));
        }
        authService = new AuthService();
        transactionService = new TransactionService();
        reportService = new ReportService();
        importService = new ImportService();
        taskService = new TaskService();
        notificationService = new NotificationService();
        ApiClientFactory.baseSpec();
        authService.login(seededUser().toLoginRequest());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownApiSession() {
        try {
            if (authService != null && TokenManager.isAuthenticated()) {
                authService.logout();
            }
        } catch (Exception e) {
            log.warn("API logout failed during DB integration teardown", e);
        } finally {
            ApiClientFactory.reset();
            TokenManager.clear();
        }
    }
}
