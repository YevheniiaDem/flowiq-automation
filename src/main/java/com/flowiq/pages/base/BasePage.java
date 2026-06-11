package com.flowiq.pages.base;

import com.flowiq.constants.TestIds;
import com.flowiq.pages.components.SidebarNavigation;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public abstract class BasePage extends AbstractPage {

  protected final SidebarNavigation sidebar;

  protected BasePage(Page page) {
    super(page);
    this.sidebar = new SidebarNavigation(page);
  }

  @SuppressWarnings("unchecked")
  public <T extends BasePage> T open() {
    page.navigate(getPath());
    waitForPageLoaded();
    return (T) this;
  }

  public void waitForPageLoaded() {
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    waitForVisible(byTestId(getPageTestId()));
    waitForAuthLayout();
  }

  protected void waitForAuthLayout() {
    waitForVisible(byTestId(TestIds.SIDEBAR));
  }

  public boolean isLoaded() {
    return byTestId(getPageTestId()).isVisible();
  }

  public SidebarNavigation sidebar() {
    return sidebar;
  }

  protected abstract String getPath();

  protected abstract String getPageTestId();
}
