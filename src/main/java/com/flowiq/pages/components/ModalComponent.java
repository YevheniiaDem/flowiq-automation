package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Reusable dialog/modal wrapper. Supports test-id roots and ARIA dialog fallbacks.
 */
public class ModalComponent extends BaseComponent {

    private final Locator root;

    public ModalComponent(Page page, String testId) {
        super(page);
        this.root = byTestId(testId);
    }

    public ModalComponent(Page page, Locator root) {
        super(page);
        this.root = root;
    }

    public static ModalComponent byRoleDialog(Page page) {
        return new ModalComponent(page, page.locator("[role='dialog']"));
    }

    public Locator root() {
        return root;
    }

    public Locator field(String testId) {
        return byTestId(testId);
    }

    public Locator fieldByIndex(int index) {
        return root.locator("input").nth(index);
    }

    public Locator submitButton() {
        return root.locator("button[type='submit']");
    }

    public void waitUntilVisible() {
        waitForVisible(root);
    }

    public void waitUntilHidden() {
        waitForHidden(root);
    }

    public void fillField(String testId, String value) {
        field(testId).fill(value);
    }

    public void submit() {
        submitButton().click();
        waitUntilHidden();
    }

    public void submit(String submitTestId) {
        byTestId(submitTestId).click();
        waitUntilHidden();
    }
}
