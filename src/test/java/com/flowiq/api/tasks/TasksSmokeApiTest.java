package com.flowiq.api.tasks;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.tasks.TaskPageResponse;
import com.flowiq.models.tasks.TaskResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Tasks")
public class TasksSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "tasks"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can list tasks")
    public void shouldListTasks() {
        ApiCallResult<TaskPageResponse> result = taskService.fetchList();

        assertHappyPath(result);
        assertThat(result.getBody().getContent()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "tasks"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Tasks endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(taskService.fetchListUnauthorized());
    }

    @Test(groups = {"smoke", "api", "tasks"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Create task with empty title is rejected")
    public void shouldRejectInvalidCreatePayload() {
        ApiCallResult<TaskResponse> result =
                taskService.attemptCreate(TestDataFactory.invalidTaskRequest());

        assertValidationError(result);
    }

    @Test(groups = {"smoke", "api", "tasks"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Task page response matches JSON schema")
    public void shouldMatchTaskPageSchema() {
        ApiCallResult<TaskPageResponse> result = taskService.fetchList();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.TASK_PAGE);
    }
}
