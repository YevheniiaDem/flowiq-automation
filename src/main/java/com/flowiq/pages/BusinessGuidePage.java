package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class BusinessGuidePage extends BasePage {

  public BusinessGuidePage(Page page) {
    super(page);
  }

  @Override
  protected String getPath() {
    return UiPaths.BUSINESS_GUIDE;
  }

  @Override
  protected String getPageTestId() {
    return TestIds.BUSINESS_GUIDE_PAGE;
  }

  public Locator searchInput() {
    return byTestId(TestIds.BUSINESS_GUIDE_SEARCH);
  }

  public BusinessGuidePage search(String query) {
    searchInput().fill(query);
    return this;
  }

  public BusinessGuidePage clearSearch() {
    searchInput().clear();
    return this;
  }

  public Locator searchResults() {
    return page.locator("[data-testid='business-guide-search-results'], .absolute.left-0.right-0");
  }

  public boolean hasSearchResults() {
    return searchResults().isVisible();
  }
}
