package com.flowiq.constants;

public final class TestIds {

  // Register
  public static final String REGISTER_PAGE = "register-page";

  // Login
  public static final String LOGIN_PAGE = "login-page";
  public static final String LOGIN_EMAIL = "login-email";
  public static final String LOGIN_PASSWORD = "login-password";
  public static final String LOGIN_SUBMIT = "login-submit";
  public static final String LOGIN_ERROR = "login-error";

  // Layout
  public static final String SIDEBAR = "sidebar";
  public static final String MAIN_CONTENT = "main-content";

  // Pages
  public static final String ANALYTICS_PAGE = "analytics-page";
  public static final String SETTINGS_PAGE = "settings-page";
  public static final String DASHBOARD_PAGE = "dashboard-page";
  public static final String TRANSACTIONS_PAGE = "transactions-page";
  public static final String IMPORTS_PAGE = "imports-page";
  public static final String REPORTS_PAGE = "reports-page";
  public static final String NOTIFICATIONS_PAGE = "notifications-page";
  public static final String TASKS_PAGE = "tasks-page";
  public static final String FORECASTS_PAGE = "forecasts-page";
  public static final String BUSINESS_GUIDE_PAGE = "business-guide-page";
  public static final String AI_ACCOUNTANT_PAGE = "ai-accountant-page";

  // Dashboard
  public static final String DASHBOARD_STATS = "dashboard-stats";

  // Transactions
  public static final String TRANSACTIONS_ADD_BTN = "transactions-add-btn";
  public static final String TRANSACTIONS_IMPORT_BTN = "transactions-import-btn";
  public static final String TRANSACTIONS_EXPORT_BTN = "transactions-export-btn";
  public static final String TRANSACTIONS_IMPORT_INPUT = "transactions-import-input";
  public static final String TRANSACTIONS_SEARCH = "transactions-search";
  public static final String TRANSACTIONS_FILTERS = "transactions-filters";
  public static final String TRANSACTIONS_TABLE = "transactions-table";
  public static final String TRANSACTION_FORM_MODAL = "transaction-form-modal";
  public static final String TRANSACTION_FORM_AMOUNT = "transaction-form-amount";
  public static final String TRANSACTION_FORM_DESCRIPTION = "transaction-form-description";
  public static final String TRANSACTION_FORM_SUBMIT = "transaction-form-submit";

  // Imports
  public static final String IMPORTS_UPLOAD_ZONE = "imports-upload-zone";
  public static final String IMPORTS_FILE_INPUT = "imports-file-input";
  public static final String IMPORTS_BROWSE_BTN = "imports-browse-btn";
  public static final String IMPORTS_HISTORY_TABLE = "imports-history-table";

  // Reports
  public static final String REPORTS_GENERATE_BTN = "reports-generate-btn";
  public static final String REPORTS_GENERATE_DIALOG = "reports-generate-dialog";
  public static final String REPORTS_GENERATE_SUBMIT = "reports-generate-submit";

  // Notifications
  public static final String NOTIFICATIONS_MARK_ALL_READ_BTN = "notifications-mark-all-read-btn";
  public static final String NOTIFICATIONS_FILTERS = "notifications-filters";
  public static final String NOTIFICATIONS_LIST = "notifications-list";

  // Tasks
  public static final String TASKS_ADD_BTN = "tasks-add-btn";
  public static final String TASKS_FILTERS = "tasks-filters";
  public static final String TASKS_SEARCH = "tasks-search";

  // Forecasts
  public static final String FORECASTS_SUMMARY_CARDS = "forecasts-summary-cards";

  // Business Guide
  public static final String BUSINESS_GUIDE_SEARCH = "business-guide-search";

  // Onboarding & activation
  public static final String ONBOARDING_WELCOME_MODAL = "onboarding-welcome-modal";
  public static final String ONBOARDING_START_TOUR_BTN = "onboarding-start-tour-btn";
  public static final String ONBOARDING_SKIP_BTN = "onboarding-skip-btn";
  public static final String ACTIVATION_CHECKLIST = "activation-checklist";
  public static final String HELP_LEARN_CENTER = "help-learn-center";
  public static final String DEMO_WORKSPACE_BANNER = "demo-workspace-banner";
  public static final String WHATS_NEW_MODAL = "whats-new-modal";

  // AI Accountant
  public static final String AI_ACCOUNTANT_CHAT = "ai-accountant-chat";
  public static final String AI_ACCOUNTANT_CHAT_FORM = "ai-accountant-chat-form";
  public static final String AI_ACCOUNTANT_CHAT_INPUT = "ai-accountant-chat-input";
  public static final String AI_ACCOUNTANT_CHAT_SEND_BTN = "ai-accountant-chat-send-btn";

  public static String navLink(String section) {
    return "nav-link-" + section;
  }

  public static String notificationFilter(String filter) {
    return "notifications-filter-" + filter;
  }

  public static String tasksSection(String section) {
    return "tasks-section-" + section;
  }

  private TestIds() {}
}
