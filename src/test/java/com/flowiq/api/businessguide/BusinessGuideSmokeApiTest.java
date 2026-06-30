package com.flowiq.api.businessguide;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
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

@Epic("API Smoke")
@Feature("Business Guide")
public class BusinessGuideSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "business-guide"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can list business guide articles")
    public void shouldListArticles() {
        ApiCallResult<KnowledgeArticlePageDto> result = businessGuideService.fetchArticles();

        assertHappyPath(result);
        assertThat(result.getBody().getContent()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "business-guide"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Business guide endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(businessGuideService.fetchArticlesUnauthorized());
    }

    @Test(groups = {"smoke", "api", "business-guide"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Search without query is handled safely (default listing or validation error)")
    public void shouldHandleSearchWithoutQuery() {
        ApiCallResult<KnowledgeSearchResponseDto> result = businessGuideService.fetchSearchWithoutQuery();

        assertHandledSafely(result);
        if (result.getStatusCode() == 200) {
            assertThat(result.getBody().getResults()).isNotNull();
        }
    }

    @Test(groups = {"smoke", "api", "business-guide"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Business guide articles response matches JSON schema")
    public void shouldMatchArticlesSchema() {
        ApiCallResult<KnowledgeArticlePageDto> result = businessGuideService.fetchArticles();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.BUSINESS_GUIDE_ARTICLES);
    }
}
