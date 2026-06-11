package com.flowiq.integration.tasks;

import com.flowiq.base.BaseDbTest;
import com.flowiq.db.seeder.TestDataSeeder;
import com.flowiq.utils.DateUtils;
import com.flowiq.utils.RandomDataGenerator;
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
public class TasksDbIT extends BaseDbTest {

    @Test(groups = {"integration", "db", "tasks"})
    @Story("Task lifecycle")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Task is created and completed for isolated user")
    public void shouldPersistAndCompleteTaskForIsolatedUser() {
        String title = "task-" + RandomDataGenerator.alphanumeric(8);
        String dedupKey = "db-it-" + RandomDataGenerator.uuid();

        long taskId = taskDb.insert(
                seededUser().getId(),
                title,
                "integration task",
                "CUSTOM",
                "MEDIUM",
                "TODO",
                DateUtils.parseDate(DateUtils.tomorrow()),
                dedupKey
        );

        taskDb.markCompleted(taskId);
        var row = taskDb.findById(taskId).orElseThrow();

        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.title()).isEqualTo(title);
        assertThat(row.status()).isEqualTo("COMPLETED");
    }

    @Test(groups = {"integration", "db", "tasks"})
    @Story("User isolation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Tasks are isolated between users")
    public void shouldIsolateTasksByUser() {
        var otherUser = TestDataSeeder.seedUser(dataSource);

        taskDb.insert(seededUser().getId(), "task-a", null, "CUSTOM", "LOW", "TODO", null, "a-" + RandomDataGenerator.uuid());
        taskDb.insert(otherUser.getId(), "task-b", null, "TAX", "HIGH", "TODO", null, "b-" + RandomDataGenerator.uuid());

        assertThat(taskDb.countByUserId(seededUser().getId())).isEqualTo(1);
        assertThat(taskDb.countByUserId(otherUser.getId())).isEqualTo(1);
    }
}
