package com.flowiq.db.gateway;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class NotificationDbGateway {

    private final DataSource dataSource;

    public long insert(long userId, String title, String message, String type, String severity,
                       String actionUrl, String deduplicationKey, boolean read) {
        String sql = """
                INSERT INTO notifications
                (user_id, title, message, type, severity, channel, is_read, action_url, deduplication_key, created_at)
                VALUES (?, ?, ?, ?, ?, 'IN_APP', ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, title);
            statement.setString(3, message);
            statement.setString(4, type);
            statement.setString(5, severity);
            statement.setBoolean(6, read);
            statement.setString(7, actionUrl);
            statement.setString(8, deduplicationKey);
            statement.setTimestamp(9, Timestamp.from(Instant.now()));
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert notification", e);
        }
    }

    public void markAsRead(long notificationId) {
        String sql = """
                UPDATE notifications
                SET is_read = true, read_at = ?
                WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(Instant.now()));
            statement.setLong(2, notificationId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to mark notification as read", e);
        }
    }

    public Optional<NotificationRow> findById(long id) {
        String sql = """
                SELECT id, user_id, title, message, type, severity, is_read, action_url, deduplication_key
                FROM notifications WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new NotificationRow(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getString("type"),
                        rs.getString("severity"),
                        rs.getBoolean("is_read"),
                        rs.getString("action_url"),
                        rs.getString("deduplication_key")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find notification", e);
        }
    }

    public int countUnreadByUserId(long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count unread notifications", e);
        }
    }

    public record NotificationRow(
            long id,
            long userId,
            String title,
            String message,
            String type,
            String severity,
            boolean read,
            String actionUrl,
            String deduplicationKey
    ) {
    }
}
