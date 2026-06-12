package com.flowiq.ui.smoke;

import com.flowiq.base.UiAssertions;
import com.flowiq.ui.smoke.base.BaseUiSmokeTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Tasks")
public class TasksSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "tasks"})
    @Story("Task list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Tasks page displays task sections and list area")
    public void shouldDisplayTaskList() {
        pages.tasks().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.tasks().isLoaded()).isTrue();
        assertThat(pages.tasks().getVisibleTaskCount()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "tasks"})
    @Story("Filters")
    @Severity(SeverityLevel.NORMAL)
    @Description("Task filters and section tabs are visible")
    public void shouldDisplayTaskFilters() {
        pages.tasks().open();

        UiAssertions.assertElementVisible(pages.tasks().filters());
        UiAssertions.assertElementVisible(pages.tasks().searchInput());
        UiAssertions.assertElementVisible(pages.tasks().sectionButton("today"));
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "tasks"})
    @Story("Create task")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can create a new task via the dialog")
    public void shouldCreateTask() {
        String title = "UI smoke task " + System.currentTimeMillis();

        pages.tasks().open();
        pages.tasks().createTask(title, "Created by UI smoke test");

        assertThat(pages.tasks().containsTask(title)).isTrue();
    }
}
