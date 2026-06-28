package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.AbstractPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginPage extends AbstractPage {

    public LoginPage(Page page) {
        super(page);
    }

    private Locator emailInput() {
        return byTestIdOr(TestIds.LOGIN_EMAIL, "#email");
    }

    private Locator passwordInput() {
        return byTestIdOr(TestIds.LOGIN_PASSWORD, "#password");
    }

    private Locator submitButton() {
        return byTestIdOr(TestIds.LOGIN_SUBMIT, "button[type='submit']");
    }

    public Locator errorMessage() {
        return byTestId(TestIds.LOGIN_ERROR);
    }

    public Locator pageRoot() {
        return byTestId(TestIds.LOGIN_PAGE);
    }

    public LoginPage open() {
        page.navigate(UiPaths.LOGIN);
        waitForDomReady();
        waitForVisible(pageRoot());
        return this;
    }

    public LoginPage enterEmail(String email) {
        emailInput().fill(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        passwordInput().fill(password);
        return this;
    }

    public void submit() {
        submitButton().click();
    }

    public void login(String email, String password) {
        open();
        enterEmail(email);
        enterPassword(password);
        submit();
    }

    public boolean isDisplayed() {
        return emailInput().isVisible() && passwordInput().isVisible();
    }

    public boolean hasError() {
        return errorMessage().isVisible();
    }

    public String getErrorText() {
        return errorMessage().textContent();
    }
}
