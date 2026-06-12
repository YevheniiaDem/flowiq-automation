package com.flowiq.api.integration.base;

import com.flowiq.base.BaseApiDbIT;

/**
 * Base for API integration tests that verify API ↔ Testcontainer DB consistency.
 * Requires backend started with spring.datasource.url pointing to the test container.
 */
public abstract class BaseApiIntegrationDbTest extends BaseApiDbIT {
}
