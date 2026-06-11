package com.flowiq.ui.tasks;

import com.flowiq.base.AuthenticatedUiTest;
import com.flowiq.base.UiAssertions;
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
public class TasksUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "tasks"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Tasks page displays add task action and filters")
    public void shouldDisplayTasksPage() {
        pages.tasks().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.tasks().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.tasks().addButton());
        UiAssertions.assertElementVisible(pages.tasks().filters());
    }
}
