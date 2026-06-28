package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.DropdownComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class SettingsPage extends BasePage {

    public SettingsPage(Page page) {
        super(page);
    }

    @Override
    protected String getPath() {
        return UiPaths.SETTINGS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.SETTINGS_PAGE;
    }

    @Override
    public SettingsPage open() {
        super.open();
        return this;
    }

    public Locator pageRoot() {
        return byTestId(TestIds.SETTINGS_PAGE);
    }

    public Locator helpLearnCenter() {
        return byTestId(TestIds.HELP_LEARN_CENTER);
    }

    public Locator tab(String tabName) {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(tabName));
    }

    public SettingsPage openTab(String tabName) {
        new DropdownComponent(page, tab(tabName)).open();
        return this;
    }

    public SettingsPage openTabByIndex(int index) {
        page.locator("[data-testid='settings-page'] button[type='button']").nth(index).click();
        return this;
    }

    public Locator helpCenterItem(String guideId) {
        return byTestId("help-center-" + guideId);
    }

    public Locator demoWorkspaceBanner() {
        return byTestId(TestIds.DEMO_WORKSPACE_BANNER);
    }
}
