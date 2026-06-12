package com.flowiq.api.integration.tasks;

import com.flowiq.api.integration.base.BaseApiIntegrationTest;
import com.flowiq.api.integration.support.IntegrationAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.factories.builders.TaskRequestBuilder;
import com.flowiq.models.tasks.TaskListResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Tasks")
public class TasksIntegrationTest extends BaseApiIntegrationTest {

    @Test(groups = {"api-integration", "api", "tasks"})
    @Story("Create task")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /tasks creates a new task")
    public void shouldCreateTask() {
        TaskResponse created = taskService.create(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test(groups = {"api-integration", "api", "tasks"})
    @Story("Update task")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /tasks/{id} updates task fields")
    public void shouldUpdateTask() {
        TaskResponse created = taskService.create(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        var builder = TaskRequestBuilder.custom().title(created.getTitle() + " updated").dueTomorrow();
        TaskResponse updated = taskService.update(created.getId(), builder.toUpdateRequest());

        assertThat(updated.getTitle()).endsWith("updated");
    }

    @Test(groups = {"api-integration", "api", "tasks"})
    @Story("Complete task")
    @Severity(SeverityLevel.BLOCKER)
    @Description("PUT /tasks/{id}/complete marks task as completed")
    public void shouldCompleteTask() {
        TaskResponse created = taskService.create(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        TaskResponse completed = taskService.complete(created.getId());

        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test(groups = {"api-integration", "api", "tasks"})
    @Story("Delete task")
    @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /tasks/{id} removes the task")
    public void shouldDeleteTask() {
        TaskResponse created = taskService.create(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());

        taskService.deleteById(created.getId());

        IntegrationAssertions.assertNotFound(
                taskService.attemptUpdate(created.getId(), TaskRequestBuilder.custom().toUpdateRequest()));
    }

    @Test(groups = {"api-integration", "api", "tasks"})
    @Story("Grouped endpoint")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /tasks/grouped returns today, upcoming, overdue and completed buckets")
    public void shouldReturnGroupedTasks() {
        TaskListResponse grouped = taskService.getGrouped();

        assertThat(grouped.getToday()).isNotNull();
        assertThat(grouped.getUpcoming()).isNotNull();
        assertThat(grouped.getOverdue()).isNotNull();
        assertThat(grouped.getCompleted()).isNotNull();
    }

    @Test(groups = {"api-integration", "api", "tasks"})
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

    @Test(groups = {"api-integration", "api", "tasks"})
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

    @Test(groups = {"api-integration", "api", "tasks"})
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
        assertThat(grouped.getToday())
                .noneMatch(task -> created.getId().equals(task.getId()));
    }

    @Test(groups = {"api-integration", "api", "tasks"})
    @Story("Validation errors")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid task payload returns 400/422")
    public void shouldRejectInvalidTaskPayload() {
        IntegrationAssertions.assertValidationError(
                taskService.attemptCreate(TestDataFactory.invalidTaskRequest()));
    }

    @Test(groups = {"api-integration", "api", "tasks"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated task requests are rejected")
    public void shouldRejectUnauthorizedAccess() {
        TokenManager.clear();
        IntegrationAssertions.assertUnauthorized(taskService.fetchListUnauthorized());
        IntegrationAssertions.assertUnauthorized(taskService.fetchGroupedUnauthorized());
        loginAsDefaultUser();
    }
}
