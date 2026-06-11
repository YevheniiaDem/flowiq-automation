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
public class ReportDbGateway {

    private final DataSource dataSource;

    public long insert(long userId, String reportType, String format, String status,
                       String fileName, long fileSize, LocalDate periodFrom, LocalDate periodTo) {
        String sql = """
                INSERT INTO report_jobs
                (user_id, report_type, format, status, file_name, file_size, period_from, period_to, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, reportType);
            statement.setString(3, format);
            statement.setString(4, status);
            statement.setString(5, fileName);
            statement.setLong(6, fileSize);
            statement.setObject(7, periodFrom);
            statement.setObject(8, periodTo);
            statement.setTimestamp(9, Timestamp.from(Instant.now()));
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert report job", e);
        }
    }

    public Optional<ReportRow> findById(long id) {
        String sql = """
                SELECT id, user_id, report_type, format, status, file_name, file_size, period_from, period_to
                FROM report_jobs WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ReportRow(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("report_type"),
                        rs.getString("format"),
                        rs.getString("status"),
                        rs.getString("file_name"),
                        rs.getLong("file_size"),
                        rs.getObject("period_from", LocalDate.class),
                        rs.getObject("period_to", LocalDate.class)
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find report job", e);
        }
    }

    public int countByUserId(long userId) {
        return count("SELECT COUNT(*) FROM report_jobs WHERE user_id = ?", userId);
    }

    private int count(String sql, long userId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count report jobs", e);
        }
    }

    public record ReportRow(
            long id,
            long userId,
            String reportType,
            String format,
            String status,
            String fileName,
            long fileSize,
            LocalDate periodFrom,
            LocalDate periodTo
    ) {
    }
}
