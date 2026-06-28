package com.flowiq.pages.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Reusable data table wrapper with row access helpers.
 */
public class TableComponent extends BaseComponent {

    private final Locator root;

    public TableComponent(Page page, String testId) {
        super(page);
        this.root = byTestId(testId);
    }

    public TableComponent(Page page, Locator root) {
        super(page);
        this.root = root;
    }

    public static TableComponent firstOnPage(Page page) {
        return new TableComponent(page, page.locator("table").first());
    }

    public Locator root() {
        return root;
    }

    public Locator rows() {
        return root.locator("tbody tr");
    }

    public Locator body() {
        return root.locator("tbody");
    }

    public int rowCount() {
        return rows().count();
    }

    public void waitUntilVisible() {
        waitForVisible(root);
    }

    public boolean containsText(String text) {
        return root.getByText(text).isVisible();
    }
}
