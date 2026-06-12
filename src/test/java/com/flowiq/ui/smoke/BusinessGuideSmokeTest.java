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
@Feature("Business Guide")
public class BusinessGuideSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "business-guide"})
    @Story("Search")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User can search business guide articles")
    public void shouldSearchArticles() {
        pages.businessGuide().open();

        pages.businessGuide().search("ФОП");

        UiAssertions.assertElementVisible(pages.businessGuide().searchInput());
        assertThat(pages.businessGuide().searchInput().inputValue()).contains("ФОП");
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "business-guide"})
    @Story("Open article")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User can open a business guide article detail page")
    public void shouldOpenArticle() {
        pages.businessGuide().open();

        assertThat(pages.businessGuide().articleLinks().count()).isGreaterThan(0);
        pages.businessGuide().openFirstArticle();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.businessGuide().isArticleDetailVisible()).isTrue();
    }
}
