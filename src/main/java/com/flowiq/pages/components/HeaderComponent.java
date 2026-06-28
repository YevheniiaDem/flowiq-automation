package com.flowiq.pages.components;

import com.flowiq.constants.TestIds;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Application shell header and main content region.
 */
public class HeaderComponent extends BaseComponent {

    public HeaderComponent(Page page) {
        super(page);
    }

    public Locator mainContent() {
        return byTestIdOr(TestIds.MAIN_CONTENT, "main");
    }

    public void waitForMainContent() {
        waitForVisible(mainContent());
    }
}
