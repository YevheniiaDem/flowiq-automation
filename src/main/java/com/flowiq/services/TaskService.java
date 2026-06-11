package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
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
        return list(Map.of("page", 0, "size", 20));
    }

    @Step("Get tasks due today")
    public List<TaskResponse> getToday() {
        return get(ApiEndpoints.TASKS_TODAY).getRaw().jsonPath().getList("", TaskResponse.class);
    }

    @Step("Get upcoming tasks")
    public List<TaskResponse> getUpcoming() {
        return get(ApiEndpoints.TASKS_UPCOMING).getRaw().jsonPath().getList("", TaskResponse.class);
    }

    @Step("Get grouped tasks")
    public TaskListResponse getGrouped() {
        return getOk(ApiEndpoints.TASKS_GROUPED, TaskListResponse.class);
    }

    @Step("Get task suggestions")
    public List<TaskSuggestionResponse> getSuggestions() {
        return get(ApiEndpoints.TASKS_SUGGESTIONS).getRaw().jsonPath().getList("", TaskSuggestionResponse.class);
    }

    @Step("Create task")
    public TaskResponse create(CreateTaskRequest request) {
        return postCreated(ApiEndpoints.TASKS, request, TaskResponse.class);
    }

    @Step("Update task {id}")
    public TaskResponse update(long id, UpdateTaskRequest request) {
        return BaseResponseSpecification.extractOk(
                put(ApiEndpoints.TASK_BY_ID.replace("{id}", String.valueOf(id)), request),
                TaskResponse.class);
    }

    @Step("Complete task {id}")
    public TaskResponse complete(long id) {
        return BaseResponseSpecification.extractOk(
                put(ApiEndpoints.TASK_COMPLETE.replace("{id}", String.valueOf(id))),
                TaskResponse.class);
    }

    @Step("Delete task {id}")
    public void deleteById(long id) {
        deleteNoContent(ApiEndpoints.TASK_BY_ID.replace("{id}", String.valueOf(id)));
    }

    @Step("Fetch tasks list (unchecked)")
    public ApiCallResult<TaskPageResponse> fetchList(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.TASKS, queryParams, TaskPageResponse.class);
    }

    @Step("Fetch tasks list (unchecked, default pagination)")
    public ApiCallResult<TaskPageResponse> fetchList() {
        return fetchList(Map.of("page", 0, "size", 20));
    }

    @Step("Fetch tasks without authentication")
    public ApiCallResult<TaskPageResponse> fetchListUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.TASKS, Map.of("page", 0, "size", 20), TaskPageResponse.class);
    }

    @Step("Attempt create task")
    public ApiCallResult<TaskResponse> attemptCreate(CreateTaskRequest request) {
        return attemptPost(ApiEndpoints.TASKS, request, TaskResponse.class);
    }
}
