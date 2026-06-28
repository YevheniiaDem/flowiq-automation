package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.ChartsComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DashboardPage extends BasePage {

    private final ChartsComponent chartWidgets;

    public DashboardPage(Page page) {
        super(page);
        this.chartWidgets = new ChartsComponent(page);
    }

    @Override
    protected String getPath() {
        return UiPaths.DASHBOARD;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.DASHBOARD_PAGE;
    }

    public Locator statsGrid() {
        return byTestId(TestIds.DASHBOARD_STATS);
    }

    public Locator mainContent() {
        return header.mainContent();
    }

    public int getStatCardCount() {
        return statsGrid().locator("> *").count();
    }

    public void waitForStatsLoaded() {
        waitForVisible(statsGrid());
    }

    public Locator charts() {
        return chartWidgets.containers();
    }

    public int getChartCount() {
        return chartWidgets.count();
    }

    public void waitForChartsLoaded() {
        chartWidgets.waitUntilVisible();
    }
}
