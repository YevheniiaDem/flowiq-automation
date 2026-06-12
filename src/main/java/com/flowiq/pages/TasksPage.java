package com.flowiq.pages;

import com.flowiq.constants.TestIds;
import com.flowiq.constants.UiPaths;
import com.flowiq.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class TasksPage extends BasePage {

  public TasksPage(Page page) {
    super(page);
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
    return byTestId(TestIds.TASKS_SEARCH);
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
    searchInput().fill(query);
    return this;
  }

  public TasksPage selectSection(String section) {
    sectionButton(section).click();
    return this;
  }

  public int getVisibleTaskCount() {
    return taskCards().count();
  }

  public Locator taskDialog() {
    return page.locator("[role='dialog']");
  }

  public TasksPage createTask(String title, String description) {
    clickAddTask();
    waitForVisible(taskDialog());
    taskDialog().locator("input").first().fill(title);
    if (description != null && !description.isBlank()) {
      taskDialog().locator("input").nth(1).fill(description);
    }
    taskDialog().locator("button[type='submit']").click();
    waitForHidden(taskDialog());
    return this;
  }

  public boolean containsTask(String title) {
    return page.getByText(title).first().isVisible();
  }
}
