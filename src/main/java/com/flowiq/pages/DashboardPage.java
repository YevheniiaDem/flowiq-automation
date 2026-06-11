package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DashboardPage extends BasePage {

  public DashboardPage(Page page) {
    super(page);
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
    return byTestIdOr(TestIds.MAIN_CONTENT, "main");
  }

  public int getStatCardCount() {
    return statsGrid().locator("> *").count();
  }

  public void waitForStatsLoaded() {
    waitForVisible(statsGrid());
  }
}
