package com.flowiq.pages.base;

import com.flowiq.constants.TestIds;
import com.flowiq.pages.components.HeaderComponent;
import com.flowiq.pages.components.SidebarComponent;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

public abstract class BasePage extends AbstractPage {

    protected final SidebarComponent sidebar;
    protected final HeaderComponent header;

    protected BasePage(Page page) {
        super(page);
        this.sidebar = new SidebarComponent(page);
        this.header = new HeaderComponent(page);
    }

    @SuppressWarnings("unchecked")
    public <T extends BasePage> T open() {
        page.navigate(getPath());
        waitForPageLoaded();
        return (T) this;
    }

    public void waitForPageLoaded() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        waitForVisible(byTestId(getPageTestId()));
        waitForAuthLayout();
    }

    protected void waitForAuthLayout() {
        sidebar.waitUntilVisible();
    }

    public boolean isLoaded() {
        return byTestId(getPageTestId()).isVisible();
    }

    public SidebarComponent sidebar() {
        return sidebar;
    }

    public HeaderComponent header() {
        return header;
    }

    protected abstract String getPath();

    protected abstract String getPageTestId();
}
