package com.flowiq.pages.base;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractPage {

  protected final Page page;

  protected Locator byTestId(String testId) {
    return page.getByTestId(testId);
  }

  protected Locator byTestIdOr(String testId, String cssFallback) {
    return page.getByTestId(testId).or(page.locator(cssFallback));
  }

  protected void waitForDomReady() {
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
  }

  protected void waitForVisible(Locator locator) {
    locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
  }

  protected void waitForHidden(Locator locator) {
    locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
  }
}
