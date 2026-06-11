package com.flowiq.integration.tasks;

import com.flowiq.base.BaseApiDbIT;
import com.flowiq.factories.builders.TaskRequestBuilder;
import com.flowiq.models.tasks.TaskResponse;
import com.flowiq.models.tasks.TaskStatus;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Database Integration")
@Feature("Tasks")
public class TasksApiDbIT extends BaseApiDbIT {

    @Test(groups = {"integration", "api-db", "tasks"})
    @Story("API to database sync")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Task completed via API is reflected in Testcontainer database")
    public void shouldPersistApiCompletedTaskInDatabase() {
        TaskResponse created = taskService.create(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());
        TaskResponse completed = taskService.complete(created.getId());

        var row = taskDb.findById(completed.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.status()).isEqualTo(TaskStatus.COMPLETED.name());
        assertThat(row.title()).isEqualTo(created.getTitle());
    }
}
