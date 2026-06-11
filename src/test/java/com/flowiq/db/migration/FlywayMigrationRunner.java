package com.flowiq.db.migration;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

@Slf4j
public final class FlywayMigrationRunner {

    private static volatile boolean migrated;

    private FlywayMigrationRunner() {
    }

    public static synchronized void migrate(DataSource dataSource) {
        if (migrated) {
            return;
        }
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .load();
        int applied = flyway.migrate().migrationsExecuted;
        migrated = true;
        log.info("Flyway applied {} migration(s) to Testcontainer database", applied);
    }

    public static synchronized void resetMigrationState() {
        migrated = false;
    }
}
