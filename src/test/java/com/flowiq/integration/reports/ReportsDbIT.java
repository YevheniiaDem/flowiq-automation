package com.flowiq.integration.reports;

import com.flowiq.base.BaseDbTest;
import com.flowiq.db.seeder.TestDataSeeder;
import com.flowiq.utils.DateUtils;
import com.flowiq.utils.RandomDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Database Integration")
@Feature("Reports")
public class ReportsDbIT extends BaseDbTest {

    @Test(groups = {"integration", "db", "reports"})
    @Story("Report job persistence")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Report job is stored for isolated user in Testcontainer")
    public void shouldPersistReportJobForIsolatedUser() {
        LocalDate from = DateUtils.parseDate(DateUtils.today()).withDayOfMonth(1);
        LocalDate to = DateUtils.parseDate(DateUtils.today());

        long reportId = reportDb.insert(
                seededUser().getId(),
                "PROFIT_AND_LOSS",
                "PDF",
                "COMPLETED",
                "report-" + RandomDataGenerator.alphanumeric(6) + ".pdf",
                4096L,
                from,
                to
        );

        var row = reportDb.findById(reportId).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.reportType()).isEqualTo("PROFIT_AND_LOSS");
        assertThat(row.status()).isEqualTo("COMPLETED");
        assertThat(reportDb.countByUserId(seededUser().getId())).isEqualTo(1);
    }

    @Test(groups = {"integration", "db", "reports"})
    @Story("User isolation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Report jobs are isolated between users")
    public void shouldIsolateReportJobsByUser() {
        var otherUser = TestDataSeeder.seedUser(dataSource);
        LocalDate today = DateUtils.parseDate(DateUtils.today());

        reportDb.insert(seededUser().getId(), "CASH_FLOW", "CSV", "COMPLETED", "a.csv", 100L, today, today);
        reportDb.insert(otherUser.getId(), "TAX_SUMMARY", "PDF", "COMPLETED", "b.pdf", 200L, today, today);

        assertThat(reportDb.countByUserId(seededUser().getId())).isEqualTo(1);
        assertThat(reportDb.countByUserId(otherUser.getId())).isEqualTo(1);
    }
}
