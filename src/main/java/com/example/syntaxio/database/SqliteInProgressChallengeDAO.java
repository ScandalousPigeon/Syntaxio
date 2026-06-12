package com.example.syntaxio.database;

import com.example.syntaxio.model.InProgressChallenge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteInProgressChallengeDAO {
    private final Connection connection;

    public SqliteInProgressChallengeDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS in_progress_challenges (
                userId INTEGER NOT NULL,
                challengeId TEXT NOT NULL,
                draftCode TEXT DEFAULT '',
                startedAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL,
                PRIMARY KEY (userId, challengeId),
                FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (challengeId) REFERENCES challenges(id) ON DELETE CASCADE
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("In-progress challenges table ready");
        } catch (SQLException e) {
            System.err.println("Error creating in-progress challenges table: " + e.getMessage());
        }
    }

    public boolean saveOrUpdateInProgress(int userId, String challengeId, String draftCode) {
        String existingStartedAt = getStartedAt(userId, challengeId)
                .map(LocalDateTime::toString)
                .orElse(LocalDateTime.now().toString());
        String now = LocalDateTime.now().toString();
        String sql = """
            INSERT OR REPLACE INTO in_progress_challenges
                (userId, challengeId, draftCode, startedAt, updatedAt)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, challengeId);
            pstmt.setString(3, draftCode == null ? "" : draftCode);
            pstmt.setString(4, existingStartedAt);
            pstmt.setString(5, now);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving in-progress challenge: " + e.getMessage());
        }
        return false;
    }

    public Optional<InProgressChallenge> getInProgressForUserAndChallenge(int userId, String challengeId) {
        String sql = """
            SELECT userId, challengeId, draftCode, startedAt, updatedAt
            FROM in_progress_challenges
            WHERE userId = ? AND challengeId = ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, challengeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToInProgressChallenge(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting in-progress challenge: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<InProgressChallenge> getInProgressForUser(int userId) {
        List<InProgressChallenge> inProgressChallenges = new ArrayList<>();
        String sql = """
            SELECT userId, challengeId, draftCode, startedAt, updatedAt
            FROM in_progress_challenges
            WHERE userId = ?
            ORDER BY updatedAt DESC
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    inProgressChallenges.add(mapResultSetToInProgressChallenge(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user in-progress challenges: " + e.getMessage());
        }
        return inProgressChallenges;
    }

    public boolean removeInProgress(int userId, String challengeId) {
        String sql = "DELETE FROM in_progress_challenges WHERE userId = ? AND challengeId = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, challengeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing in-progress challenge: " + e.getMessage());
        }
        return false;
    }

    private Optional<LocalDateTime> getStartedAt(int userId, String challengeId) {
        String sql = "SELECT startedAt FROM in_progress_challenges WHERE userId = ? AND challengeId = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, challengeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(LocalDateTime.parse(rs.getString("startedAt")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting in-progress start time: " + e.getMessage());
        }
        return Optional.empty();
    }

    private InProgressChallenge mapResultSetToInProgressChallenge(ResultSet rs) throws SQLException {
        return new InProgressChallenge(
                rs.getInt("userId"),
                rs.getString("challengeId"),
                rs.getString("draftCode"),
                LocalDateTime.parse(rs.getString("startedAt")),
                LocalDateTime.parse(rs.getString("updatedAt"))
        );
    }
}
