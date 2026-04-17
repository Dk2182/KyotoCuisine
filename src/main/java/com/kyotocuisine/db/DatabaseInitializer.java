package com.kyotocuisine.db;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * DatabaseInitializer
 *
 * Runs once at application startup. Reads schema.sql and data.sql from
 * the resources folder and executes each SQL statement using raw JDBC.
 *
 * This replaces what Spring Boot's auto-initializer used to do.
 * Everything here is plain Java - only @PostConstruct and @Component
 * are Spring annotations, used only to "wire" the class to run at startup.
 */
@Component
public class DatabaseInitializer {

    @PostConstruct
    public void initDatabase() {
        System.out.println("[DatabaseInitializer] Running schema.sql ...");
        runSqlFile("schema.sql");
        System.out.println("[DatabaseInitializer] Running data.sql ...");
        runSqlFile("data.sql");
        System.out.println("[DatabaseInitializer] Database ready.");
    }

    /**
     * Reads a .sql file from resources and executes each statement via JDBC.
     */
    private void runSqlFile(String fileName) {
        String sqlText = readResource(fileName);
        if (sqlText == null) {
            System.err.println("[DatabaseInitializer] Could not find " + fileName);
            return;
        }

        // Split on semicolons so each statement runs independently.
        String[] statements = sqlText.split(";");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : statements) {
                String trimmed = sql.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                try {
                    stmt.execute(trimmed);
                } catch (Exception e) {
                    // Ignore "already exists" / duplicate entry errors so
                    // restarts don't crash on seed data that was inserted already.
                    System.err.println("[DatabaseInitializer] Skipped statement: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /** Reads a file from src/main/resources/ as a String. */
    private String readResource(String fileName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Strip inline comments
                    if (line.trim().startsWith("--")) continue;
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
