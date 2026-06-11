package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

public class TransactionsPage extends BasePage {

  public TransactionsPage(Page page) {
    super(page);
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
    return byTestIdOr(TestIds.TRANSACTIONS_SEARCH, "input[type='search']");
  }

  public Locator filters() {
    return byTestId(TestIds.TRANSACTIONS_FILTERS);
  }

  public Locator table() {
    return byTestId(TestIds.TRANSACTIONS_TABLE);
  }

  public Locator tableRows() {
    return table().locator("tbody tr");
  }

  public TransactionsPage clickAddTransaction() {
    addButton().click();
    return this;
  }

  public TransactionsPage search(String query) {
    searchInput().fill(query);
    return this;
  }

  public TransactionsPage clearSearch() {
    searchInput().clear();
    return this;
  }

  public TransactionsPage importCsv(Path filePath) {
    fileInput().setInputFiles(filePath);
    return this;
  }

  public Locator fileInput() {
    return byTestIdOr(TestIds.TRANSACTIONS_IMPORT_INPUT, "input[type='file']").first();
  }

  public TransactionsPage exportCsv() {
    exportButton().click();
    return this;
  }

  public int getTransactionRowCount() {
    return tableRows().count();
  }

  public TransactionsPage createTransaction(String amount, String description) {
    clickAddTransaction();
    waitForVisible(byTestId(TestIds.TRANSACTION_FORM_MODAL));
    byTestId(TestIds.TRANSACTION_FORM_AMOUNT).fill(amount);
    byTestId(TestIds.TRANSACTION_FORM_DESCRIPTION).fill(description);
    byTestId(TestIds.TRANSACTION_FORM_SUBMIT).click();
    waitForHidden(byTestId(TestIds.TRANSACTION_FORM_MODAL));
    return this;
  }

  public boolean containsTransactionText(String text) {
    return table().getByText(text).isVisible();
  }
}
