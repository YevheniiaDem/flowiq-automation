package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.DropdownComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class NotificationsPage extends BasePage {

    public NotificationsPage(Page page) {
        super(page);
    }

    @Override
    protected String getPath() {
        return UiPaths.NOTIFICATIONS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.NOTIFICATIONS_PAGE;
    }

    public Locator markAllReadButton() {
        return byTestId(TestIds.NOTIFICATIONS_MARK_ALL_READ_BTN);
    }

    public Locator filters() {
        return byTestId(TestIds.NOTIFICATIONS_FILTERS);
    }

    public Locator filterButton(String filter) {
        return byTestId(TestIds.notificationFilter(filter));
    }

    public Locator list() {
        return byTestId(TestIds.NOTIFICATIONS_LIST);
    }

    public Locator notificationItems() {
        return list().locator("section .space-y-2 > *");
    }

    public NotificationsPage markAllAsRead() {
        if (markAllReadButton().isVisible()) {
            markAllReadButton().click();
        }
        return this;
    }

    public NotificationsPage selectFilter(String filter) {
        new DropdownComponent(page, filterButton(filter)).open();
        return this;
    }

    public boolean isEmpty() {
        return list().locator("p.text-muted-foreground").isVisible();
    }

    public Locator unreadCards() {
        return list().locator(".shadow-sm");
    }

    public NotificationsPage markFirstUnreadAsRead() {
        Locator unread = unreadCards().first();
        if (unread.isVisible()) {
            unread.click();
        }
        return this;
    }

    public int getUnreadCardCount() {
        return unreadCards().count();
    }
}
