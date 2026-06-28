package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.FileUploadComponent;
import com.flowiq.pages.components.ModalComponent;
import com.flowiq.pages.components.SearchInputComponent;
import com.flowiq.pages.components.TableComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

public class TransactionsPage extends BasePage {

    private final SearchInputComponent search;
    private final TableComponent transactionTable;
    private final FileUploadComponent importUpload;
    private final ModalComponent transactionForm;

    public TransactionsPage(Page page) {
        super(page);
        this.search = new SearchInputComponent(page, TestIds.TRANSACTIONS_SEARCH, "input[type='search']");
        this.transactionTable = new TableComponent(page, TestIds.TRANSACTIONS_TABLE);
        this.importUpload = new FileUploadComponent(page, TestIds.TRANSACTIONS_IMPORT_INPUT, "input[type='file']");
        this.transactionForm = new ModalComponent(page, TestIds.TRANSACTION_FORM_MODAL);
    }

    @Override
    protected String getPath() {
        return UiPaths.TRANSACTIONS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.TRANSACTIONS_PAGE;
    }

    public Locator addButton() {
        return byTestId(TestIds.TRANSACTIONS_ADD_BTN);
    }

    public Locator importButton() {
        return byTestId(TestIds.TRANSACTIONS_IMPORT_BTN);
    }

    public Locator exportButton() {
        return byTestId(TestIds.TRANSACTIONS_EXPORT_BTN);
    }

    public Locator searchInput() {
        return search.input();
    }

    public Locator filters() {
        return byTestId(TestIds.TRANSACTIONS_FILTERS);
    }

    public Locator table() {
        return transactionTable.root();
    }

    public Locator tableRows() {
        return transactionTable.rows();
    }

    public TransactionsPage clickAddTransaction() {
        addButton().click();
        return this;
    }

    public TransactionsPage search(String query) {
        search.fill(query);
        return this;
    }

    public TransactionsPage clearSearch() {
        search.clear();
        return this;
    }

    public TransactionsPage importCsv(Path filePath) {
        importUpload.upload(filePath);
        return this;
    }

    public Locator fileInput() {
        return importUpload.input();
    }

    public TransactionsPage exportCsv() {
        exportButton().click();
        return this;
    }

    public int getTransactionRowCount() {
        return transactionTable.rowCount();
    }

    public TransactionsPage createTransaction(String amount, String description) {
        clickAddTransaction();
        transactionForm.waitUntilVisible();
        transactionForm.fillField(TestIds.TRANSACTION_FORM_AMOUNT, amount);
        transactionForm.fillField(TestIds.TRANSACTION_FORM_DESCRIPTION, description);
        transactionForm.submit(TestIds.TRANSACTION_FORM_SUBMIT);
        return this;
    }

    public boolean containsTransactionText(String text) {
        return transactionTable.containsText(text);
    }
}
