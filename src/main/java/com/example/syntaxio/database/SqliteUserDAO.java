package com.example.syntaxio.database;

import com.example.syntaxio.model.User;
import com.example.syntaxio.ui.util.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteUserDAO {
    private Connection connection;
    
    public SqliteUserDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }
    
    // Create users table if it doesn't exist
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                passwordHash TEXT NOT NULL,
                createdAt TEXT NOT NULL,
                lastLoginAt TEXT NOT NULL,
                totalHintsUsed INTEGER DEFAULT 0,
                totalChallengesCompleted INTEGER DEFAULT 0
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table ready");
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }
    
    // INSERT a new user
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, passwordHash, createdAt, lastLoginAt, totalHintsUsed, totalChallengesCompleted) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getCreatedAt().toString());
            pstmt.setString(4, user.getLastLoginAt().toString());
            pstmt.setInt(5, user.getTotalHintsUsed());
            pstmt.setInt(6, user.getTotalChallengesCompleted());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
        return false;
    }
    
    // SELECT user by username
    public Optional<User> findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    // SELECT user by ID
    public Optional<User> findUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    // SELECT all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }
    
    // UPDATE user
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, passwordHash = ?, lastLoginAt = ?, totalHintsUsed = ?, totalChallengesCompleted = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getLastLoginAt().toString());
            pstmt.setInt(4, user.getTotalHintsUsed());
            pstmt.setInt(5, user.getTotalChallengesCompleted());
            pstmt.setInt(6, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }
    
    // UPDATE last login time only
    public boolean updateLastLogin(String username, LocalDateTime loginTime) {
        String sql = "UPDATE users SET lastLoginAt = ? WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, loginTime.toString());
            pstmt.setString(2, username);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
        return false;
    }
    
    // DELETE user
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }
    
    // Check if user exists
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        return false;
    }
    
    // Verify login credentials
    public boolean verifyLogin(String username, String plainPassword) {
        Optional<User> userOpt = findUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean verified = PasswordHasher.verifyPassword(plainPassword, user.getPasswordHash());
            if (verified) {
                updateLastLogin(username, LocalDateTime.now());
            }
            return verified;
        }
        return false;
    }
    
    // Helper: Convert ResultSet row to User object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("passwordHash"),
            LocalDateTime.parse(rs.getString("createdAt")),
            LocalDateTime.parse(rs.getString("lastLoginAt")),
            rs.getInt("totalHintsUsed"),
            rs.getInt("totalChallengesCompleted")
        );
    }
    
    // Get total number of users
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user count: " + e.getMessage());
        }
        return 0;
    }    
}
