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
import static org.junit.jupiter.api.Assertions.assertNull;
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
                     SELECT username, password_hash, created_at, last_login_at, updated_at,
                            login_count, last_login_date, total_login_count, total_challenges_completed
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
                String lastLoginDate = resultSet.getString("last_login_date");
                int totalLoginCount = resultSet.getInt("total_login_count");
                int totalChallengesCompleted = resultSet.getInt("total_challenges_completed");

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
                        () -> assertEquals(createdAt.toLocalDate().toString(), lastLoginDate),
                        () -> assertEquals(0, loginCount),
                        () -> assertEquals(0, totalLoginCount),
                        () -> assertEquals(0, totalChallengesCompleted)
                );
            }
        }
    }

    @Test
    void signupInitializesProgressStatisticsWithDefaults() throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();

        assertTrue(sessionManager.signup(USERNAME, PASSWORD));

        try (Connection connection = SqliteConnection.getInstance();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            statement.setString(1, USERNAME);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Expected a row for the newly created account");

                assertIntegerColumnsEqualZero(resultSet,
                        "total_challenges_completed",
                        "total_challenges_attempted",
                        "total_unfinished_challenges",
                        "total_attempts",
                        "failed_attempts",
                        "successful_attempts",
                        "total_time_spent_coding",
                        "fastest_completion_time",
                        "slowest_completion_time",
                        "time_spent_today",
                        "time_spent_this_week",
                        "time_spent_this_month",
                        "easy_challenges_completed",
                        "medium_challenges_completed",
                        "hard_challenges_completed",
                        "total_login_count",
                        "days_active",
                        "current_activity_streak",
                        "longest_activity_streak",
                        "challenges_completed_today",
                        "challenges_completed_this_week",
                        "number_of_coding_sessions",
                        "longest_session",
                        "shortest_session",
                        "last_session_duration"
                );

                assertRealColumnsEqualZero(resultSet,
                        "completion_percentage",
                        "average_attempts_before_completion",
                        "average_completion_time",
                        "average_session_length"
                );

                assertTextColumnsEqual(resultSet, "{}",
                        "attempts_per_challenge",
                        "time_spent_per_challenge",
                        "time_spent_by_difficulty",
                        "challenges_completed_per_topic",
                        "average_score_per_topic",
                        "difficulty_success_rate",
                        "error_tracking"
                );

                assertTextColumnsEqual(resultSet, "[]",
                        "topic_improvement_over_time",
                        "challenges_attempted_per_session"
                );

                assertNullColumns(resultSet,
                        "first_completed_challenge_date",
                        "most_recent_completed_challenge_date",
                        "most_attempted_challenge",
                        "weakest_topic",
                        "strongest_topic",
                        "highest_difficulty_completed"
                );

                assertEquals("EASY", resultSet.getString("current_recommended_difficulty"));
            }
        }
    }

    @Test
    void userDaoLoadsCurrentActivityStreakFromDatabase() throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();

        assertTrue(sessionManager.signup(USERNAME, PASSWORD));

        Connection connection = SqliteConnection.getInstance();
        try (PreparedStatement statement = connection.prepareStatement("""
                     UPDATE users
                     SET current_activity_streak = ?
                     WHERE username = ?
                     """)) {
            statement.setInt(1, 5);
            statement.setString(2, USERNAME);
            assertEquals(1, statement.executeUpdate());
        }

        var loadedUser = sessionManager.getUserDAO().findUserByUsername(USERNAME);

        assertTrue(loadedUser.isPresent());
        assertEquals(5, loadedUser.orElseThrow().getCurrentActivityStreak());
    }

    private void assertIntegerColumnsEqualZero(ResultSet resultSet, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            assertEquals(0, resultSet.getInt(columnName), "Expected zero for " + columnName);
        }
    }

    private void assertRealColumnsEqualZero(ResultSet resultSet, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            assertEquals(0.0, resultSet.getDouble(columnName), "Expected zero for " + columnName);
        }
    }

    private void assertTextColumnsEqual(ResultSet resultSet, String expectedValue, String... columnNames)
            throws SQLException {
        for (String columnName : columnNames) {
            assertEquals(expectedValue, resultSet.getString(columnName), "Unexpected value for " + columnName);
        }
    }

    private void assertNullColumns(ResultSet resultSet, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            assertNull(resultSet.getString(columnName), "Expected null for " + columnName);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        return assertDoesNotThrow(() -> LocalDateTime.parse(value));
    }
}
