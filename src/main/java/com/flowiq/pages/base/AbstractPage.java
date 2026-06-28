package com.flowiq.pages.base;

import com.flowiq.pages.components.BaseComponent;
import com.microsoft.playwright.Page;

public abstract class AbstractPage extends BaseComponent {

    protected AbstractPage(Page page) {
        super(page);
    }
}
