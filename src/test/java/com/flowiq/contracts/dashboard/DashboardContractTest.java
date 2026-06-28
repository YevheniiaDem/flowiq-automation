package com.flowiq.contracts.dashboard;

import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Dashboard")
public class DashboardContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "dashboard"})
    @Story("GET /api/dashboard/stats")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Dashboard stats response matches contract schema")
    public void dashboardStatsShouldMatchContract() {
        ContractAssertions.assertContractResponse(
                dashboardService.fetchStats(), 200, ContractSchemas.DASHBOARD_STATS);
    }
}
