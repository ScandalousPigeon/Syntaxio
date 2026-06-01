package com.example.syntaxio.database;

import com.example.syntaxio.model.Challenge;
import com.example.syntaxio.model.TestCase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteChallengeDAO {
    private Connection connection;

    public SqliteChallengeDAO() {
        connection = SqliteConnection.getInstance();
        createChallengesTable();
        createTestCasesTable();
        loadDefaultChallenges();
    }

    private void createChallengesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS challenges (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                starterCode TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                modelSolution TEXT NOT NULL
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Challenges table ready");
        } catch (SQLException e) {
            System.err.println("Error creating challenges table: " + e.getMessage());
        }
    }

    private void createTestCasesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS test_cases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                challengeId TEXT NOT NULL,
                description TEXT NOT NULL,
                input TEXT NOT NULL,
                expectedOutput TEXT NOT NULL,
                FOREIGN KEY (challengeId) REFERENCES challenges(id) ON DELETE CASCADE
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Test cases table ready");
        } catch (SQLException e) {
            System.err.println("Error creating test_cases table: " + e.getMessage());
        }
    }

    private void loadDefaultChallenges() {
        int addedCount = 0;

        for (Challenge challenge : DefaultChallenges.all()) {
            if (getChallengeById(challenge.getId()) == null && addChallenge(challenge)) {
                addedCount++;
            }
        }

        if (addedCount > 0) {
            System.out.println("Default challenges loaded: " + addedCount);
        }
    }

    public boolean addChallenge(Challenge challenge) {
        String sql = "INSERT INTO challenges (id, title, description, starterCode, difficulty, modelSolution) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, challenge.getId());
            pstmt.setString(2, challenge.getTitle());
            pstmt.setString(3, challenge.getDescription());
            pstmt.setString(4, challenge.getStarterCode());
            pstmt.setString(5, challenge.getDifficulty());
            pstmt.setString(6, challenge.getModelSolution());
            pstmt.executeUpdate();

            addTestCases(challenge.getId(), challenge.getTestCases());
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding challenge: " + e.getMessage());
            return false;
        }
    }

    private void addTestCases(String challengeId, List<TestCase> testCases) {
        String sql = "INSERT INTO test_cases (challengeId, description, input, expectedOutput) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (TestCase testCase : testCases) {
                pstmt.setString(1, challengeId);
                pstmt.setString(2, testCase.getDescription());
                pstmt.setString(3, testCase.getInput());
                pstmt.setString(4, testCase.getExpectedOutput());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error adding test cases: " + e.getMessage());
        }
    }

    public List<Challenge> getAllChallenges() {
        List<Challenge> challenges = new ArrayList<>();
        String sql = "SELECT * FROM challenges ORDER BY CASE difficulty WHEN 'EASY' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'HARD' THEN 3 ELSE 4 END ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                challenges.add(mapResultSetToChallenge(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting challenges: " + e.getMessage());
        }
        return challenges;
    }

    public Challenge getChallengeById(String id) {
        String sql = "SELECT * FROM challenges WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChallenge(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting challenge by ID: " + e.getMessage());
        }
        return null;
    }

    private List<TestCase> getTestCasesByChallengeId(String challengeId) {
        List<TestCase> testCases = new ArrayList<>();
        String sql = "SELECT * FROM test_cases WHERE challengeId = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, challengeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    testCases.add(new TestCase(
                        rs.getString("description"),
                        rs.getString("input"),
                        rs.getString("expectedOutput")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting test cases: " + e.getMessage());
        }
        return testCases;
    }

    private Challenge mapResultSetToChallenge(ResultSet rs) throws SQLException {
        String challengeId = rs.getString("id");
        return new Challenge(
            challengeId,
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("starterCode"),
            rs.getString("difficulty"),
            getTestCasesByChallengeId(challengeId),
            rs.getString("modelSolution")
        );
    }
}
