package com.flowiq.base;

import com.flowiq.assertions.SoftAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiClientFactory;
import com.flowiq.config.ConfigManager;
import com.flowiq.config.EnvironmentConfig;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.services.AIAccountantService;
import com.flowiq.services.AnalyticsService;
import com.flowiq.services.AuthService;
import com.flowiq.services.BusinessGuideService;
import com.flowiq.services.DashboardService;
import com.flowiq.services.ForecastService;
import com.flowiq.services.ImportService;
import com.flowiq.services.NotificationService;
import com.flowiq.services.ProfileService;
import com.flowiq.services.ReportService;
import com.flowiq.services.SettingsService;
import com.flowiq.services.TaskService;
import com.flowiq.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

@Slf4j
public abstract class BaseApiTest {

    protected EnvironmentConfig config;
    protected AuthService authService;
    protected DashboardService dashboardService;
    protected TransactionService transactionService;
    protected ImportService importService;
    protected AnalyticsService analyticsService;
    protected ReportService reportService;
    protected NotificationService notificationService;
    protected TaskService taskService;
    protected ForecastService forecastService;
    protected BusinessGuideService businessGuideService;
    protected AIAccountantService aiAccountantService;
    protected ProfileService profileService;
    protected SettingsService settingsService;

    @BeforeClass(alwaysRun = true)
    public void setUpApiTests() {
        config = ConfigManager.getConfig();
        authService = new AuthService();
        dashboardService = new DashboardService();
        transactionService = new TransactionService();
        importService = new ImportService();
        analyticsService = new AnalyticsService();
        reportService = new ReportService();
        notificationService = new NotificationService();
        taskService = new TaskService();
        forecastService = new ForecastService();
        businessGuideService = new BusinessGuideService();
        aiAccountantService = new AIAccountantService();
        profileService = new ProfileService();
        settingsService = new SettingsService();
        ApiClientFactory.baseSpec();
        log.info("API test setup completed for environment: {}", config.env());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownApiTests() {
        if (TokenManager.isAuthenticated()) {
            authService.logout();
        }
        ApiClientFactory.reset();
        log.info("API test teardown completed");
    }

    protected AuthResponse loginAsDefaultUser() {
        return authService.login(TestDataFactory.defaultLoginRequest());
    }

    protected void ensureAuthenticated() {
        if (!TokenManager.isAuthenticated()) {
            loginAsDefaultUser();
        }
    }

    protected SoftAssertions softAssert() {
        return new SoftAssertions();
    }
}
