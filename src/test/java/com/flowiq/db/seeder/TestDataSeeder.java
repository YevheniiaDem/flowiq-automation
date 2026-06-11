package com.flowiq.db.seeder;

import com.flowiq.db.model.SeededUser;
import com.flowiq.utils.RandomDataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
public final class TestDataSeeder {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private TestDataSeeder() {
    }

    public static SeededUser seedUser(DataSource dataSource) {
        String email = "it-" + RandomDataGenerator.uuid().substring(0, 8) + "@flowiq.test";
        String plainPassword = RandomDataGenerator.password(12);
        String encodedPassword = PASSWORD_ENCODER.encode(plainPassword);
        String name = RandomDataGenerator.firstName() + " " + RandomDataGenerator.lastName();
        String company = RandomDataGenerator.companyName();
        Timestamp now = Timestamp.from(Instant.now());

        String sql = """
                INSERT INTO users (email, password, name, company, role, is_active, email_verified, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'USER', true, true, ?, ?)
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, encodedPassword);
            statement.setString(3, name);
            statement.setString(4, company);
            statement.setTimestamp(5, now);
            statement.setTimestamp(6, now);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Failed to insert isolated test user");
                }
                long userId = resultSet.getLong("id");
                log.debug("Seeded isolated user id={} email={}", userId, email);
                return SeededUser.builder()
                        .id(userId)
                        .email(email)
                        .plainPassword(plainPassword)
                        .name(name)
                        .company(company)
                        .build();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to seed isolated test user", e);
        }
    }
}
