package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Reusable dropdown/select trigger with option selection.
 */
public class DropdownComponent extends BaseComponent {

    private final Locator trigger;

    public DropdownComponent(Page page, Locator trigger) {
        super(page);
        this.trigger = trigger;
    }

    public DropdownComponent(Page page, String testId) {
        super(page);
        this.trigger = byTestId(testId);
    }

    public Locator trigger() {
        return trigger;
    }

    public void open() {
        trigger.click();
    }

    public void selectOption(String optionName) {
        open();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(optionName)).click();
    }

    public void selectByTestId(String optionTestId) {
        open();
        byTestId(optionTestId).click();
    }
}
