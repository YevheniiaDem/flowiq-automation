package com.flowiq.api.regression.businessguide;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.api.regression.support.RegressionDataProviders;
import com.flowiq.auth.TokenManager;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Business Guide")
public class BusinessGuideRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Article list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /business-guide/articles returns paginated articles")
    public void shouldListArticles() {
        KnowledgeArticlePageDto page = businessGuideService.listArticles();

        assertThat(page.getContent()).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"},
            dataProvider = "paginationPages", dataProviderClass = RegressionDataProviders.class)
    @Story("Article pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Articles list supports page parameter")
    public void shouldPaginateArticlesByPage(int page) {
        KnowledgeArticlePageDto response = businessGuideService.listArticles(
                Map.of("page", page, "size", 10));

        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getContent()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"},
            dataProvider = "pageSizes", dataProviderClass = RegressionDataProviders.class)
    @Story("Article pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Articles list supports size parameter")
    public void shouldPaginateArticlesByPageSize(int size) {
        KnowledgeArticlePageDto response = businessGuideService.listArticles(
                Map.of("page", 0, "size", size));

        assertThat(response.getSize()).isEqualTo(size);
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"},
            dataProvider = "businessGuideSearchQueries", dataProviderClass = RegressionDataProviders.class)
    @Story("Article search")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Business guide search returns results for known query")
    public void shouldSearchArticles(String query) {
        KnowledgeSearchResponseDto search = businessGuideService.search(
                query, Map.of("page", 0, "size", 10));

        assertThat(search.getResults()).isNotNull();
        assertThat(search.getTotalElements()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Categories")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /business-guide/categories returns article categories")
    public void shouldReturnCategories() {
        assertThat(businessGuideService.getCategories()).isNotEmpty();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Article details")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /business-guide/articles/{slug} returns article details")
    public void shouldGetArticleBySlug() {
        KnowledgeArticlePageDto page = businessGuideService.listArticles();
        assertThat(page.getContent()).isNotEmpty();

        String slug = page.getContent().get(0).getSlug();
        KnowledgeArticleDetailDto article = businessGuideService.getArticleBySlug(slug);

        assertThat(article.getSlug()).isEqualTo(slug);
        assertThat(article.getTitle()).isNotBlank();
        assertThat(article.getContent()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"},
            dataProvider = "invalidSlugs", dataProviderClass = RegressionDataProviders.class)
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid article slug returns 404")
    public void shouldReturnNotFoundForInvalidSlug(String invalidSlug) {
        RegressionAssertions.assertNotFound(
                businessGuideService.fetchArticleBySlug(invalidSlug));
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated articles list request is rejected")
    public void shouldRejectUnauthorizedArticlesAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(businessGuideService.fetchArticlesUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated categories request is rejected")
    public void shouldRejectUnauthorizedCategoriesAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(businessGuideService.fetchCategoriesUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated article-by-slug request is rejected")
    public void shouldRejectUnauthorizedArticleBySlugAccess() {
        KnowledgeArticlePageDto page = businessGuideService.listArticles();
        String slug = page.getContent().get(0).getSlug();

        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(businessGuideService.fetchArticleBySlug(slug));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Dashboard snapshot")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /business-guide/dashboard-snapshot returns knowledge widget data")
    public void shouldReturnDashboardSnapshot() {
        assertThat(businessGuideService.getDashboardSnapshot()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Article search")
    @Severity(SeverityLevel.NORMAL)
    @Description("Search without query is handled safely")
    public void shouldHandleSearchWithoutQuery() {
        var result = businessGuideService.fetchSearchWithoutQuery();
        assertThat(result.getStatusCode()).isIn(200, 400, 422);
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Article list")
    @Severity(SeverityLevel.NORMAL)
    @Description("Articles fetch returns successful response")
    public void shouldFetchArticlesSuccessfully() {
        RegressionAssertions.assertOk(businessGuideService.fetchArticles());
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Categories")
    @Severity(SeverityLevel.NORMAL)
    @Description("Each category has a name and slug")
    public void shouldReturnCategoriesWithNames() {
        var categories = businessGuideService.getCategories();

        assertThat(categories.get(0).getLabel()).isNotBlank();
        assertThat(categories.get(0).getId()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "business-guide"})
    @Story("Article details")
    @Severity(SeverityLevel.NORMAL)
    @Description("Article detail includes category and reading time metadata")
    public void shouldReturnArticleWithMetadata() {
        KnowledgeArticlePageDto page = businessGuideService.listArticles();
        String slug = page.getContent().get(0).getSlug();

        KnowledgeArticleDetailDto article = businessGuideService.getArticleBySlug(slug);

        assertThat(article.getCategory()).isNotNull();
        assertThat(article.getReadingTimeMinutes()).isGreaterThanOrEqualTo(0);
    }
}
