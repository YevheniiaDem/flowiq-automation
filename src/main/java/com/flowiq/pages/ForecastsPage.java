package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ForecastsPage extends BasePage {

  public ForecastsPage(Page page) {
    super(page);
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
    return page.locator("[data-testid='forecasts-warnings'], .space-y-2 > div");
  }

  public int getSummaryCardCount() {
    return summaryCards().locator("> *").count();
  }

  public void waitForSummaryLoaded() {
    waitForVisible(summaryCards());
  }

  public Locator charts() {
    return page.locator(".recharts-responsive-container");
  }

  public int getChartCount() {
    return charts().count();
  }

  public Locator warningBanners() {
    return page.locator(".rounded-xl.border.p-3.backdrop-blur-sm");
  }

  public boolean hasWarnings() {
    return warningBanners().count() > 0;
  }

  public void waitForChartsLoaded() {
    waitForVisible(charts().first());
  }
}
