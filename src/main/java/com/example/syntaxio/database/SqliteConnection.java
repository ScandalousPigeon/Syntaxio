package com.example.syntaxio.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnection {
    public static final String DATABASE_PATH_PROPERTY = "syntaxio.database.path";
    private static Connection instance = null;

    private SqliteConnection() {
        String databasePath = System.getProperty(DATABASE_PATH_PROPERTY, "syntaxio.db");
        String url = "jdbc:sqlite:" + databasePath;  // database file name
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
                System.out.println("database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }
}
