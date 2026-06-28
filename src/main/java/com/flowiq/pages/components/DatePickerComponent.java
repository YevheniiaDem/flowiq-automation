package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Reusable date picker trigger and calendar panel.
 */
public class DatePickerComponent extends BaseComponent {

    private final Locator trigger;

    public DatePickerComponent(Page page, String testId) {
        super(page);
        this.trigger = byTestIdOr(testId, "button[aria-haspopup='dialog']");
    }

    public Locator trigger() {
        return trigger;
    }

    public Locator calendar() {
        return page.locator("[role='dialog'] [role='grid'], .rdp");
    }

    public void open() {
        trigger.click();
        waitForVisible(calendar());
    }

    public void selectDay(String dayLabel) {
        calendar().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(dayLabel)).click();
    }
}
