package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Toast/notification snackbar component (Sonner and ARIA status fallbacks).
 */
public class ToastComponent extends BaseComponent {

    public ToastComponent(Page page) {
        super(page);
    }

    public Locator toasts() {
        return page.locator("[data-sonner-toast], [role='status']");
    }

    public Locator first() {
        return toasts().first();
    }

    public void waitUntilVisible() {
        waitForVisible(first());
    }

    public void waitUntilHidden() {
        waitForHidden(first());
    }

    public boolean isVisible() {
        return first().isVisible();
    }
}
