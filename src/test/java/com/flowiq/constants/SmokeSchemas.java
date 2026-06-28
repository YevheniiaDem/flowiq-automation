package com.flowiq.constants;

/**
 * JSON Schema paths for API smoke tests. Uses legacy flat filenames for backward compatibility.
 */
public final class SmokeSchemas {

    public static final String AUTH_LOGIN = SchemaPaths.AUTH_LOGIN_LEGACY;
    public static final String AUTH_ME = SchemaPaths.AUTH_ME_LEGACY;
    public static final String TRANSACTION_PAGE = SchemaPaths.TRANSACTION_PAGE_LEGACY;
    public static final String IMPORT_LIST = SchemaPaths.IMPORT_LIST_LEGACY;
    public static final String REPORT_LIST = SchemaPaths.REPORT_LIST_LEGACY;
    public static final String ANALYTICS_OVERVIEW = SchemaPaths.ANALYTICS_OVERVIEW_LEGACY;
    public static final String NOTIFICATION_PAGE = SchemaPaths.NOTIFICATION_PAGE_LEGACY;
    public static final String TASK_PAGE = SchemaPaths.TASK_PAGE_LEGACY;
    public static final String FORECAST_SUMMARY = SchemaPaths.FORECAST_SUMMARY_LEGACY;
    public static final String BUSINESS_GUIDE_ARTICLES = SchemaPaths.BUSINESS_GUIDE_ARTICLES_LEGACY;
    public static final String AI_ACCOUNTANT_HEALTH = SchemaPaths.AI_ACCOUNTANT_HEALTH_LEGACY;

    private SmokeSchemas() {
    }
}
