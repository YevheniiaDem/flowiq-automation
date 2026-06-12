package com.flowiq.contracts.tasks;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.tasks.TaskListResponse;
import com.flowiq.models.tasks.TaskPageResponse;
import com.flowiq.models.tasks.TaskPriority;
import com.flowiq.models.tasks.TaskStatus;
import com.flowiq.models.tasks.TaskType;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Tasks")
public class TasksContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "tasks"})
    @Story("GET /api/tasks")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Tasks page response matches contract schema")
    public void tasksListShouldMatchContract() {
        ApiCallResult<TaskPageResponse> result = taskService.fetchList();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.TASKS_PAGE,
                "content", "page", "size", "totalElements", "totalPages");

        if (result.getResponse() != null) {
            ContractAssertions.assertEnumValuesInNestedArray(
                    result.getResponse(), "content", "type", TaskType.class);
            ContractAssertions.assertEnumValuesInNestedArray(
                    result.getResponse(), "content", "status", TaskStatus.class);
            ContractAssertions.assertEnumValuesInNestedArray(
                    result.getResponse(), "content", "priority", TaskPriority.class);
        }
    }

    @Test(groups = {"contract", "tasks"})
    @Story("GET /api/tasks/grouped")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Grouped tasks response matches contract schema")
    public void groupedTasksShouldMatchContract() {
        ApiCallResult<TaskListResponse> result = taskService.fetchGrouped();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.TASKS_GROUPED,
                "today", "upcoming", "overdue", "completed");
    }
}
