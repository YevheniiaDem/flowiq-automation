package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class AnalyticsPage extends BasePage {

    public AnalyticsPage(Page page) {
        super(page);
    }

    @Override
    protected String getPath() {
        return UiPaths.ANALYTICS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.ANALYTICS_PAGE;
    }

    @Override
    public AnalyticsPage open() {
        super.open();
        return this;
    }

    public Locator pageRoot() {
        return byTestId(TestIds.ANALYTICS_PAGE);
    }

    public Locator emptyState() {
        return byTestId("analytics-empty-state");
    }

    public Locator summaryCards() {
        return page.locator("[data-testid='analytics-page'] .grid").first();
    }

    public boolean hasContentOrEmptyState() {
        return pageRoot().isVisible()
                && (emptyState().isVisible() || summaryCards().count() > 0);
    }
}
