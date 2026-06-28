package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.FileUploadComponent;
import com.flowiq.pages.components.TableComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

public class ImportsPage extends BasePage {

    private final FileUploadComponent fileUpload;
    private final TableComponent historyTableComponent;

    public ImportsPage(Page page) {
        super(page);
        this.fileUpload = new FileUploadComponent(page, TestIds.IMPORTS_FILE_INPUT, "input[type='file']");
        this.historyTableComponent = new TableComponent(page, TestIds.IMPORTS_HISTORY_TABLE);
    }

    @Override
    protected String getPath() {
        return UiPaths.IMPORTS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.IMPORTS_PAGE;
    }

    public Locator uploadZone() {
        return byTestId(TestIds.IMPORTS_UPLOAD_ZONE);
    }

    public Locator browseButton() {
        return byTestId(TestIds.IMPORTS_BROWSE_BTN);
    }

    public Locator fileInput() {
        return fileUpload.input();
    }

    public Locator historyTable() {
        return historyTableComponent.root();
    }

    public Locator historyRows() {
        return historyTableComponent.rows();
    }

    public ImportsPage uploadFile(Path filePath) {
        fileUpload.upload(filePath);
        return this;
    }

    public ImportsPage clickBrowse() {
        browseButton().click();
        return this;
    }

    public int getHistoryRowCount() {
        return historyTableComponent.rowCount();
    }
}
