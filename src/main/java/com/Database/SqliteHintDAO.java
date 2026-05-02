package com.Database;

import com.Model.Hint;

import java.time.LocalDateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteHintDAO {
    private Connection connection;
    
    public SqliteHintDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }
    
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS hints (
                id TEXT PRIMARY KEY,
                userId INTEGER NOT NULL,
                challengeId TEXT NOT NULL,
                hintText TEXT NOT NULL,
                hintType TEXT NOT NULL,
                confidence INTEGER NOT NULL,
                wasHelpful INTEGER DEFAULT 0,
                requestedAt TEXT NOT NULL,
                FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating hints table: " + e.getMessage());
        }
    }
    
    public void saveHint(int userId, Hint hint) {
        String sql = "INSERT INTO hints (id, userId, challengeId, hintText, hintType, confidence, wasHelpful, requestedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, hint.getId());
            pstmt.setInt(2, userId);
            pstmt.setString(3, hint.getChallengeId());
            pstmt.setString(4, hint.getHintText());
            pstmt.setString(5, hint.getHintType());
            pstmt.setInt(6, hint.getConfidence());
            pstmt.setInt(7, hint.isWasHelpful() ? 1 : 0);
            pstmt.setString(8, hint.getRequestedAt().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving hint: " + e.getMessage());
        }
    }
    
    public void markHintHelpful(String hintId, boolean helpful) {
        String sql = "UPDATE hints SET wasHelpful = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, helpful ? 1 : 0);
            pstmt.setString(2, hintId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating hint: " + e.getMessage());
        }
    }
    
    public List<Hint> getUserHintHistory(int userId, String challengeId) {
        List<Hint> hints = new ArrayList<>();
        String sql = "SELECT * FROM hints WHERE userId = ? AND challengeId = ? ORDER BY requestedAt DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, challengeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Hint hint = new Hint(
                    rs.getString("challengeId"),
                    rs.getString("hintText"),
                    rs.getString("hintType"),
                    rs.getInt("confidence")
                );
                hint.setId(rs.getString("id"));
                hint.setWasHelpful(rs.getInt("wasHelpful") == 1);
                hint.setRequestedAt(LocalDateTime.parse(rs.getString("requestedAt")));
                hints.add(hint);
            }
        } catch (SQLException e) {
            System.err.println("Error getting hint history: " + e.getMessage());
        }
        return hints;
    }    
}
