package com.flowiq.api.regression.tasks;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.api.regression.support.RegressionDataProviders;
import com.flowiq.auth.TokenManager;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.factories.builders.TaskRequestBuilder;
import com.flowiq.models.tasks.TaskListResponse;
import com.flowiq.models.tasks.TaskPageResponse;
import com.flowiq.models.tasks.TaskResponse;
import com.flowiq.models.tasks.TaskStatus;
import com.flowiq.support.TestCleanupManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Tasks")
public class TasksRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Task CRUD")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /tasks creates a new task")
    public void shouldCreateTask() {
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Task CRUD")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /tasks/{id} updates task fields")
    public void shouldUpdateTask() {
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        var updateRequest = TaskRequestBuilder.custom()
                .title(created.getTitle() + " updated")
                .dueTomorrow()
                .toUpdateRequest();
        TaskResponse updated = taskService.update(created.getId(), updateRequest);

        assertThat(updated.getTitle()).endsWith("updated");
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Task CRUD")
    @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /tasks/{id} removes the task")
    public void shouldDeleteTask() {
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());

        taskService.deleteById(created.getId());

        RegressionAssertions.assertNotFound(
                taskService.attemptUpdate(created.getId(), TaskRequestBuilder.custom().toUpdateRequest()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Complete business rule")
    @Severity(SeverityLevel.BLOCKER)
    @Description("PUT /tasks/{id}/complete marks task as completed with timestamp")
    public void shouldCompleteTask() {
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        TaskResponse completed = taskService.complete(created.getId());

        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Complete business rule")
    @Severity(SeverityLevel.NORMAL)
    @Description("Completed task appears in grouped completed bucket")
    public void shouldIncludeCompletedTaskInGroupedResponse() {
        String title = "Completed task " + System.currentTimeMillis();
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().title(title).dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        taskService.complete(created.getId());

        TaskListResponse grouped = taskService.getGrouped();
        assertThat(grouped.getCompleted())
                .anyMatch(task -> created.getId().equals(task.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Grouped tasks")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /tasks/grouped returns today, upcoming, overdue and completed buckets")
    public void shouldReturnGroupedTasks() {
        TaskListResponse grouped = taskService.getGrouped();

        assertThat(grouped.getToday()).isNotNull();
        assertThat(grouped.getUpcoming()).isNotNull();
        assertThat(grouped.getOverdue()).isNotNull();
        assertThat(grouped.getCompleted()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Today endpoint")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /tasks/today returns tasks due today")
    public void shouldReturnTodayTasks() {
        String title = "Today task " + System.currentTimeMillis();
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().title(title).dueToday().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        assertThat(taskService.getToday())
                .anyMatch(task -> created.getId().equals(task.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Upcoming endpoint")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /tasks/upcoming returns tasks due in the future")
    public void shouldReturnUpcomingTasks() {
        String title = "Upcoming task " + System.currentTimeMillis();
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().title(title).dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        assertThat(taskService.getUpcoming())
                .anyMatch(task -> created.getId().equals(task.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"},
            dataProvider = "paginationPages", dataProviderClass = RegressionDataProviders.class)
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Tasks list supports page parameter")
    public void shouldPaginateTasksByPage(int page) {
        TaskPageResponse response = taskService.list(Map.of("page", page, "size", 10));

        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getContent()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"},
            dataProvider = "pageSizes", dataProviderClass = RegressionDataProviders.class)
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Tasks list supports size parameter")
    public void shouldPaginateTasksByPageSize(int size) {
        TaskPageResponse response = taskService.list(Map.of("page", 0, "size", size));

        assertThat(response.getSize()).isEqualTo(size);
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Search")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Tasks list supports search by title")
    public void shouldSearchTasksByTitle() {
        String uniqueTitle = "Search task " + System.currentTimeMillis();
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().title(uniqueTitle).dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        TaskPageResponse results = taskService.list(Map.of("search", uniqueTitle, "page", 0, "size", 20));

        assertThat(results.getContent())
                .anyMatch(task -> created.getId().equals(task.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"},
            dataProvider = "taskSections", dataProviderClass = RegressionDataProviders.class)
    @Story("Section filters")
    @Severity(SeverityLevel.NORMAL)
    @Description("Tasks list supports section filter parameter")
    public void shouldFilterTasksBySection(String section) {
        TaskPageResponse page = taskService.list(Map.of("section", section, "page", 0, "size", 20));

        assertThat(page.getContent()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated task list request is rejected")
    public void shouldRejectUnauthorizedListAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(taskService.fetchListUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated grouped tasks request is rejected")
    public void shouldRejectUnauthorizedGroupedAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(taskService.fetchGroupedUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated task create request is rejected")
    public void shouldRejectUnauthorizedCreateAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(
                taskService.attemptCreate(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build()));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated task complete request is rejected")
    public void shouldRejectUnauthorizedCompleteAccess() {
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(taskService.attemptComplete(created.getId()));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Validation errors")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid task payload returns 400/422")
    public void shouldRejectInvalidTaskPayload() {
        RegressionAssertions.assertValidationError(
                taskService.attemptCreate(TestDataFactory.invalidTaskRequest()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Updating non-existent task returns 404")
    public void shouldRejectUpdateForInvalidId() {
        RegressionAssertions.assertNotFound(
                taskService.attemptUpdate(INVALID_ID, TaskRequestBuilder.custom().toUpdateRequest()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Completing non-existent task returns 404")
    public void shouldRejectCompleteForInvalidId() {
        RegressionAssertions.assertNotFound(taskService.attemptComplete(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Deleting non-existent task returns 404")
    public void shouldRejectDeleteForInvalidId() {
        RegressionAssertions.assertNotFound(taskService.attemptDeleteById(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Overdue logic")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Past-due tasks appear in overdue bucket")
    public void shouldIncludeOverdueTasksInGroupedResponse() {
        String title = "Overdue task " + System.currentTimeMillis();
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().title(title).duePast(5).build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        TaskListResponse grouped = taskService.getGrouped();

        assertThat(grouped.getOverdue())
                .anyMatch(task -> created.getId().equals(task.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Task CRUD")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can create, update, complete and delete a task")
    public void shouldExecuteFullTaskLifecycle() {
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        TaskResponse updated = taskService.update(created.getId(),
                TaskRequestBuilder.custom().title(created.getTitle() + " v2").dueTomorrow().toUpdateRequest());
        assertThat(updated.getTitle()).endsWith("v2");

        TaskResponse completed = taskService.complete(created.getId());
        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        taskService.deleteById(created.getId());
        TestCleanupManager.clear();

        RegressionAssertions.assertNotFound(taskService.attemptComplete(created.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("Task suggestions")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /tasks/suggestions returns task suggestions")
    public void shouldReturnTaskSuggestions() {
        assertThat(taskService.getSuggestions()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "tasks"})
    @Story("List tasks")
    @Severity(SeverityLevel.NORMAL)
    @Description("Default tasks list returns paginated content")
    public void shouldListTasksWithDefaults() {
        TaskPageResponse page = taskService.list();

        assertThat(page.getContent()).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(0);
    }
}
