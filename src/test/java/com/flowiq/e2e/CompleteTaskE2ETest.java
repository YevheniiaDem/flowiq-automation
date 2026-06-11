package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.factories.builders.TaskRequestBuilder;
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

@Epic("E2E")
@Feature("Complete Task")
public class CompleteTaskE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "tasks"})
    @Story("Complete task flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Task created via API is completed and verified in UI tasks page")
    public void shouldCompleteTaskAndVerifyInUi() {
        TaskResponse created = taskService.create(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TestCleanupManager.registerTaskCleanup(taskService, created.getId());

        TaskResponse completed = taskService.complete(created.getId());
        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        var tasksPage = pages.tasks();
        tasksPage.open();
        tasksPage.search(created.getTitle());

        assertThat(tasksPage.isLoaded()).isTrue();
        assertThat(tasksPage.searchInput().inputValue()).contains(created.getTitle());
    }
}
