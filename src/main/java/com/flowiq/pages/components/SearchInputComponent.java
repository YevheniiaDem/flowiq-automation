package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Reusable search field with fill/clear actions.
 */
public class SearchInputComponent extends BaseComponent {

    private final Locator input;

    public SearchInputComponent(Page page, String testId) {
        this(page, testId, null);
    }

    public SearchInputComponent(Page page, String testId, String cssFallback) {
        super(page);
        this.input = cssFallback == null ? byTestId(testId) : byTestIdOr(testId, cssFallback);
    }

    public Locator input() {
        return input;
    }

    public void fill(String query) {
        input.fill(query);
    }

    public void clear() {
        input.clear();
    }
}
