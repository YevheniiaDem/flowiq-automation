package com.flowiq.db.cleanup;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public final class DatabaseCleaner {

    private static final String CLEANUP_SQL = """
            TRUNCATE TABLE
                chat_messages,
                chat_conversations,
                notifications,
                tasks,
                report_jobs,
                import_jobs,
                transactions,
                users
            RESTART IDENTITY CASCADE
            """;

    private DatabaseCleaner() {
    }

    public static void clean(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(CLEANUP_SQL);
            log.debug("Test database cleaned for isolated test execution");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to clean test database", e);
        }
    }
}
