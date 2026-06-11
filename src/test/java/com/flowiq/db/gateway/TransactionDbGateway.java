package com.flowiq.db.gateway;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
public class TransactionDbGateway {

    private final DataSource dataSource;

    public long insert(long userId, String type, BigDecimal amount, String category,
                       String description, LocalDate transactionDate) {
        String sql = """
                INSERT INTO transactions
                (user_id, type, amount, category, description, transaction_date, auto_categorized, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, false, ?, ?)
                RETURNING id
                """;
        Timestamp now = Timestamp.from(Instant.now());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, type);
            statement.setBigDecimal(3, amount);
            statement.setString(4, category);
            statement.setString(5, description);
            statement.setObject(6, transactionDate);
            statement.setTimestamp(7, now);
            statement.setTimestamp(8, now);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert transaction", e);
        }
    }

    public Optional<TransactionRow> findById(long id) {
        String sql = """
                SELECT id, user_id, type, amount, category, description, transaction_date
                FROM transactions WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find transaction", e);
        }
    }

    public int countByUserId(long userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count transactions", e);
        }
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete transaction", e);
        }
    }

    private TransactionRow mapRow(ResultSet rs) throws SQLException {
        return new TransactionRow(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("type"),
                rs.getBigDecimal("amount"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getObject("transaction_date", LocalDate.class)
        );
    }

    public record TransactionRow(
            long id,
            long userId,
            String type,
            BigDecimal amount,
            String category,
            String description,
            LocalDate transactionDate
    ) {
    }
}
