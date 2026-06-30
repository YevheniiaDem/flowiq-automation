package com.flowiq.base;

import com.flowiq.support.TestCleanupManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseRegressionApiTest extends BaseSmokeApiTest {

    @BeforeMethod(alwaysRun = true)
    public void prepareRegressionTest() {
        TestCleanupManager.clear();
        ensureAuthenticated();
    }

    @AfterMethod(alwaysRun = true)
    public void cleanupRegressionTest() {
        TestCleanupManager.runAll();
    }
}
