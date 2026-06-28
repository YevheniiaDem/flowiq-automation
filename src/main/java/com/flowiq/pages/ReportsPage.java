package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.ModalComponent;
import com.flowiq.pages.components.TableComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ReportsPage extends BasePage {

    private final ModalComponent generateDialog;
    private final TableComponent historyTableComponent;

    public ReportsPage(Page page) {
        super(page);
        this.generateDialog = new ModalComponent(page, TestIds.REPORTS_GENERATE_DIALOG);
        this.historyTableComponent = TableComponent.firstOnPage(page);
    }

    @Override
    protected String getPath() {
        return UiPaths.REPORTS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.REPORTS_PAGE;
    }

    public Locator generateButton() {
        return byTestId(TestIds.REPORTS_GENERATE_BTN);
    }

    public ReportsPage openGenerateDialog() {
        generateButton().click();
        generateDialog.waitUntilVisible();
        return this;
    }

    public ReportsPage submitGenerateDialog() {
        generateDialog.submit(TestIds.REPORTS_GENERATE_SUBMIT);
        return this;
    }

    public ReportsPage generateDefaultReport() {
        openGenerateDialog();
        submitGenerateDialog();
        return this;
    }

    public Locator historyTable() {
        return historyTableComponent.root();
    }

    public Locator historyRows() {
        return historyTableComponent.rows();
    }

    public int getHistoryRowCount() {
        return historyTableComponent.rowCount();
    }

    public void waitForHistoryLoaded() {
        historyTableComponent.waitUntilVisible();
    }
}
