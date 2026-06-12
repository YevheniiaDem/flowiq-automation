package com.flowiq.contracts.businessguide;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.knowledge.KnowledgeArticlePageDto;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Business Guide")
public class BusinessGuideContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "business-guide"})
    @Story("GET /api/business-guide/articles")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Business guide articles page matches contract schema")
    public void articlesPageShouldMatchContract() {
        ApiCallResult<KnowledgeArticlePageDto> result = businessGuideService.fetchArticles();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.BUSINESS_GUIDE_ARTICLES,
                "content", "page", "size", "totalElements", "totalPages");
    }
}
