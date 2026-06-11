package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

public class ImportsPage extends BasePage {

  public ImportsPage(Page page) {
    super(page);
  }

  @Override
  protected String getPath() {
    return UiPaths.IMPORTS;
  }

  @Override
  protected String getPageTestId() {
    return TestIds.IMPORTS_PAGE;
  }

  public Locator uploadZone() {
    return byTestId(TestIds.IMPORTS_UPLOAD_ZONE);
  }

  public Locator browseButton() {
    return byTestId(TestIds.IMPORTS_BROWSE_BTN);
  }

  public Locator fileInput() {
    return byTestIdOr(TestIds.IMPORTS_FILE_INPUT, "input[type='file']");
  }

  public Locator historyTable() {
    return byTestId(TestIds.IMPORTS_HISTORY_TABLE);
  }

  public Locator historyRows() {
    return historyTable().locator("tbody tr");
  }

  public ImportsPage uploadFile(Path filePath) {
    fileInput().first().setInputFiles(filePath);
    return this;
  }

  public ImportsPage clickBrowse() {
    browseButton().click();
    return this;
  }

  public int getHistoryRowCount() {
    return historyRows().count();
  }
}
