package com.flowiq.factories.builders;

import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.tasks.CreateTaskRequest;
import com.flowiq.models.tasks.TaskPriority;
import com.flowiq.models.tasks.TaskStatus;
import com.flowiq.models.tasks.TaskType;
import com.flowiq.models.tasks.UpdateTaskRequest;
import com.flowiq.utils.DateUtils;
import com.flowiq.utils.RandomDataGenerator;

import java.time.LocalDate;

public final class TaskRequestBuilder {

    private final CreateTaskRequest request;

    private TaskRequestBuilder(CreateTaskRequest request) {
        this.request = request;
    }

    public static TaskRequestBuilder custom() {
        return new TaskRequestBuilder(TestDataFactory.validTaskRequest());
    }

    public TaskRequestBuilder title(String title) {
        request.setTitle(title);
        return this;
    }

    public TaskRequestBuilder uniqueTitle() {
        request.setTitle("Auto task " + RandomDataGenerator.alphanumeric(8));
        return this;
    }

    public TaskRequestBuilder description(String description) {
        request.setDescription(description);
        return this;
    }

    public TaskRequestBuilder type(TaskType type) {
        request.setType(type);
        return this;
    }

    public TaskRequestBuilder priority(TaskPriority priority) {
        request.setPriority(priority);
        return this;
    }

    public TaskRequestBuilder status(TaskStatus status) {
        request.setStatus(status);
        return this;
    }

    public TaskRequestBuilder dueDate(LocalDate dueDate) {
        request.setDueDate(dueDate);
        return this;
    }

    public TaskRequestBuilder dueTomorrow() {
        request.setDueDate(DateUtils.parseDate(DateUtils.tomorrow()));
        return this;
    }

    public TaskRequestBuilder dueToday() {
        request.setDueDate(DateUtils.parseDate(DateUtils.today()));
        return this;
    }

    public TaskRequestBuilder duePast(int daysAgo) {
        request.setDueDate(DateUtils.parseDate(DateUtils.daysAgo(daysAgo)));
        return this;
    }

    public UpdateTaskRequest toUpdateRequest() {
        UpdateTaskRequest update = new UpdateTaskRequest();
        update.setTitle(request.getTitle());
        update.setDescription(request.getDescription());
        update.setType(request.getType());
        update.setPriority(request.getPriority());
        update.setStatus(request.getStatus());
        update.setDueDate(request.getDueDate());
        return update;
    }

    public CreateTaskRequest build() {
        return request;
    }
}
