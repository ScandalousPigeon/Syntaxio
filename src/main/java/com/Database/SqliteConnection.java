package com.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnection {
    private static Connection instance = null;

    private SqliteConnection() {
        String url = "jdbc:sqlite:codebuddy.db";  // Database file name
        try {
            instance = DriverManager.getConnection(url);
            System.out.println("SQLite database connected successfully");
        } catch (SQLException sqlEx) {
            System.err.println("Failed to connect to database: " + sqlEx.getMessage());
            sqlEx.printStackTrace();
        }
    }

    public static synchronized Connection getInstance() {
        if (instance == null) {
            new SqliteConnection();
        }
        return instance;
    }
    
    public static void closeConnection() {
        if (instance != null) {
            try {
                instance.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }
}
