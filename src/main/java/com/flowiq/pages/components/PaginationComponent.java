package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Reusable pagination control for list/table pages.
 */
public class PaginationComponent extends BaseComponent {

    public PaginationComponent(Page page) {
        super(page);
    }

    public Locator root() {
        return byTestIdOr("pagination", "nav[aria-label='pagination']");
    }

    public Locator pageButton(int pageNumber) {
        return root().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(String.valueOf(pageNumber)));
    }

    public Locator nextButton() {
        return root().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Next"));
    }

    public Locator previousButton() {
        return root().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Previous"));
    }

    public void goToPage(int pageNumber) {
        pageButton(pageNumber).click();
    }
}
