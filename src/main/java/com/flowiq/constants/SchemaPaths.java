package com.flowiq.constants;

/**
 * Canonical classpath locations for JSON Schema files used by services and tests.
 * Legacy flat filenames remain for backward-compatible smoke validation paths.
 */
public final class SchemaPaths {

    private static final String BASE = "schemas/";

    // Legacy flat paths (smoke tests, AuthService login validation)
    public static final String AUTH_LOGIN_LEGACY = "auth-login-response-schema.json";
    public static final String AUTH_ME_LEGACY = "auth-me-response-schema.json";
    public static final String TRANSACTION_PAGE_LEGACY = "transaction-page-schema.json";
    public static final String IMPORT_LIST_LEGACY = "import-list-schema.json";
    public static final String REPORT_LIST_LEGACY = "report-list-schema.json";
    public static final String ANALYTICS_OVERVIEW_LEGACY = "analytics-overview-schema.json";
    public static final String NOTIFICATION_PAGE_LEGACY = "notification-page-schema.json";
    public static final String TASK_PAGE_LEGACY = "task-page-schema.json";
    public static final String FORECAST_SUMMARY_LEGACY = "forecast-summary-schema.json";
    public static final String BUSINESS_GUIDE_ARTICLES_LEGACY = "business-guide-articles-schema.json";
    public static final String AI_ACCOUNTANT_HEALTH_LEGACY = "ai-accountant-health-schema.json";

    // Canonical organized paths (contract tests)
    public static final String AUTH_LOGIN = BASE + "auth/login-response.schema.json";
    public static final String AUTH_REGISTER = BASE + "auth/register-response.schema.json";
    public static final String AUTH_ME = BASE + "auth/me-response.schema.json";
    public static final String DASHBOARD_STATS = BASE + "dashboard/stats-response.schema.json";
    public static final String PROFILE = BASE + "profile/profile-response.schema.json";
    public static final String IMPORTS_LIST = BASE + "imports/list-response.schema.json";
    public static final String TRANSACTIONS_PAGE = BASE + "transactions/page-response.schema.json";
    public static final String TRANSACTIONS_SUMMARY = BASE + "transactions/summary-response.schema.json";
    public static final String ANALYTICS_OVERVIEW = BASE + "analytics/overview-response.schema.json";
    public static final String ANALYTICS_FOP_INSIGHTS = BASE + "analytics/fop-insights-response.schema.json";
    public static final String REPORTS_LIST = BASE + "reports/list-response.schema.json";
    public static final String REPORTS_PREVIEW = BASE + "reports/preview-response.schema.json";
    public static final String TASKS_PAGE = BASE + "tasks/page-response.schema.json";
    public static final String TASKS_GROUPED = BASE + "tasks/grouped-response.schema.json";
    public static final String NOTIFICATIONS_PAGE = BASE + "notifications/page-response.schema.json";
    public static final String NOTIFICATIONS_SUMMARY = BASE + "notifications/summary-response.schema.json";
    public static final String FORECASTS_SUMMARY = BASE + "forecasts/summary-response.schema.json";
    public static final String BUSINESS_GUIDE_ARTICLES = BASE + "businessguide/articles-page-response.schema.json";
    public static final String AI_ACCOUNTANT_HEALTH = BASE + "aiaccountant/health-response.schema.json";

    private SchemaPaths() {
    }
}
