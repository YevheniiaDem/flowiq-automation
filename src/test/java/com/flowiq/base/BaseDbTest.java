package com.flowiq.base;

import com.flowiq.db.cleanup.DatabaseCleaner;
import com.flowiq.db.container.PostgresTestContainer;
import com.flowiq.db.gateway.ImportDbGateway;
import com.flowiq.db.gateway.NotificationDbGateway;
import com.flowiq.db.gateway.ReportDbGateway;
import com.flowiq.db.gateway.TaskDbGateway;
import com.flowiq.db.gateway.TransactionDbGateway;
import com.flowiq.db.migration.FlywayMigrationRunner;
import com.flowiq.db.model.SeededUser;
import com.flowiq.db.seeder.TestDataSeeder;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.DockerClientFactory;
import org.testng.SkipException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import javax.sql.DataSource;

@Slf4j
public abstract class BaseDbTest {

    protected static boolean dockerAvailable;
    protected static PostgresTestContainer postgres;
    protected SeededUser seededUser;
    protected DataSource dataSource;

    protected TransactionDbGateway transactionDb;
    protected ReportDbGateway reportDb;
    protected ImportDbGateway importDb;
    protected TaskDbGateway taskDb;
    protected NotificationDbGateway notificationDb;

    @BeforeSuite(alwaysRun = true)
    public static void startTestDatabase() {
        dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        if (!dockerAvailable) {
            log.warn("Docker is not available. Database integration tests will be skipped.");
            return;
        }
        postgres = PostgresTestContainer.getInstance();
        postgres.start();
        FlywayMigrationRunner.migrate(postgres.dataSource());
        log.info("Testcontainers PostgreSQL ready at {}", postgres.getJdbcUrl());
    }

    @AfterSuite(alwaysRun = true)
    public static void stopTestDatabase() {
        if (postgres != null) {
            postgres.stop();
            FlywayMigrationRunner.resetMigrationState();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareIsolatedDatabase() {
        if (!dockerAvailable) {
            throw new SkipException("Docker is required for Testcontainers database integration tests");
        }
        dataSource = postgres.dataSource();
        DatabaseCleaner.clean(dataSource);
        seededUser = TestDataSeeder.seedUser(dataSource);

        transactionDb = new TransactionDbGateway(dataSource);
        reportDb = new ReportDbGateway(dataSource);
        importDb = new ImportDbGateway(dataSource);
        taskDb = new TaskDbGateway(dataSource);
        notificationDb = new NotificationDbGateway(dataSource);
    }

    protected SeededUser seededUser() {
        return seededUser;
    }
}
