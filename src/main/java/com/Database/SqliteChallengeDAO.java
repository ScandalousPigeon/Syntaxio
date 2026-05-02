package com.Database;

import com.Model.Challenge;
import com.Model.TestCase;

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
        if (!getAllChallenges().isEmpty()) return;

        List<TestCase> testCases1 = List.of(
            new TestCase("Array with positive numbers", "new int[]{1, 2, 3}", "6"),
            new TestCase("Array with negative numbers", "new int[]{-1, -2, -3}", "-6"),
            new TestCase("Empty array", "new int[]{}", "0"),
            new TestCase("Array with one element", "new int[]{42}", "42")
        );

        Challenge challenge1 = new Challenge(
            "ch-001",
            "Sum of Array",
            "Write a method that takes an array of integers and returns the sum of all elements.\n\n" +
            "Method signature: `public int sumArray(int[] numbers)`",
            "public int sumArray(int[] numbers) {\n    // Your code here\n    return 0;\n}",
            "EASY",
            testCases1,
            "public int sumArray(int[] numbers) {\n    int sum = 0;\n    for (int num : numbers) {\n        sum += num;\n    }\n    return sum;\n}"
        );

        List<TestCase> testCases2 = List.of(
            new TestCase("Normal array", "new int[]{3, 7, 2, 9, 1}", "9"),
            new TestCase("All negative", "new int[]{-5, -2, -8, -1}", "-1"),
            new TestCase("Single element", "new int[]{10}", "10")
        );

        Challenge challenge2 = new Challenge(
            "ch-002",
            "Find Maximum",
            "Write a method that finds and returns the maximum value in an array of integers.\n\n" +
            "Method signature: `public int findMax(int[] numbers)`\n\n" +
            "Hint: If the array is empty, return Integer.MIN_VALUE",
            "public int findMax(int[] numbers) {\n    if (numbers.length == 0) {\n        return Integer.MIN_VALUE;\n    }\n    // Your code here\n    return 0;\n}",
            "MEDIUM",
            testCases2,
            "public int findMax(int[] numbers) {\n    if (numbers.length == 0) {\n        return Integer.MIN_VALUE;\n    }\n    int max = numbers[0];\n    for (int num : numbers) {\n        if (num > max) {\n            max = num;\n        }\n    }\n    return max;\n}"
        );

        List<TestCase> testCases3 = List.of(
            new TestCase("Normal string", "\"hello\"", "olleh"),
            new TestCase("Palindrome", "\"racecar\"", "racecar"),
            new TestCase("Empty string", "\"\"", "")
        );

        Challenge challenge3 = new Challenge(
            "ch-003",
            "Reverse String",
            "Write a method that reverses a string.\n\n" +
            "Method signature: `public String reverseString(String input)`\n\n" +
            "Example: `reverseString(\"hello\")` should return `\"olleh\"`",
            "public String reverseString(String input) {\n    // Your code here\n    return \"\";\n}",
            "MEDIUM",
            testCases3,
            "public String reverseString(String input) {\n    return new StringBuilder(input).reverse().toString();\n}"
        );

        addChallenge(challenge1);
        addChallenge(challenge2);
        addChallenge(challenge3);

        System.out.println("Default challenges loaded");
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
