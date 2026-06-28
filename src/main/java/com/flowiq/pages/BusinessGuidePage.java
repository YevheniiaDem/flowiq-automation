package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.SearchInputComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class BusinessGuidePage extends BasePage {

    private final SearchInputComponent search;

    public BusinessGuidePage(Page page) {
        super(page);
        this.search = new SearchInputComponent(page, TestIds.BUSINESS_GUIDE_SEARCH);
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
        return search.input();
    }

    public BusinessGuidePage search(String query) {
        search.fill(query);
        return this;
    }

    public BusinessGuidePage clearSearch() {
        search.clear();
        return this;
    }

    public Locator searchResults() {
        return page.locator("[data-testid='business-guide-search-results'], .absolute.left-0.right-0");
    }

    public boolean hasSearchResults() {
        return searchResults().isVisible();
    }

    public Locator articleLinks() {
        return page.locator("a[href*='/business-guide/articles/']");
    }

    public BusinessGuidePage openFirstArticle() {
        articleLinks().first().click();
        waitForDomReady();
        return this;
    }

    public boolean isArticleDetailVisible() {
        return page.locator("article").isVisible();
    }
}
