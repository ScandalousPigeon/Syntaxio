package com.example.syntaxio.database;

import com.example.syntaxio.model.Solution;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteSolutionDAO {
    private Connection connection;
    
    public SqliteSolutionDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }
    
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS solutions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                challengeId TEXT NOT NULL,
                code TEXT NOT NULL,
                passed INTEGER NOT NULL,
                hintsUsed INTEGER DEFAULT 0,
                submittedAt TEXT NOT NULL,
                FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Solutions table ready");
        } catch (SQLException e) {
            System.err.println("Error creating solutions table: " + e.getMessage());
        }
    }
    
    // Add a solution
    public boolean addSolution(int userId, Solution solution) {
        String sql = "INSERT INTO solutions (userId, challengeId, code, passed, hintsUsed, submittedAt) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, solution.getChallengeId());
            pstmt.setString(3, solution.getCode());
            pstmt.setInt(4, solution.isPassed() ? 1 : 0);
            pstmt.setInt(5, solution.getHintsUsedForThisSolution());
            pstmt.setString(6, solution.getSubmittedAt().toString());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding solution: " + e.getMessage());
        }
        return false;
    }
    
    // Get all solutions for a user
    public List<Solution> getSolutionsByUserId(int userId) {
        List<Solution> solutions = new ArrayList<>();
        String sql = "SELECT * FROM solutions WHERE userId = ? ORDER BY submittedAt DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Solution solution = new Solution(
                    rs.getString("challengeId"),
                    rs.getString("code"),
                    rs.getInt("passed") == 1,
                    rs.getInt("hintsUsed")
                );
                solution.setSubmittedAt(LocalDateTime.parse(rs.getString("submittedAt")));
                solutions.add(solution);
            }
        } catch (SQLException e) {
            System.err.println("Error getting solutions: " + e.getMessage());
        }
        return solutions;
    }
    
    // Get solutions for a specific challenge
    public List<Solution> getSolutionsByUserAndChallenge(int userId, String challengeId) {
        List<Solution> solutions = new ArrayList<>();
        String sql = "SELECT * FROM solutions WHERE userId = ? AND challengeId = ? ORDER BY submittedAt DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, challengeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Solution solution = new Solution(
                    rs.getString("challengeId"),
                    rs.getString("code"),
                    rs.getInt("passed") == 1,
                    rs.getInt("hintsUsed")
                );
                solution.setSubmittedAt(LocalDateTime.parse(rs.getString("submittedAt")));
                solutions.add(solution);
            }
        } catch (SQLException e) {
            System.err.println("Error getting solutions by challenge: " + e.getMessage());
        }
        return solutions;
    }
}
