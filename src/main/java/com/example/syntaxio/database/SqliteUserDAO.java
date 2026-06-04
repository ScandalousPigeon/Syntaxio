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
    private static final String EMPTY_JSON = "{}";
    private static final String EMPTY_JSON_ARRAY = "[]";
    private static final ColumnDefinition[] PROGRESS_STAT_COLUMNS = {
            new ColumnDefinition("total_challenges_completed", "INTEGER DEFAULT 0"),
            new ColumnDefinition("total_challenges_attempted", "INTEGER DEFAULT 0"),
            new ColumnDefinition("total_unfinished_challenges", "INTEGER DEFAULT 0"),
            new ColumnDefinition("completion_percentage", "REAL DEFAULT 0.0"),
            new ColumnDefinition("first_completed_challenge_date", "TEXT"),
            new ColumnDefinition("most_recent_completed_challenge_date", "TEXT"),
            new ColumnDefinition("total_attempts", "INTEGER DEFAULT 0"),
            new ColumnDefinition("attempts_per_challenge", "TEXT DEFAULT '{}'"),
            new ColumnDefinition("average_attempts_before_completion", "REAL DEFAULT 0.0"),
            new ColumnDefinition("failed_attempts", "INTEGER DEFAULT 0"),
            new ColumnDefinition("successful_attempts", "INTEGER DEFAULT 0"),
            new ColumnDefinition("most_attempted_challenge", "TEXT"),
            new ColumnDefinition("total_time_spent_coding", "INTEGER DEFAULT 0"),
            new ColumnDefinition("time_spent_per_challenge", "TEXT DEFAULT '{}'"),
            new ColumnDefinition("average_completion_time", "REAL DEFAULT 0.0"),
            new ColumnDefinition("fastest_completion_time", "INTEGER DEFAULT 0"),
            new ColumnDefinition("slowest_completion_time", "INTEGER DEFAULT 0"),
            new ColumnDefinition("time_spent_by_difficulty", "TEXT DEFAULT '{}'"),
            new ColumnDefinition("time_spent_today", "INTEGER DEFAULT 0"),
            new ColumnDefinition("time_spent_this_week", "INTEGER DEFAULT 0"),
            new ColumnDefinition("time_spent_this_month", "INTEGER DEFAULT 0"),
            new ColumnDefinition("challenges_completed_per_topic", "TEXT DEFAULT '{}'"),
            new ColumnDefinition("average_score_per_topic", "TEXT DEFAULT '{}'"),
            new ColumnDefinition("weakest_topic", "TEXT"),
            new ColumnDefinition("strongest_topic", "TEXT"),
            new ColumnDefinition("topic_improvement_over_time", "TEXT DEFAULT '[]'"),
            new ColumnDefinition("easy_challenges_completed", "INTEGER DEFAULT 0"),
            new ColumnDefinition("medium_challenges_completed", "INTEGER DEFAULT 0"),
            new ColumnDefinition("hard_challenges_completed", "INTEGER DEFAULT 0"),
            new ColumnDefinition("current_recommended_difficulty", "TEXT DEFAULT 'EASY'"),
            new ColumnDefinition("highest_difficulty_completed", "TEXT"),
            new ColumnDefinition("difficulty_success_rate", "TEXT DEFAULT '{}'"),
            new ColumnDefinition("error_tracking", "TEXT DEFAULT '{}'"),
            new ColumnDefinition("last_login_date", "TEXT"),
            new ColumnDefinition("total_login_count", "INTEGER DEFAULT 0"),
            new ColumnDefinition("days_active", "INTEGER DEFAULT 0"),
            new ColumnDefinition("current_activity_streak", "INTEGER DEFAULT 0"),
            new ColumnDefinition("longest_activity_streak", "INTEGER DEFAULT 0"),
            new ColumnDefinition("challenges_completed_today", "INTEGER DEFAULT 0"),
            new ColumnDefinition("challenges_completed_this_week", "INTEGER DEFAULT 0"),
            new ColumnDefinition("number_of_coding_sessions", "INTEGER DEFAULT 0"),
            new ColumnDefinition("average_session_length", "REAL DEFAULT 0.0"),
            new ColumnDefinition("longest_session", "INTEGER DEFAULT 0"),
            new ColumnDefinition("shortest_session", "INTEGER DEFAULT 0"),
            new ColumnDefinition("last_session_duration", "INTEGER DEFAULT 0"),
            new ColumnDefinition("challenges_attempted_per_session", "TEXT DEFAULT '[]'")
    };
    private static final String[] ZERO_INTEGER_COLUMNS = {
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
    };
    private static final String[] ZERO_REAL_COLUMNS = {
            "completion_percentage",
            "average_attempts_before_completion",
            "average_completion_time",
            "average_session_length"
    };
    private static final String[] EMPTY_JSON_COLUMNS = {
            "attempts_per_challenge",
            "time_spent_per_challenge",
            "time_spent_by_difficulty",
            "challenges_completed_per_topic",
            "average_score_per_topic",
            "difficulty_success_rate",
            "error_tracking"
    };
    private static final String[] EMPTY_JSON_ARRAY_COLUMNS = {
            "topic_improvement_over_time",
            "challenges_attempted_per_session"
    };

    private Connection connection;
    private Set<String> userColumns = new HashSet<>();
    
    public SqliteUserDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }
    
    // Create users table if it doesn't exist
    private void createTable() {
        String sql = createUserTableSql();
        
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
        addColumnValue(columns, values, "total_login_count", user.getLoginCount());
        addColumnValue(columns, values, "last_login_date", formatDate(user.getLastLoginAt()));
        addColumnValue(columns, values, "totalHintsUsed", user.getTotalHintsUsed());
        addColumnValue(columns, values, "totalChallengesCompleted", user.getTotalChallengesCompleted());
        addColumnValue(columns, values, "total_challenges_completed", user.getTotalChallengesCompleted());

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
        addAssignmentValue(assignments, values, "total_login_count", user.getLoginCount());
        addAssignmentValue(assignments, values, "last_login_date", formatDate(user.getLastLoginAt()));
        addAssignmentValue(assignments, values, "totalHintsUsed", user.getTotalHintsUsed());
        addAssignmentValue(assignments, values, "totalChallengesCompleted", user.getTotalChallengesCompleted());
        addAssignmentValue(assignments, values, "total_challenges_completed", user.getTotalChallengesCompleted());

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
        addAssignmentValue(assignments, values, "last_login_date", formatDate(loginTime));
        addRawAssignment(assignments, "login_count", "COALESCE(login_count, 0) + 1");
        addRawAssignment(assignments, "total_login_count", "COALESCE(total_login_count, 0) + 1");

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
        int loginCount = Math.max(getInt(rs, "login_count", 0), getInt(rs, "total_login_count", 0));
        int completedChallenges = Math.max(
                getInt(rs, "totalChallengesCompleted", 0),
                getInt(rs, "total_challenges_completed", 0)
        );

        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            getString(rs, "password_hash", "passwordHash"),
            createdAt,
            lastLoginAt,
            updatedAt,
            loginCount,
            rs.getInt("totalHintsUsed"),
            completedChallenges
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

    private String createUserTableSql() {
        StringBuilder sql = new StringBuilder("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at TEXT NOT NULL,
                last_login_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                login_count INTEGER DEFAULT 0,
                totalHintsUsed INTEGER DEFAULT 0,
                totalChallengesCompleted INTEGER DEFAULT 0""");

        for (ColumnDefinition column : PROGRESS_STAT_COLUMNS) {
            sql.append(",\n                ")
                    .append(column.name())
                    .append(" ")
                    .append(column.definition());
        }

        sql.append("""

            )
            """);
        return sql.toString();
    }

    private void ensureUserColumns() throws SQLException {
        refreshUserColumns();
        addColumnIfMissing("password_hash", "TEXT");
        addColumnIfMissing("created_at", "TEXT");
        addColumnIfMissing("last_login_at", "TEXT");
        addColumnIfMissing("updated_at", "TEXT");
        addColumnIfMissing("login_count", "INTEGER DEFAULT 0");
        for (ColumnDefinition column : PROGRESS_STAT_COLUMNS) {
            addColumnIfMissing(column.name(), column.definition());
        }
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

        backfillProgressStatDefaults();
        syncProgressStatAliases();
    }

    private void backfillProgressStatDefaults() throws SQLException {
        for (String columnName : ZERO_INTEGER_COLUMNS) {
            backfillNullNumber(columnName, 0);
        }

        for (String columnName : ZERO_REAL_COLUMNS) {
            backfillNullNumber(columnName, 0.0);
        }

        for (String columnName : EMPTY_JSON_COLUMNS) {
            backfillNullText(columnName, EMPTY_JSON);
        }

        for (String columnName : EMPTY_JSON_ARRAY_COLUMNS) {
            backfillNullText(columnName, EMPTY_JSON_ARRAY);
        }

        backfillNullText("current_recommended_difficulty", "EASY");
        backfillLastLoginDate();
    }

    private void syncProgressStatAliases() throws SQLException {
        syncIntegerColumnPair("login_count", "total_login_count");
        syncIntegerColumnPair("totalChallengesCompleted", "total_challenges_completed");
    }

    private void syncIntegerColumnPair(String firstColumn, String secondColumn) throws SQLException {
        if (!columnExists(firstColumn) || !columnExists(secondColumn)) {
            return;
        }

        String maxExpression = "MAX(COALESCE(" + firstColumn + ", 0), COALESCE(" + secondColumn + ", 0))";
        executeUpdate("UPDATE users SET " + firstColumn + " = " + maxExpression);
        executeUpdate("UPDATE users SET " + secondColumn + " = " + maxExpression);
    }

    private void backfillLastLoginDate() throws SQLException {
        if (!columnExists("last_login_date")) {
            return;
        }

        if (columnExists("last_login_at")) {
            executeUpdate("""
                    UPDATE users
                    SET last_login_date = substr(last_login_at, 1, 10)
                    WHERE (last_login_date IS NULL OR last_login_date = '')
                      AND last_login_at IS NOT NULL
                    """);
        }

        if (columnExists("lastLoginAt")) {
            executeUpdate("""
                    UPDATE users
                    SET last_login_date = substr(lastLoginAt, 1, 10)
                    WHERE (last_login_date IS NULL OR last_login_date = '')
                      AND lastLoginAt IS NOT NULL
                    """);
        }
    }

    private void backfillNullNumber(String columnName, Number value) throws SQLException {
        if (!columnExists(columnName)) {
            return;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE users SET " + columnName + " = ? WHERE " + columnName + " IS NULL")) {
            if (value instanceof Double || value instanceof Float) {
                pstmt.setDouble(1, value.doubleValue());
            } else {
                pstmt.setLong(1, value.longValue());
            }
            pstmt.executeUpdate();
        }
    }

    private void backfillNullText(String columnName, String value) throws SQLException {
        if (!columnExists(columnName)) {
            return;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE users SET " + columnName + " = ? WHERE " + columnName + " IS NULL")) {
            pstmt.setString(1, value);
            pstmt.executeUpdate();
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
            if (value == null) {
                pstmt.setObject(i + 1, null);
            } else if (value instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) value);
            } else if (value instanceof Long) {
                pstmt.setLong(i + 1, (Long) value);
            } else if (value instanceof Double || value instanceof Float) {
                pstmt.setDouble(i + 1, ((Number) value).doubleValue());
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

    private String formatDate(LocalDateTime dateTime) {
        return (dateTime != null ? dateTime : LocalDateTime.now()).toLocalDate().toString();
    }

    private record ColumnDefinition(String name, String definition) {
    }
}
