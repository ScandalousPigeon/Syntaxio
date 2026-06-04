package com.example.syntaxio.database;

import com.example.syntaxio.model.User;
import com.example.syntaxio.ui.util.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class SqliteUserDAO {
    private Connection connection;
    private Set<String> userColumns = new HashSet<>();
    
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
                password_hash TEXT NOT NULL,
                created_at TEXT NOT NULL,
                last_login_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                login_count INTEGER DEFAULT 0,
                totalHintsUsed INTEGER DEFAULT 0,
                totalChallengesCompleted INTEGER DEFAULT 0
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            ensureUserColumns();
            System.out.println("Users table ready");
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }
    
    // INSERT a new user
    public boolean addUser(User user) {
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        addColumnValue(columns, values, "username", user.getUsername());
        addColumnValue(columns, values, "password_hash", user.getPasswordHash());
        addColumnValue(columns, values, "passwordHash", user.getPasswordHash());
        addColumnValue(columns, values, "created_at", formatDateTime(user.getCreatedAt()));
        addColumnValue(columns, values, "createdAt", formatDateTime(user.getCreatedAt()));
        addColumnValue(columns, values, "last_login_at", formatDateTime(user.getLastLoginAt()));
        addColumnValue(columns, values, "lastLoginAt", formatDateTime(user.getLastLoginAt()));
        addColumnValue(columns, values, "updated_at", formatDateTime(user.getUpdatedAt()));
        addColumnValue(columns, values, "login_count", user.getLoginCount());
        addColumnValue(columns, values, "totalHintsUsed", user.getTotalHintsUsed());
        addColumnValue(columns, values, "totalChallengesCompleted", user.getTotalChallengesCompleted());

        String sql = "INSERT INTO users (" + String.join(", ", columns)
                + ") VALUES (" + placeholders(columns.size()) + ")";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindValues(pstmt, values);
            
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
        user.setUpdatedAt(LocalDateTime.now());

        List<String> assignments = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        addAssignmentValue(assignments, values, "username", user.getUsername());
        addAssignmentValue(assignments, values, "password_hash", user.getPasswordHash());
        addAssignmentValue(assignments, values, "passwordHash", user.getPasswordHash());
        addAssignmentValue(assignments, values, "last_login_at", formatDateTime(user.getLastLoginAt()));
        addAssignmentValue(assignments, values, "lastLoginAt", formatDateTime(user.getLastLoginAt()));
        addAssignmentValue(assignments, values, "updated_at", formatDateTime(user.getUpdatedAt()));
        addAssignmentValue(assignments, values, "login_count", user.getLoginCount());
        addAssignmentValue(assignments, values, "totalHintsUsed", user.getTotalHintsUsed());
        addAssignmentValue(assignments, values, "totalChallengesCompleted", user.getTotalChallengesCompleted());

        if (assignments.isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET " + String.join(", ", assignments) + " WHERE id = ?";
        values.add(user.getId());
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            bindValues(pstmt, values);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }
    
    // UPDATE last login time only
    public boolean updateLastLogin(String username, LocalDateTime loginTime) {
        List<String> assignments = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        addAssignmentValue(assignments, values, "last_login_at", formatDateTime(loginTime));
        addAssignmentValue(assignments, values, "lastLoginAt", formatDateTime(loginTime));
        addRawAssignment(assignments, "login_count", "COALESCE(login_count, 0) + 1");

        if (assignments.isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET " + String.join(", ", assignments) + " WHERE username = ?";
        values.add(username);
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            bindValues(pstmt, values);
            
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
            boolean verified = user.getPasswordHash() != null
                    && PasswordHasher.verifyPassword(plainPassword, user.getPasswordHash());
            if (verified) {
                updateLastLogin(username, LocalDateTime.now());
            }
            return verified;
        }
        return false;
    }
    
    // Helper: Convert ResultSet row to User object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        LocalDateTime createdAt = parseDateTime(getString(rs, "created_at", "createdAt"), LocalDateTime.now());
        LocalDateTime lastLoginAt = parseDateTime(getString(rs, "last_login_at", "lastLoginAt"), createdAt);
        LocalDateTime updatedAt = parseDateTime(getString(rs, "updated_at", null), lastLoginAt);

        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            getString(rs, "password_hash", "passwordHash"),
            createdAt,
            lastLoginAt,
            updatedAt,
            getInt(rs, "login_count", 0),
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

    private void ensureUserColumns() throws SQLException {
        refreshUserColumns();
        addColumnIfMissing("password_hash", "TEXT");
        addColumnIfMissing("created_at", "TEXT");
        addColumnIfMissing("last_login_at", "TEXT");
        addColumnIfMissing("updated_at", "TEXT");
        addColumnIfMissing("login_count", "INTEGER DEFAULT 0");
        refreshUserColumns();
        backfillNewUserColumns();
    }

    private void refreshUserColumns() throws SQLException {
        userColumns.clear();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)")) {
            while (rs.next()) {
                userColumns.add(rs.getString("name").toLowerCase(Locale.ROOT));
            }
        }
    }

    private void addColumnIfMissing(String columnName, String definition) throws SQLException {
        if (!columnExists(columnName)) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN " + columnName + " " + definition);
            }
            userColumns.add(columnName.toLowerCase(Locale.ROOT));
        }
    }

    private void backfillNewUserColumns() throws SQLException {
        String now = LocalDateTime.now().toString();

        if (columnExists("password_hash") && columnExists("passwordHash")) {
            executeUpdate("UPDATE users SET password_hash = passwordHash WHERE password_hash IS NULL");
        }

        if (columnExists("created_at") && columnExists("createdAt")) {
            executeUpdate("UPDATE users SET created_at = createdAt WHERE created_at IS NULL");
        }
        backfillNullTimestamp("created_at", now);

        if (columnExists("last_login_at") && columnExists("lastLoginAt")) {
            executeUpdate("UPDATE users SET last_login_at = lastLoginAt WHERE last_login_at IS NULL");
        }
        backfillNullTimestamp("last_login_at", now);

        if (columnExists("updated_at")) {
            if (columnExists("last_login_at") && columnExists("created_at")) {
                try (PreparedStatement pstmt = connection.prepareStatement(
                        "UPDATE users SET updated_at = COALESCE(last_login_at, created_at, ?) WHERE updated_at IS NULL")) {
                    pstmt.setString(1, now);
                    pstmt.executeUpdate();
                }
            } else {
                backfillNullTimestamp("updated_at", now);
            }
        }

        if (columnExists("login_count")) {
            executeUpdate("UPDATE users SET login_count = 0 WHERE login_count IS NULL");
        }
    }

    private void backfillNullTimestamp(String columnName, String value) throws SQLException {
        if (!columnExists(columnName)) {
            return;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE users SET " + columnName + " = ? WHERE " + columnName + " IS NULL")) {
            pstmt.setString(1, value);
            pstmt.executeUpdate();
        }
    }

    private void executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private boolean columnExists(String columnName) {
        return userColumns.contains(columnName.toLowerCase(Locale.ROOT));
    }

    private void addColumnValue(List<String> columns, List<Object> values, String columnName, Object value) {
        if (columnExists(columnName)) {
            columns.add(columnName);
            values.add(value);
        }
    }

    private void addAssignmentValue(List<String> assignments, List<Object> values,
                                    String columnName, Object value) {
        if (columnExists(columnName)) {
            assignments.add(columnName + " = ?");
            values.add(value);
        }
    }

    private void addRawAssignment(List<String> assignments, String columnName, String expression) {
        if (columnExists(columnName)) {
            assignments.add(columnName + " = " + expression);
        }
    }

    private void bindValues(PreparedStatement pstmt, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) value);
            } else {
                pstmt.setString(i + 1, (String) value);
            }
        }
    }

    private String placeholders(int count) {
        List<String> placeholders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            placeholders.add("?");
        }
        return String.join(", ", placeholders);
    }

    private String getString(ResultSet rs, String preferredColumn, String fallbackColumn) throws SQLException {
        if (preferredColumn != null && columnExists(preferredColumn)) {
            String value = rs.getString(preferredColumn);
            if (value != null) {
                return value;
            }
        }

        if (fallbackColumn != null && columnExists(fallbackColumn)) {
            return rs.getString(fallbackColumn);
        }

        return null;
    }

    private int getInt(ResultSet rs, String columnName, int defaultValue) throws SQLException {
        if (!columnExists(columnName)) {
            return defaultValue;
        }

        int value = rs.getInt(columnName);
        return rs.wasNull() ? defaultValue : value;
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return (dateTime != null ? dateTime : LocalDateTime.now()).toString();
    }
}
