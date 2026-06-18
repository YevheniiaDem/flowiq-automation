package com.flowiq.pages;

import com.microsoft.playwright.Page;

public class ImportsPage {
    public ImportsPage(Page page) {
        page.locator("//button[@type='submit']").click();
        page.locator("section .space-y-2 > div").count();
    }
}
