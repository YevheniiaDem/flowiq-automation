package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiLocators;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.ChartsComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ForecastsPage extends BasePage {

    private final ChartsComponent chartWidgets;

    public ForecastsPage(Page page) {
        super(page);
        this.chartWidgets = new ChartsComponent(page);
    }

    @Override
    protected String getPath() {
        return UiPaths.FORECASTS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.FORECASTS_PAGE;
    }

    public Locator summaryCards() {
        return byTestId(TestIds.FORECASTS_SUMMARY_CARDS);
    }

    public Locator warningBanners() {
        return byTestIdOr("forecasts-warnings", UiLocators.FORECASTS_WARNINGS_FALLBACK);
    }

    public int getSummaryCardCount() {
        return summaryCards().locator("> *").count();
    }

    public void waitForSummaryLoaded() {
        waitForVisible(summaryCards());
    }

    public Locator charts() {
        return chartWidgets.containers();
    }

    public int getChartCount() {
        return chartWidgets.count();
    }

    public boolean hasWarnings() {
        return warningBanners().count() > 0;
    }

    public void waitForChartsLoaded() {
        chartWidgets.waitUntilVisible();
    }
}
