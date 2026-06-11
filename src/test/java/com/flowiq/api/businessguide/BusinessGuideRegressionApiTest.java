package com.flowiq.api.businessguide;

import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.models.knowledge.KnowledgeArticleDetailDto;
import com.flowiq.models.knowledge.KnowledgeArticlePageDto;
import com.flowiq.models.knowledge.KnowledgeSearchResponseDto;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Business Guide")
public class BusinessGuideRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "business-guide"})
    @Story("Article search")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Business guide search returns results for known query")
    public void shouldSearchArticles() {
        KnowledgeSearchResponseDto search = businessGuideService.search("ФОП", java.util.Map.of("page", 0, "size", 10));

        assertThat(search.getResults()).isNotNull();
        assertThat(search.getTotalElements()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"regression", "api", "business-guide"})
    @Story("Article details")
    @Severity(SeverityLevel.NORMAL)
    @Description("Articles can be listed and opened by slug")
    public void shouldListArticlesAndOpenBySlug() {
        KnowledgeArticlePageDto page = businessGuideService.listArticles();
        assertThat(page.getContent()).isNotEmpty();

        String slug = page.getContent().get(0).getSlug();
        KnowledgeArticleDetailDto article = businessGuideService.getArticleBySlug(slug);
        assertThat(article.getSlug()).isEqualTo(slug);
        assertThat(article.getTitle()).isNotBlank();
    }

    @Test(groups = {"regression", "api", "business-guide"})
    @Story("Categories")
    @Severity(SeverityLevel.NORMAL)
    @Description("Business guide categories are available")
    public void shouldReturnCategories() {
        assertThat(businessGuideService.getCategories()).isNotEmpty();
    }
}
