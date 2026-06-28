package com.flowiq.pages;

import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.AbstractPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class RegisterPage extends AbstractPage {

    public RegisterPage(Page page) {
        super(page);
    }

    public RegisterPage open() {
        page.navigate(UiPaths.REGISTER);
        waitForDomReady();
        return this;
    }

    public RegisterPage enterName(String name) {
        page.locator("#name").fill(name);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        page.locator("#email").fill(email);
        return this;
    }

    public RegisterPage enterCompany(String company) {
        page.locator("#company").fill(company);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        page.locator("#password").fill(password);
        return this;
    }

    public void submit() {
        page.locator("button[type='submit']").click();
    }

    public Locator errorBanner() {
        return page.locator(".text-destructive").first();
    }

    public boolean isDisplayed() {
        return page.locator("#email").isVisible() && page.locator("#password").isVisible();
    }
}
