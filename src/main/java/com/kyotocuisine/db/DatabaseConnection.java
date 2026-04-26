package com.kyotocuisine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Opens MySQL connections.
public class DatabaseConnection {

    // Read once at class load.
    private static final String DB_URL      = resolve("DB_URL");
    private static final String DB_USERNAME = resolve("DB_USERNAME");
    private static final String DB_PASSWORD = resolve("DB_PASSWORD");

    static {
        try {
            // Load MySQL driver.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found", e);
        }
    }

    // Returns a new connection.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    // Read system property or env var.
    private static String resolve(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        return value;
    }
}
