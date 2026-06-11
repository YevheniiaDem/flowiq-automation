package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ReportsPage extends BasePage {

  public ReportsPage(Page page) {
    super(page);
  }

  @Override
  protected String getPath() {
    return UiPaths.REPORTS;
  }

  @Override
  protected String getPageTestId() {
    return TestIds.REPORTS_PAGE;
  }

  public Locator generateButton() {
    return byTestId(TestIds.REPORTS_GENERATE_BTN);
  }

  public ReportsPage openGenerateDialog() {
    generateButton().click();
    waitForVisible(byTestId(TestIds.REPORTS_GENERATE_DIALOG));
    return this;
  }

  public ReportsPage submitGenerateDialog() {
    byTestId(TestIds.REPORTS_GENERATE_SUBMIT).click();
    waitForHidden(byTestId(TestIds.REPORTS_GENERATE_DIALOG));
    return this;
  }

  public ReportsPage generateDefaultReport() {
    openGenerateDialog();
    submitGenerateDialog();
    return this;
  }
}
