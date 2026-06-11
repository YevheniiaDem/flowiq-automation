package com.flowiq.base;

import com.flowiq.clients.PlaywrightFactory;
import com.flowiq.config.ConfigManager;
import com.flowiq.config.EnvironmentConfig;
import com.flowiq.pages.Pages;
import com.flowiq.utils.UiAttachmentHelper;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@Slf4j
public abstract class BaseUiTest {

    protected EnvironmentConfig config;
    protected Page page;
    protected Pages pages;

    @BeforeMethod(alwaysRun = true)
    public void setUpUiTest() {
        config = ConfigManager.getConfig();
        page = PlaywrightFactory.createPage();
        pages = new Pages(page);
        log.info("UI test setup completed for environment: {}", config.env());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownUiTest(ITestResult result) {
        boolean failed = result != null && result.getStatus() == ITestResult.FAILURE;
        if (failed) {
            UiAttachmentHelper.attachScreenshot(page, result.getName());
        }
        PlaywrightFactory.closeSession(failed);
        log.info("UI test teardown completed");
    }
}
