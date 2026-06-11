package com.flowiq.db.gateway;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
public class TaskDbGateway {

    private final DataSource dataSource;

    public long insert(long userId, String title, String description, String type,
                       String priority, String status, LocalDate dueDate, String deduplicationKey) {
        String sql = """
                INSERT INTO tasks
                (user_id, title, description, type, priority, status, due_date, deduplication_key, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        Timestamp now = Timestamp.from(Instant.now());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setString(4, type);
            statement.setString(5, priority);
            statement.setString(6, status);
            statement.setObject(7, dueDate);
            statement.setString(8, deduplicationKey);
            statement.setTimestamp(9, now);
            statement.setTimestamp(10, now);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert task", e);
        }
    }

    public void markCompleted(long taskId) {
        String sql = """
                UPDATE tasks
                SET status = 'COMPLETED', completed_at = ?, updated_at = ?
                WHERE id = ?
                """;
        Timestamp now = Timestamp.from(Instant.now());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, now);
            statement.setTimestamp(2, now);
            statement.setLong(3, taskId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to complete task", e);
        }
    }

    public Optional<TaskRow> findById(long id) {
        String sql = """
                SELECT id, user_id, title, description, type, priority, status, due_date
                FROM tasks WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new TaskRow(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getObject("due_date", LocalDate.class)
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find task", e);
        }
    }

    public int countByUserId(long userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count tasks", e);
        }
    }

    public record TaskRow(
            long id,
            long userId,
            String title,
            String description,
            String type,
            String priority,
            String status,
            LocalDate dueDate
    ) {
    }
}
