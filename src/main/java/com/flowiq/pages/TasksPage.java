package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.flowiq.pages.components.DropdownComponent;
import com.flowiq.pages.components.ModalComponent;
import com.flowiq.pages.components.SearchInputComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class TasksPage extends BasePage {

    private final SearchInputComponent search;
    private final ModalComponent taskDialog;

    public TasksPage(Page page) {
        super(page);
        this.search = new SearchInputComponent(page, TestIds.TASKS_SEARCH);
        this.taskDialog = ModalComponent.byRoleDialog(page);
    }

    @Override
    protected String getPath() {
        return UiPaths.TASKS;
    }

    @Override
    protected String getPageTestId() {
        return TestIds.TASKS_PAGE;
    }

    public Locator addButton() {
        return byTestId(TestIds.TASKS_ADD_BTN);
    }

    public Locator searchInput() {
        return search.input();
    }

    public Locator filters() {
        return byTestId(TestIds.TASKS_FILTERS);
    }

    public Locator sectionButton(String section) {
        return byTestId(TestIds.tasksSection(section));
    }

    public Locator taskCards() {
        return page.locator("section .space-y-2 > div");
    }

    public TasksPage clickAddTask() {
        addButton().click();
        return this;
    }

    public TasksPage search(String query) {
        search.fill(query);
        return this;
    }

    public TasksPage selectSection(String section) {
        new DropdownComponent(page, sectionButton(section)).open();
        return this;
    }

    public int getVisibleTaskCount() {
        return taskCards().count();
    }

    public Locator taskDialog() {
        return taskDialog.root();
    }

    public TasksPage createTask(String title, String description) {
        clickAddTask();
        taskDialog.waitUntilVisible();
        taskDialog.fieldByIndex(0).fill(title);
        if (description != null && !description.isBlank()) {
            taskDialog.fieldByIndex(1).fill(description);
        }
        taskDialog.submit();
        return this;
    }

    public boolean containsTask(String title) {
        return page.getByText(title).first().isVisible();
    }
}
