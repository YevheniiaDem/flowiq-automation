package com.flowiq.contracts.base;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseApiTest;
import org.testng.annotations.BeforeMethod;

public abstract class BaseContractTest extends BaseApiTest {

    protected abstract boolean requiresAuthentication();

    @BeforeMethod(alwaysRun = true)
    public void prepareContractTest() {
        if (requiresAuthentication()) {
            ensureAuthenticated();
        }
    }

    protected void ensureAuthenticated() {
        if (!TokenManager.isAuthenticated()) {
            loginAsDefaultUser();
        }
    }
}
