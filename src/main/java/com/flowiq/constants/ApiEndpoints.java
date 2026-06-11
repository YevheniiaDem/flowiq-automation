package com.flowiq.constants;

public final class ApiEndpoints {

    // Health
    public static final String HEALTH = "/health";
    public static final String HEALTH_PING = "/health/ping";

    // Auth
    public static final String AUTH_REGISTER = "/auth/register";
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_LOGOUT = "/auth/logout";
    public static final String AUTH_ME = "/auth/me";

    // Dashboard
    public static final String DASHBOARD_STATS = "/dashboard/stats";
    public static final String DASHBOARD_INSIGHTS = "/dashboard/insights";
    public static final String DASHBOARD_HEALTH = "/dashboard/health";
    public static final String DASHBOARD_SUMMARY = "/dashboard/summary";
    public static final String DASHBOARD_CHARTS_REVENUE_TREND = "/dashboard/charts/revenue-trend";
    public static final String DASHBOARD_CHARTS_EXPENSE_BREAKDOWN = "/dashboard/charts/expense-breakdown";
    public static final String DASHBOARD_FORECAST_SNAPSHOT = "/dashboard/forecast-snapshot";
    public static final String DASHBOARD_TASKS_SNAPSHOT = "/dashboard/tasks-snapshot";
    public static final String DASHBOARD_BUSINESS_GUIDE_SNAPSHOT = "/dashboard/business-guide-snapshot";

    // Transactions
    public static final String TRANSACTIONS = "/transactions";
    public static final String TRANSACTIONS_SUMMARY = "/transactions/summary";
    public static final String TRANSACTION_BY_ID = "/transactions/{id}";

    // Imports
    public static final String IMPORTS = "/imports";
    public static final String IMPORTS_UPLOAD = "/imports/upload";
    public static final String IMPORT_BY_ID = "/imports/{id}";

    // Analytics
    public static final String ANALYTICS_OVERVIEW = "/analytics/overview";
    public static final String ANALYTICS_REVENUE_TREND = "/analytics/revenue-trend";
    public static final String ANALYTICS_EXPENSE_BREAKDOWN = "/analytics/expense-breakdown";
    public static final String ANALYTICS_PROFIT_TREND = "/analytics/profit-trend";
    public static final String ANALYTICS_FOP_INSIGHTS = "/analytics/fop-insights";
    public static final String ANALYTICS_INCOME_VS_EXPENSES = "/analytics/income-vs-expenses";

    // Reports
    public static final String REPORTS = "/reports";
    public static final String REPORTS_PREVIEW = "/reports/preview";
    public static final String REPORTS_GENERATE = "/reports/generate";
    public static final String REPORT_BY_ID = "/reports/{id}";
    public static final String REPORT_DOWNLOAD = "/reports/{id}/download";

    // Notifications
    public static final String NOTIFICATIONS = "/notifications";
    public static final String NOTIFICATIONS_UNREAD_COUNT = "/notifications/unread-count";
    public static final String NOTIFICATIONS_SUMMARY = "/notifications/summary";
    public static final String NOTIFICATION_READ = "/notifications/{id}/read";
    public static final String NOTIFICATIONS_READ_ALL = "/notifications/read-all";
    public static final String NOTIFICATION_BY_ID = "/notifications/{id}";

    // Tasks
    public static final String TASKS = "/tasks";
    public static final String TASKS_TODAY = "/tasks/today";
    public static final String TASKS_UPCOMING = "/tasks/upcoming";
    public static final String TASKS_GROUPED = "/tasks/grouped";
    public static final String TASKS_SUGGESTIONS = "/tasks/suggestions";
    public static final String TASK_BY_ID = "/tasks/{id}";
    public static final String TASK_COMPLETE = "/tasks/{id}/complete";

    // Forecasts
    public static final String FORECASTS_REVENUE = "/forecasts/revenue";
    public static final String FORECASTS_EXPENSES = "/forecasts/expenses";
    public static final String FORECASTS_PROFIT = "/forecasts/profit";
    public static final String FORECASTS_TAXES = "/forecasts/taxes";
    public static final String FORECASTS_FOP_LIMIT = "/forecasts/fop-limit";
    public static final String FORECASTS_SUMMARY = "/forecasts/summary";

    // Business Guide
    public static final String BUSINESS_GUIDE_ARTICLES = "/business-guide/articles";
    public static final String BUSINESS_GUIDE_ARTICLE_BY_SLUG = "/business-guide/articles/{slug}";
    public static final String BUSINESS_GUIDE_CATEGORIES = "/business-guide/categories";
    public static final String BUSINESS_GUIDE_SEARCH = "/business-guide/search";
    public static final String BUSINESS_GUIDE_DASHBOARD_SNAPSHOT = "/business-guide/dashboard-snapshot";

    // AI Accountant
    public static final String AI_ACCOUNTANT_HEALTH = "/ai-accountant/health";
    public static final String AI_ACCOUNTANT_RECOMMENDATIONS = "/ai-accountant/recommendations";
    public static final String AI_ACCOUNTANT_TAX_ADVISOR = "/ai-accountant/tax-advisor";
    public static final String AI_ACCOUNTANT_FORECASTS = "/ai-accountant/forecasts";
    public static final String AI_ACCOUNTANT_CHAT = "/ai-accountant/chat";

    private ApiEndpoints() {
    }
}
