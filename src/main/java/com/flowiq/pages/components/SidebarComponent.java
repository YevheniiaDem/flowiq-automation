package com.flowiq.pages.components;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SidebarComponent extends BaseComponent {

    public SidebarComponent(Page page) {
        super(page);
    }

    public Locator sidebar() {
        return page.getByTestId(TestIds.SIDEBAR).or(page.locator("aside"));
    }

    public Locator navLink(String section) {
        return page.getByTestId(TestIds.navLink(section));
    }

    public void goToDashboard() {
        navigateTo("dashboard", UiPaths.DASHBOARD);
    }

    public void goToTransactions() {
        navigateTo("transactions", UiPaths.TRANSACTIONS);
    }

    public void goToImports() {
        navigateTo("imports", UiPaths.IMPORTS);
    }

    public void goToReports() {
        navigateTo("reports", UiPaths.REPORTS);
    }

    public void goToNotifications() {
        navigateTo("notifications", UiPaths.NOTIFICATIONS);
    }

    public void goToTasks() {
        navigateTo("tasks", UiPaths.TASKS);
    }

    public void goToForecasts() {
        navigateTo("forecasts", UiPaths.FORECASTS);
    }

    public void goToBusinessGuide() {
        navigateTo("business-guide", UiPaths.BUSINESS_GUIDE);
    }

    public void goToAiAccountant() {
        navigateTo("ai-accountant", UiPaths.AI_ACCOUNTANT);
    }

    public void waitUntilVisible() {
        waitForVisible(sidebar());
    }

    private void navigateTo(String section, String path) {
        Locator link = navLink(section).or(page.locator("a[href='" + path + "']"));
        link.click();
    }
}
