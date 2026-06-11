package com.flowiq.ui.businessguide;

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
@Feature("Business Guide")
public class BusinessGuideUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "business-guide"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Business guide page displays search input")
    public void shouldDisplayBusinessGuidePage() {
        pages.businessGuide().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.businessGuide().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.businessGuide().searchInput());
    }
}
