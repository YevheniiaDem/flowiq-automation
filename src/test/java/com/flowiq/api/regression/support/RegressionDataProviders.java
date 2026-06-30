package com.flowiq.api.regression.support;

import com.flowiq.models.request.GenerateReportRequest;
import org.testng.annotations.DataProvider;

public final class RegressionDataProviders {

    private RegressionDataProviders() {
    }

    @DataProvider(name = "paginationPages")
    public static Object[][] paginationPages() {
        return new Object[][]{{0}, {1}, {2}};
    }

    @DataProvider(name = "pageSizes")
    public static Object[][] pageSizes() {
        return new Object[][]{{5}, {10}, {20}, {50}};
    }

    @DataProvider(name = "transactionSorts")
    public static Object[][] transactionSorts() {
        return new Object[][]{
                {"amount,asc"},
                {"amount,desc"},
                {"transactionDate,asc"},
                {"transactionDate,desc"}
        };
    }

    @DataProvider(name = "transactionTypes")
    public static Object[][] transactionTypes() {
        return new Object[][]{{"INCOME"}, {"EXPENSE"}};
    }

    @DataProvider(name = "invalidEmails")
    public static Object[][] invalidEmails() {
        return new Object[][]{
                {""},
                {"not-an-email"},
                {"missing-at-sign.com"},
                {"@nodomain.com"},
                {"spaces in@email.com"}
        };
    }

    @DataProvider(name = "invalidPasswords")
    public static Object[][] invalidPasswords() {
        return new Object[][]{{""}, {"1"}, {"12"}, {"short"}};
    }

    @DataProvider(name = "reportFormats")
    public static Object[][] reportFormats() {
        return new Object[][]{
                {GenerateReportRequest.Format.PDF},
                {GenerateReportRequest.Format.CSV},
                {GenerateReportRequest.Format.EXCEL}
        };
    }

    @DataProvider(name = "reportTypes")
    public static Object[][] reportTypes() {
        return new Object[][]{
                {GenerateReportRequest.ReportType.PROFIT_AND_LOSS},
                {GenerateReportRequest.ReportType.CASH_FLOW},
                {GenerateReportRequest.ReportType.REVENUE_SUMMARY},
                {GenerateReportRequest.ReportType.EXPENSE_SUMMARY},
                {GenerateReportRequest.ReportType.TAX_SUMMARY},
                {GenerateReportRequest.ReportType.FOP_SUMMARY}
        };
    }

    @DataProvider(name = "reportPeriodPresets")
    public static Object[][] reportPeriodPresets() {
        return new Object[][]{
                {"THIS_MONTH"},
                {"LAST_MONTH"},
                {"QUARTER"},
                {"YEAR"}
        };
    }

    @DataProvider(name = "businessGuideSearchQueries")
    public static Object[][] businessGuideSearchQueries() {
        return new Object[][]{{"ФОП"}, {"податки"}, {"звітність"}, {"бізнес"}, {"дохід"}};
    }

    @DataProvider(name = "taskSections")
    public static Object[][] taskSections() {
        return new Object[][]{{"today"}, {"upcoming"}, {"overdue"}, {"completed"}};
    }

    @DataProvider(name = "notificationUnreadFilters")
    public static Object[][] notificationUnreadFilters() {
        return new Object[][]{{true}, {false}};
    }

    @DataProvider(name = "reportTypeFormatCombinations")
    public static Object[][] reportTypeFormatCombinations() {
        boolean ciEnv = "ci".equalsIgnoreCase(System.getProperty("env", "local"));
        GenerateReportRequest.ReportType[] types = {
                GenerateReportRequest.ReportType.PROFIT_AND_LOSS,
                GenerateReportRequest.ReportType.CASH_FLOW,
                GenerateReportRequest.ReportType.REVENUE_SUMMARY,
                GenerateReportRequest.ReportType.EXPENSE_SUMMARY,
                GenerateReportRequest.ReportType.TAX_SUMMARY,
                GenerateReportRequest.ReportType.FOP_SUMMARY
        };
        if (ciEnv) {
            Object[][] rows = new Object[types.length][2];
            for (int i = 0; i < types.length; i++) {
                rows[i] = new Object[]{types[i], GenerateReportRequest.Format.PDF};
            }
            return rows;
        }
        return new Object[][]{
                {GenerateReportRequest.ReportType.PROFIT_AND_LOSS, GenerateReportRequest.Format.PDF},
                {GenerateReportRequest.ReportType.PROFIT_AND_LOSS, GenerateReportRequest.Format.CSV},
                {GenerateReportRequest.ReportType.PROFIT_AND_LOSS, GenerateReportRequest.Format.EXCEL},
                {GenerateReportRequest.ReportType.CASH_FLOW, GenerateReportRequest.Format.PDF},
                {GenerateReportRequest.ReportType.CASH_FLOW, GenerateReportRequest.Format.CSV},
                {GenerateReportRequest.ReportType.CASH_FLOW, GenerateReportRequest.Format.EXCEL},
                {GenerateReportRequest.ReportType.REVENUE_SUMMARY, GenerateReportRequest.Format.PDF},
                {GenerateReportRequest.ReportType.REVENUE_SUMMARY, GenerateReportRequest.Format.CSV},
                {GenerateReportRequest.ReportType.REVENUE_SUMMARY, GenerateReportRequest.Format.EXCEL},
                {GenerateReportRequest.ReportType.EXPENSE_SUMMARY, GenerateReportRequest.Format.PDF},
                {GenerateReportRequest.ReportType.EXPENSE_SUMMARY, GenerateReportRequest.Format.CSV},
                {GenerateReportRequest.ReportType.EXPENSE_SUMMARY, GenerateReportRequest.Format.EXCEL},
                {GenerateReportRequest.ReportType.TAX_SUMMARY, GenerateReportRequest.Format.PDF},
                {GenerateReportRequest.ReportType.TAX_SUMMARY, GenerateReportRequest.Format.CSV},
                {GenerateReportRequest.ReportType.TAX_SUMMARY, GenerateReportRequest.Format.EXCEL},
                {GenerateReportRequest.ReportType.FOP_SUMMARY, GenerateReportRequest.Format.PDF},
                {GenerateReportRequest.ReportType.FOP_SUMMARY, GenerateReportRequest.Format.CSV},
                {GenerateReportRequest.ReportType.FOP_SUMMARY, GenerateReportRequest.Format.EXCEL}
        };
    }

    @DataProvider(name = "invalidSlugs")
    public static Object[][] invalidSlugs() {
        return new Object[][]{
                {"non-existent-slug-xyz"},
                {"../../../etc/passwd"},
                {""},
                {"invalid slug with spaces"}
        };
    }

    @DataProvider(name = "invalidChatMessages")
    public static Object[][] invalidChatMessages() {
        return new Object[][]{{""}, {" "}, {"\n"}};
    }
}
