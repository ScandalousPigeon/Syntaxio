package com.example.syntaxio.database;

import com.example.syntaxio.ui.util.PasswordHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountCreationTest {
    private static final String USERNAME = "new_account";
    private static final String PASSWORD = "password123";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        SqliteConnection.closeConnection();
        SessionManager.resetForTesting();
        System.setProperty(
                SqliteConnection.DATABASE_PATH_PROPERTY,
                tempDir.resolve("syntaxio-test.db").toString()
        );
    }

    @AfterEach
    void tearDown() {
        SessionManager.resetForTesting();
        SqliteConnection.closeConnection();
        System.clearProperty(SqliteConnection.DATABASE_PATH_PROPERTY);
    }

    @Test
    void signupPopulatesNewAccountDatabaseFields() throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();

        assertTrue(sessionManager.signup(USERNAME, PASSWORD));

        try (Connection connection = SqliteConnection.getInstance();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT username, password_hash, created_at, last_login_at, updated_at, login_count
                     FROM users
                     WHERE username = ?
                     """)) {
            statement.setString(1, USERNAME);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Expected a row for the newly created account");

                String username = resultSet.getString("username");
                String passwordHash = resultSet.getString("password_hash");
                String createdAtValue = resultSet.getString("created_at");
                String lastLoginAtValue = resultSet.getString("last_login_at");
                String updatedAtValue = resultSet.getString("updated_at");
                int loginCount = resultSet.getInt("login_count");

                LocalDateTime createdAt = parseDateTime(createdAtValue);
                LocalDateTime lastLoginAt = parseDateTime(lastLoginAtValue);
                LocalDateTime updatedAt = parseDateTime(updatedAtValue);

                assertAll(
                        () -> assertEquals(USERNAME, username),
                        () -> assertEquals(PasswordHasher.hashPassword(PASSWORD), passwordHash),
                        () -> assertNotEquals(PASSWORD, passwordHash),
                        () -> assertFalse(createdAtValue.isBlank()),
                        () -> assertFalse(lastLoginAtValue.isBlank()),
                        () -> assertFalse(updatedAtValue.isBlank()),
                        () -> assertEquals(createdAt, lastLoginAt),
                        () -> assertEquals(createdAt, updatedAt),
                        () -> assertEquals(0, loginCount)
                );
            }
        }
    }

    private LocalDateTime parseDateTime(String value) {
        return assertDoesNotThrow(() -> LocalDateTime.parse(value));
    }
}
