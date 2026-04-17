package com.kyotocuisine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection
 *
 * Opens a JDBC connection to the MySQL database.
 * Credentials are read from environment variables (or .env file in dev).
 *
 * This class uses ONLY the standard Java JDBC API (java.sql.*).
 * No frameworks are used here.
 */
public class DatabaseConnection {

    // Read config ONCE when the class is loaded.
    // We check System properties first (our .env loader puts values there),
    // then environment variables (used in Railway / production).
    private static final String DB_URL      = resolve("DB_URL");
    private static final String DB_USERNAME = resolve("DB_USERNAME");
    private static final String DB_PASSWORD = resolve("DB_PASSWORD");

    static {
        try {
            // Explicitly load the MySQL driver. Not required in modern JDBC,
            // but makes it obvious what driver we are using.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found", e);
        }
    }

    /**
     * Returns a new JDBC connection to the MySQL database.
     * Callers are responsible for closing the connection (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    /** Look up a config value from System property, then env var. */
    private static String resolve(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        return value;
    }
}
