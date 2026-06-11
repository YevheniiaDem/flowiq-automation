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
public class ImportDbGateway {

    private final DataSource dataSource;

    public long insert(long userId, String fileName, long fileSize, String status,
                       int rowsProcessed, int rowsImported, int errorsCount, String bankFormat) {
        String sql = """
                INSERT INTO import_jobs
                (user_id, file_name, file_size, status, rows_processed, rows_imported, errors_count, bank_format, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, fileName);
            statement.setLong(3, fileSize);
            statement.setString(4, status);
            statement.setInt(5, rowsProcessed);
            statement.setInt(6, rowsImported);
            statement.setInt(7, errorsCount);
            statement.setString(8, bankFormat);
            statement.setTimestamp(9, Timestamp.from(Instant.now()));
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert import job", e);
        }
    }

    public Optional<ImportRow> findById(long id) {
        String sql = """
                SELECT id, user_id, file_name, file_size, status, rows_processed, rows_imported, errors_count, bank_format
                FROM import_jobs WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ImportRow(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("file_name"),
                        rs.getLong("file_size"),
                        rs.getString("status"),
                        rs.getInt("rows_processed"),
                        rs.getInt("rows_imported"),
                        rs.getInt("errors_count"),
                        rs.getString("bank_format")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find import job", e);
        }
    }

    public int countByUserId(long userId) {
        String sql = "SELECT COUNT(*) FROM import_jobs WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count import jobs", e);
        }
    }

    public record ImportRow(
            long id,
            long userId,
            String fileName,
            long fileSize,
            String status,
            int rowsProcessed,
            int rowsImported,
            int errorsCount,
            String bankFormat
    ) {
    }
}
