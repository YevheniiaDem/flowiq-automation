package com.flowiq.base;

import com.flowiq.services.AnalyticsService;
import com.flowiq.services.ForecastService;
import com.flowiq.services.ImportService;
import com.flowiq.services.NotificationService;
import com.flowiq.services.ReportService;
import com.flowiq.services.TaskService;
import com.flowiq.services.TransactionService;
import com.flowiq.support.TestCleanupManager;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@Slf4j
public abstract class BaseE2ETest extends AuthenticatedUiTest {

    protected TransactionService transactionService;
    protected ImportService importService;
    protected ReportService reportService;
    protected NotificationService notificationService;
    protected TaskService taskService;
    protected AnalyticsService analyticsService;
    protected ForecastService forecastService;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "authenticateUiSession")
    public void wireE2EServices() {
        transactionService = new TransactionService();
        importService = new ImportService();
        reportService = new ReportService();
        notificationService = new NotificationService();
        taskService = new TaskService();
        analyticsService = new AnalyticsService();
        forecastService = new ForecastService();
    }

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "wireE2EServices")
    public void prepareE2ETest() {
        TestCleanupManager.clear();
        log.info("E2E test context ready");
    }

}
