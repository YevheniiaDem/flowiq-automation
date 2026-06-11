package com.flowiq.base;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.assertj.core.api.Assertions;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public final class UiAssertions {

    private UiAssertions() {
    }

    public static void assertPageTitle(Page page, String expectedTitle) {
        Assertions.assertThat(page.title())
                .as("Page title")
                .isEqualTo(expectedTitle);
    }

    public static void assertPageUrlContains(Page page, String expectedFragment) {
        Assertions.assertThat(page.url())
                .as("Page URL")
                .contains(expectedFragment);
    }

    public static void assertElementVisible(Locator locator) {
        Assertions.assertThat(locator.isVisible())
                .as("Element visibility")
                .isTrue();
    }

    public static void assertElementText(Locator locator, String expectedText) {
        Assertions.assertThat(locator.textContent())
                .as("Element text")
                .contains(expectedText);
    }

    public static void assertElementEnabled(Locator locator) {
        Assertions.assertThat(locator.isEnabled())
                .as("Element enabled state")
                .isTrue();
    }

    public static void waitForPageLoad(Page page) {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public static void waitUntilVisible(Locator locator, long timeoutSeconds) {
        await()
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> locator.isVisible());
    }
}
