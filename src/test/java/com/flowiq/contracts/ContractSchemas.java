package com.flowiq.contracts;

public final class ContractSchemas {

    private static final String BASE = "schemas/";

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

    private ContractSchemas() {
    }
}
