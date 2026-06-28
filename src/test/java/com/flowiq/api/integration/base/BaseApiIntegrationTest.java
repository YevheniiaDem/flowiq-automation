package com.flowiq.api.integration.base;

import com.flowiq.api.regression.base.BaseRegressionApiTest;

public abstract class BaseApiIntegrationTest extends BaseRegressionApiTest {

    protected long createTransactionForIsolationTest() {
        return createTrackedTransaction().getId();
    }
}
