package com.flowiq.pages.components;

import com.flowiq.constants.UiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Recharts chart container wrapper shared by dashboard and forecasts pages.
 */
public class ChartsComponent extends BaseComponent {

    public ChartsComponent(Page page) {
        super(page);
    }

    public Locator containers() {
        return page.locator(UiLocators.RECHARTS_CONTAINER);
    }

    public int count() {
        return containers().count();
    }

    public void waitUntilVisible() {
        waitForVisible(containers().first());
    }
}
