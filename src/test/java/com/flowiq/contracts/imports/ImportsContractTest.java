package com.flowiq.contracts.imports;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.response.ImportListResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Imports")
public class ImportsContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "imports"})
    @Story("GET /api/imports")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Imports list response matches contract schema")
    public void importsListShouldMatchContract() {
        ApiCallResult<ImportListResponse> result = importService.fetchList();

        ContractAssertions.assertContractResponse(result, 200, ContractSchemas.IMPORTS_LIST);
    }
}
