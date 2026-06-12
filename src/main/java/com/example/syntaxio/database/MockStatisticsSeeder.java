package com.example.syntaxio.database;

import com.example.syntaxio.model.Solution;
import com.example.syntaxio.model.User;
import com.example.syntaxio.ui.util.PasswordHasher;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MockStatisticsSeeder {
    public static final String MOCK_USERNAME = "dashboard_demo";
    public static final String MOCK_PASSWORD = "password123";

    private static final int TOTAL_CHALLENGES = 20;
    private static final int COMPLETED_CHALLENGES = 7;
    private static final int ATTEMPTED_CHALLENGES = 9;
    private static final int TOTAL_HINTS_USED = 14;

    private static final List<MockSubmission> MOCK_SUBMISSIONS = List.of(
            new MockSubmission("ch-001", true, 0, 6, 9, 15),
            new MockSubmission("ch-004", true, 1, 5, 13, 40),
            new MockSubmission("ch-005", false, 1, 4, 10, 25),
            new MockSubmission("ch-005", true, 0, 4, 10, 55),
            new MockSubmission("ch-006", true, 2, 3, 14, 20),
            new MockSubmission("ch-002", false, 1, 3, 16, 5),
            new MockSubmission("ch-002", true, 1, 2, 11, 30),
            new MockSubmission("ch-003", true, 0, 2, 15, 45),
            new MockSubmission("ch-009", false, 2, 1, 9, 35),
            new MockSubmission("ch-017", false, 1, 1, 17, 10),
            new MockSubmission("ch-017", true, 3, 0, 10, 5),
            new MockSubmission("ch-018", false, 2, 0, 14, 50)
    );

    private MockStatisticsSeeder() {
    }

    public static User seed() {
        SqliteUserDAO userDAO = new SqliteUserDAO();
        new SqliteChallengeDAO();
        SqliteSolutionDAO solutionDAO = new SqliteSolutionDAO();

        User user = findOrCreateMockUser(userDAO);
        resetMockSubmissions(user.getId());

        for (MockSubmission submission : MOCK_SUBMISSIONS) {
            solutionDAO.addSolution(user.getId(), submission.toSolution());
        }

        updateMockUserStatistics(user.getId());
        return userDAO.findUserById(user.getId()).orElse(user);
    }

    public static void main(String[] args) {
        User user = seed();
        String databasePath = System.getProperty(SqliteConnection.DATABASE_PATH_PROPERTY, "syntaxio.db");
        System.out.printf(
                "Seeded mock dashboard statistics for user '%s' with password '%s' in %s%n",
                user.getUsername(),
                MOCK_PASSWORD,
                databasePath
        );
        SqliteConnection.closeConnection();
    }

    private static User findOrCreateMockUser(SqliteUserDAO userDAO) {
        Optional<User> existingUser = userDAO.findUserByUsername(MOCK_USERNAME);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setPasswordHash(PasswordHasher.hashPassword(MOCK_PASSWORD));
            user.setLastLoginAt(LocalDateTime.now());
            user.setLoginCount(Math.max(user.getLoginCount(), 8));
            user.setTotalHintsUsed(TOTAL_HINTS_USED);
            user.setTotalChallengesCompleted(COMPLETED_CHALLENGES);
            user.setCurrentActivityStreak(5);
            userDAO.updateUser(user);
            return user;
        }

        LocalDateTime createdAt = LocalDateTime.now().minusDays(21);
        User user = new User(MOCK_USERNAME, PasswordHasher.hashPassword(MOCK_PASSWORD));
        user.setCreatedAt(createdAt);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLoginCount(8);
        user.setTotalHintsUsed(TOTAL_HINTS_USED);
        user.setTotalChallengesCompleted(COMPLETED_CHALLENGES);
        user.setCurrentActivityStreak(5);
        userDAO.addUser(user);
        return user;
    }

    private static void resetMockSubmissions(int userId) {
        try (PreparedStatement statement = SqliteConnection.getInstance()
                .prepareStatement("DELETE FROM solutions WHERE userId = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not reset mock dashboard submissions", e);
        }
    }

    private static void updateMockUserStatistics(int userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("updated_at", now.toString());
        values.put("last_login_at", now.toString());
        values.put("last_login_date", today.toString());
        values.put("login_count", 8);
        values.put("total_login_count", 8);
        values.put("totalHintsUsed", TOTAL_HINTS_USED);
        values.put("totalChallengesCompleted", COMPLETED_CHALLENGES);
        values.put("total_challenges_completed", COMPLETED_CHALLENGES);
        values.put("total_challenges_attempted", ATTEMPTED_CHALLENGES);
        values.put("total_unfinished_challenges", ATTEMPTED_CHALLENGES - COMPLETED_CHALLENGES);
        values.put("completion_percentage", COMPLETED_CHALLENGES * 100.0 / TOTAL_CHALLENGES);
        values.put("first_completed_challenge_date", today.minusDays(6).toString());
        values.put("most_recent_completed_challenge_date", today.toString());
        values.put("total_attempts", MOCK_SUBMISSIONS.size());
        values.put("attempts_per_challenge",
                "{\"ch-001\":1,\"ch-002\":2,\"ch-003\":1,\"ch-004\":1,\"ch-005\":2,\"ch-006\":1,"
                        + "\"ch-009\":1,\"ch-017\":2,\"ch-018\":1}");
        values.put("average_attempts_before_completion", 1.43);
        values.put("failed_attempts", 5);
        values.put("successful_attempts", 7);
        values.put("most_attempted_challenge", "ch-002");
        values.put("total_time_spent_coding", 6120);
        values.put("time_spent_per_challenge",
                "{\"ch-001\":520,\"ch-002\":980,\"ch-003\":640,\"ch-004\":480,\"ch-005\":720,"
                        + "\"ch-006\":690,\"ch-009\":540,\"ch-017\":1180,\"ch-018\":370}");
        values.put("average_completion_time", 744.0);
        values.put("fastest_completion_time", 480);
        values.put("slowest_completion_time", 1180);
        values.put("time_spent_by_difficulty", "{\"EASY\":2410,\"MEDIUM\":2160,\"HARD\":1550}");
        values.put("time_spent_today", 1550);
        values.put("time_spent_this_week", 6120);
        values.put("time_spent_this_month", 6120);
        values.put("challenges_completed_per_topic", "{\"arrays\":3,\"strings\":2,\"math\":1,\"algorithms\":1}");
        values.put("average_score_per_topic",
                "{\"arrays\":86.0,\"strings\":90.0,\"math\":78.0,\"algorithms\":72.0}");
        values.put("weakest_topic", "algorithms");
        values.put("strongest_topic", "strings");
        values.put("topic_improvement_over_time",
                "[{\"topic\":\"arrays\",\"change\":12},{\"topic\":\"strings\",\"change\":8},"
                        + "{\"topic\":\"algorithms\",\"change\":5}]");
        values.put("easy_challenges_completed", 4);
        values.put("medium_challenges_completed", 2);
        values.put("hard_challenges_completed", 1);
        values.put("current_recommended_difficulty", "MEDIUM");
        values.put("highest_difficulty_completed", "HARD");
        values.put("difficulty_success_rate", "{\"EASY\":80.0,\"MEDIUM\":66.7,\"HARD\":50.0}");
        values.put("error_tracking", "{\"syntax\":3,\"logic\":4,\"edge_cases\":2}");
        values.put("days_active", 7);
        values.put("current_activity_streak", 5);
        values.put("longest_activity_streak", 5);
        values.put("challenges_completed_today", 1);
        values.put("challenges_completed_this_week", COMPLETED_CHALLENGES);
        values.put("number_of_coding_sessions", 6);
        values.put("average_session_length", 1020.0);
        values.put("longest_session", 1500);
        values.put("shortest_session", 520);
        values.put("last_session_duration", 920);
        values.put("challenges_attempted_per_session", "[1,2,1,2,2,1]");

        String sql = "UPDATE users SET " + assignments(values) + " WHERE id = ?";
        try (PreparedStatement statement = SqliteConnection.getInstance().prepareStatement(sql)) {
            bindValues(statement, values);
            statement.setInt(values.size() + 1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update mock dashboard statistics", e);
        }
    }

    private static String assignments(Map<String, Object> values) {
        return String.join(", ", values.keySet().stream()
                .map(column -> column + " = ?")
                .toList());
    }

    private static void bindValues(PreparedStatement statement, Map<String, Object> values) throws SQLException {
        int index = 1;
        for (Object value : values.values()) {
            if (value instanceof Integer integerValue) {
                statement.setInt(index, integerValue);
            } else if (value instanceof Double doubleValue) {
                statement.setDouble(index, doubleValue);
            } else {
                statement.setString(index, (String) value);
            }
            index++;
        }
    }

    private record MockSubmission(
            String challengeId,
            boolean passed,
            int hintsUsed,
            int daysAgo,
            int hour,
            int minute
    ) {
        private Solution toSolution() {
            Solution solution = new Solution(
                    challengeId,
                    "// Mock dashboard submission for " + challengeId,
                    passed,
                    hintsUsed
            );
            solution.setSubmittedAt(LocalDate.now().minusDays(daysAgo).atTime(hour, minute));
            return solution;
        }
    }
}
