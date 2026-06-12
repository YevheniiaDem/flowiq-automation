package com.flowiq.api.integration.tasks;

import com.flowiq.api.integration.base.BaseApiIntegrationDbTest;
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

@Epic("API Integration")
@Feature("Tasks DB Consistency")
public class TasksIntegrationDbTest extends BaseApiIntegrationDbTest {

    @Test(groups = {"api-integration", "api-integration-db", "tasks"})
    @Story("Data consistency")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Task completed via API updates status in Testcontainer database")
    public void shouldPersistCompletedTaskStatusInDatabase() {
        TaskResponse created = taskService.create(TaskRequestBuilder.custom().uniqueTitle().dueTomorrow().build());

        taskService.complete(created.getId());

        var row = taskDb.findById(created.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.status()).isEqualTo(TaskStatus.COMPLETED.name());
    }
}
