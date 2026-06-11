package com.flowiq.api.tasks;

import com.flowiq.base.BaseRegressionApiTest;
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

@Epic("API Regression")
@Feature("Tasks")
public class TasksRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "tasks"})
    @Story("Task lifecycle")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can create and complete a task")
    public void shouldCreateAndCompleteTask() {
        TaskResponse created = taskService.create(
                TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(TaskStatus.TODO);

        TaskResponse completed = taskService.complete(created.getId());
        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test(groups = {"regression", "api", "tasks"})
    @Story("Grouped tasks")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Grouped tasks endpoint returns task buckets")
    public void shouldReturnGroupedTasks() {
        TaskListResponse grouped = taskService.getGrouped();

        assertThat(grouped.getToday()).isNotNull();
        assertThat(grouped.getUpcoming()).isNotNull();
        assertThat(grouped.getOverdue()).isNotNull();
        assertThat(grouped.getCompleted()).isNotNull();
    }
}
