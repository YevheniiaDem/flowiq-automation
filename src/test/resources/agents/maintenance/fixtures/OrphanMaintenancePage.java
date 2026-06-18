package com.flowiq.pages;

import com.microsoft.playwright.Page;

public class OrphanMaintenancePage {
    public OrphanMaintenancePage(Page page) {
        page.locator("//div[@class='orphan']").click();
        page.locator("section li:nth-child(2)").count();
    }
}
