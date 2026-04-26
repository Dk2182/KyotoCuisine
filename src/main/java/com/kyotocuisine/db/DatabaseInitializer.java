package com.kyotocuisine.db;

import jakarta.annotation.PostConstruct;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// Runs schema and data on startup.
@Component
public class DatabaseInitializer {

    @PostConstruct
    public void initDatabase() {
        System.out.println("[DatabaseInitializer] Running schema.sql ...");
        runSqlFile("schema.sql");
        System.out.println("[DatabaseInitializer] Running data.sql ...");
        runSqlFile("data.sql");
        System.out.println("[DatabaseInitializer] Seeding admin and staff accounts ...");
        seedAdminAndStaff();
        System.out.println("[DatabaseInitializer] Database ready.");
    }

    // Run a SQL file.

    private void runSqlFile(String fileName) {
        String sqlText = readResource(fileName);
        if (sqlText == null) {
            System.err.println("[DatabaseInitializer] Could not find " + fileName);
            return;
        }
        String[] statements = sqlText.split(";");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                try {
                    stmt.execute(trimmed);
                } catch (Exception e) {
                    System.err.println("[DatabaseInitializer] Skipped statement: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private String readResource(String fileName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("--")) continue;
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // Seed admin and staff if missing.
    private void seedAdminAndStaff() {
        String adminEmail = resolveEnv("ADMIN_EMAIL");
        String adminPassword = resolveEnv("ADMIN_PASSWORD");
        String staffEmail = resolveEnv("STAFF_EMAIL");
        String staffPassword = resolveEnv("STAFF_PASSWORD");

        if (adminEmail != null && adminPassword != null) {
            createUserIfMissing(adminEmail, adminPassword, /*roleId*/ 3, "Kyoto", "Admin", null);
        } else {
            System.out.println("[DatabaseInitializer] ADMIN_EMAIL / ADMIN_PASSWORD not set - skipping admin seed.");
        }

        if (staffEmail != null && staffPassword != null) {
            int staffUserId = createUserIfMissing(staffEmail, staffPassword, /*roleId*/ 2, "Staff", "Member", "Waiter");
            // Ensure staff_profiles row exists.
            if (staffUserId > 0) ensureStaffProfile(staffUserId, "Waiter");
        } else {
            System.out.println("[DatabaseInitializer] STAFF_EMAIL / STAFF_PASSWORD not set - skipping staff seed.");
        }
    }

    private String resolveEnv(String key) {
        String v = System.getProperty(key);
        if (v == null || v.isEmpty()) v = System.getenv(key);
        return (v == null || v.isEmpty()) ? null : v;
    }

    // Create user if missing.
    private int createUserIfMissing(String email, String plainPassword, int roleId,
                                    String firstName, String lastName, String staffPosition) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if user exists.
            try (PreparedStatement check = conn.prepareStatement("SELECT user_id FROM users WHERE email = ?")) {
                check.setString(1, email);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[DatabaseInitializer] User " + email + " already exists - left untouched.");
                        return rs.getInt(1);
                    }
                }
            }

            String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO users (role_id, first_name, last_name, email, password_hash, is_active) VALUES (?, ?, ?, ?, ?, TRUE)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ins.setInt(1, roleId);
                ins.setString(2, firstName);
                ins.setString(3, lastName);
                ins.setString(4, email);
                ins.setString(5, hash);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        System.out.println("[DatabaseInitializer] Created user " + email + " (id " + id + ")");
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Could not seed user " + email + ": " + e.getMessage());
        }
        return -1;
    }

    private void ensureStaffProfile(int userId, String position) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement check = conn.prepareStatement("SELECT staff_id FROM staff_profiles WHERE user_id = ?")) {
                check.setInt(1, userId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) return;
                }
            }
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO staff_profiles (user_id, staff_position, hire_date) VALUES (?, ?, CURDATE())")) {
                ins.setInt(1, userId);
                ins.setString(2, position);
                ins.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Could not ensure staff profile: " + e.getMessage());
        }
    }
}
