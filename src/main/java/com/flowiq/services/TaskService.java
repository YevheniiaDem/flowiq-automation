package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.constants.TestConstants;
import com.flowiq.models.tasks.CreateTaskRequest;
import com.flowiq.models.tasks.TaskListResponse;
import com.flowiq.models.tasks.TaskPageResponse;
import com.flowiq.models.tasks.TaskResponse;
import com.flowiq.models.tasks.TaskSuggestionResponse;
import com.flowiq.models.tasks.UpdateTaskRequest;
import io.qameta.allure.Step;

import java.util.List;
import java.util.Map;

public class TaskService extends BaseApiService {

    @Step("List tasks")
    public TaskPageResponse list(Map<String, ?> queryParams) {
        return getOk(ApiEndpoints.TASKS, queryParams, TaskPageResponse.class);
    }

    @Step("List tasks (default pagination)")
    public TaskPageResponse list() {
        return list(TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE));
    }

    @Step("Get tasks due today")
    public List<TaskResponse> getToday() {
        return getList(ApiEndpoints.TASKS_TODAY, TaskResponse.class);
    }

    @Step("Get upcoming tasks")
    public List<TaskResponse> getUpcoming() {
        return getList(ApiEndpoints.TASKS_UPCOMING, TaskResponse.class);
    }

    @Step("Get grouped tasks")
    public TaskListResponse getGrouped() {
        return getOk(ApiEndpoints.TASKS_GROUPED, TaskListResponse.class);
    }

    @Step("Get task suggestions")
    public List<TaskSuggestionResponse> getSuggestions() {
        return getList(ApiEndpoints.TASKS_SUGGESTIONS, TaskSuggestionResponse.class);
    }

    @Step("Create task")
    public TaskResponse create(CreateTaskRequest request) {
        return postCreated(ApiEndpoints.TASKS, request, TaskResponse.class);
    }

    @Step("Update task {id}")
    public TaskResponse update(long id, UpdateTaskRequest request) {
        return BaseResponseSpecification.extractOk(
                put(ApiEndpoints.withPathParam(ApiEndpoints.TASK_BY_ID, "id", id), request),
                TaskResponse.class);
    }

    @Step("Complete task {id}")
    public TaskResponse complete(long id) {
        return BaseResponseSpecification.extractOk(
                put(ApiEndpoints.withPathParam(ApiEndpoints.TASK_COMPLETE, "id", id)),
                TaskResponse.class);
    }

    @Step("Delete task {id}")
    public void deleteById(long id) {
        deleteNoContent(ApiEndpoints.withPathParam(ApiEndpoints.TASK_BY_ID, "id", id));
    }

    @Step("Fetch tasks list (unchecked)")
    public ApiCallResult<TaskPageResponse> fetchList(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.TASKS, queryParams, TaskPageResponse.class);
    }

    @Step("Fetch tasks list (unchecked, default pagination)")
    public ApiCallResult<TaskPageResponse> fetchList() {
        return fetchList(TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE));
    }

    @Step("Fetch tasks without authentication")
    public ApiCallResult<TaskPageResponse> fetchListUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.TASKS, TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE),
                TaskPageResponse.class);
    }

    @Step("Attempt create task")
    public ApiCallResult<TaskResponse> attemptCreate(CreateTaskRequest request) {
        return attemptPost(ApiEndpoints.TASKS, request, TaskResponse.class);
    }

    @Step("Fetch grouped tasks (unchecked)")
    public ApiCallResult<TaskListResponse> fetchGrouped() {
        return fetch(ApiEndpoints.TASKS_GROUPED, TaskListResponse.class);
    }

    @Step("Fetch grouped tasks without authentication")
    public ApiCallResult<TaskListResponse> fetchGroupedUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.TASKS_GROUPED, TaskListResponse.class);
    }

    @Step("Attempt update task {id}")
    public ApiCallResult<TaskResponse> attemptUpdate(long id, UpdateTaskRequest request) {
        return attemptPut(ApiEndpoints.withPathParam(ApiEndpoints.TASK_BY_ID, "id", id), request, TaskResponse.class);
    }

    @Step("Attempt complete task {id}")
    public ApiCallResult<TaskResponse> attemptComplete(long id) {
        return attemptPut(ApiEndpoints.withPathParam(ApiEndpoints.TASK_COMPLETE, "id", id), TaskResponse.class);
    }

    @Step("Attempt delete task {id}")
    public ApiCallResult<Void> attemptDeleteById(long id) {
        return super.attemptDelete(ApiEndpoints.withPathParam(ApiEndpoints.TASK_BY_ID, "id", id));
    }
}
