package com.example.syntaxio.database;

import com.example.syntaxio.model.Solution;
import com.example.syntaxio.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockStatisticsSeederTest {

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
    void seedCreatesDemoUserWithDashboardStatistics() throws SQLException {
        User seededUser = MockStatisticsSeeder.seed();

        Optional<User> demoUser = new SqliteUserDAO().findUserByUsername(MockStatisticsSeeder.MOCK_USERNAME);
        List<Solution> solutions = new SqliteSolutionDAO().getSolutionsByUserId(seededUser.getId());
        Set<String> completedChallengeIds = solutions.stream()
                .filter(Solution::isPassed)
                .map(Solution::getChallengeId)
                .collect(Collectors.toSet());

        assertTrue(demoUser.isPresent());
        assertAll(
                () -> assertEquals(12, solutions.size()),
                () -> assertEquals(7, completedChallengeIds.size()),
                () -> assertEquals(14, solutions.stream().mapToInt(Solution::getHintsUsedForThisSolution).sum()),
                () -> assertEquals(7, demoUser.orElseThrow().getTotalChallengesCompleted()),
                () -> assertEquals(14, demoUser.orElseThrow().getTotalHintsUsed()),
                () -> assertTrue(SessionManager.getInstance().login(
                        MockStatisticsSeeder.MOCK_USERNAME,
                        MockStatisticsSeeder.MOCK_PASSWORD
                ))
        );

        assertSummaryColumns(seededUser.getId());
    }

    @Test
    void seedCanBeRunRepeatedlyWithoutDuplicatingSubmissions() {
        User firstSeed = MockStatisticsSeeder.seed();
        User secondSeed = MockStatisticsSeeder.seed();

        List<Solution> solutions = new SqliteSolutionDAO().getSolutionsByUserId(secondSeed.getId());

        assertAll(
                () -> assertEquals(firstSeed.getId(), secondSeed.getId()),
                () -> assertEquals(12, solutions.size())
        );
    }

    private void assertSummaryColumns(int userId) throws SQLException {
        try (Connection connection = SqliteConnection.getInstance();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT total_challenges_completed,
                            total_challenges_attempted,
                            total_unfinished_challenges,
                            completion_percentage,
                            total_attempts,
                            failed_attempts,
                            successful_attempts,
                            easy_challenges_completed,
                            medium_challenges_completed,
                            hard_challenges_completed,
                            current_recommended_difficulty,
                            highest_difficulty_completed
                     FROM users
                     WHERE id = ?
                     """)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Expected seeded demo user statistics");
                assertAll(
                        () -> assertEquals(7, resultSet.getInt("total_challenges_completed")),
                        () -> assertEquals(9, resultSet.getInt("total_challenges_attempted")),
                        () -> assertEquals(2, resultSet.getInt("total_unfinished_challenges")),
                        () -> assertEquals(35.0, resultSet.getDouble("completion_percentage"), 0.001),
                        () -> assertEquals(12, resultSet.getInt("total_attempts")),
                        () -> assertEquals(5, resultSet.getInt("failed_attempts")),
                        () -> assertEquals(7, resultSet.getInt("successful_attempts")),
                        () -> assertEquals(4, resultSet.getInt("easy_challenges_completed")),
                        () -> assertEquals(2, resultSet.getInt("medium_challenges_completed")),
                        () -> assertEquals(1, resultSet.getInt("hard_challenges_completed")),
                        () -> assertEquals("MEDIUM", resultSet.getString("current_recommended_difficulty")),
                        () -> assertEquals("HARD", resultSet.getString("highest_difficulty_completed"))
                );
            }
        }
    }
}
