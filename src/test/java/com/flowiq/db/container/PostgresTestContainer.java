package com.flowiq.db.container;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
public final class PostgresTestContainer {

    private static final String IMAGE = "postgres:15-alpine";
    private static final String DATABASE = "flowiq";
    private static final String USERNAME = "flowiq";
    private static final String PASSWORD = "flowiq123";

    private static final PostgresTestContainer INSTANCE = new PostgresTestContainer();

    private final PostgreSQLContainer<?> container;
    private volatile HikariDataSource dataSource;

    private PostgresTestContainer() {
        this.container = new PostgreSQLContainer<>(IMAGE)
                .withDatabaseName(DATABASE)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withReuse(false);
    }

    public static PostgresTestContainer getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (!container.isRunning()) {
            container.start();
            log.info("PostgreSQL Testcontainer started: {}", container.getJdbcUrl());
            exportConnectionProperties();
        }
        dataSource();
    }

    public synchronized void stop() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
        if (container.isRunning()) {
            container.stop();
            log.info("PostgreSQL Testcontainer stopped");
        }
    }

    public PostgreSQLContainer<?> container() {
        return container;
    }

    public String getJdbcUrl() {
        ensureRunning();
        return container.getJdbcUrl();
    }

    public DataSource dataSource() {
        ensureRunning();
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(container.getJdbcUrl());
            config.setUsername(container.getUsername());
            config.setPassword(container.getPassword());
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(5);
            config.setPoolName("flowiq-testcontainers-pool");
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    private void ensureRunning() {
        if (!container.isRunning()) {
            throw new IllegalStateException("PostgreSQL Testcontainer is not running. Call start() first.");
        }
    }

    private void exportConnectionProperties() {
        System.setProperty("flowiq.test.jdbc.url", container.getJdbcUrl());
        System.setProperty("flowiq.test.jdbc.username", container.getUsername());
        System.setProperty("flowiq.test.jdbc.password", container.getPassword());
    }

    public Properties connectionProperties() {
        ensureRunning();
        Properties properties = new Properties();
        properties.setProperty("jdbc.url", container.getJdbcUrl());
        properties.setProperty("jdbc.username", container.getUsername());
        properties.setProperty("jdbc.password", container.getPassword());
        return properties;
    }
}
