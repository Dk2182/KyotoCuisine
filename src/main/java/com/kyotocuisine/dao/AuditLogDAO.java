package com.kyotocuisine.dao;

import com.kyotocuisine.db.DatabaseConnection;
import com.kyotocuisine.model.AuditLog;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogDAO - records and reads audit log entries.
 * Uses pure JDBC.
 */
@Repository
public class AuditLogDAO {

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));
        int uid = rs.getInt("user_id");
        log.setUserId(rs.wasNull() ? null : uid);
        log.setActionType(rs.getString("action_type"));
        log.setEntityName(rs.getString("entity_name"));
        log.setEntityId(rs.getInt("entity_id"));
        log.setActionDetails(rs.getString("action_details"));
        log.setLoggedAt(rs.getTimestamp("logged_at").toLocalDateTime());
        try { log.setUserName(rs.getString("user_name")); } catch (SQLException ignore) {}
        return log;
    }

    public void insertLog(Integer userId, String actionType, String entityName, int entityId, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action_type, entity_name, entity_id, action_details) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId != null) ps.setInt(1, userId);
            else ps.setNull(1, Types.INTEGER);

            ps.setString(2, actionType);
            ps.setString(3, entityName);
            ps.setInt(4, entityId);
            ps.setString(5, details);

            ps.executeUpdate();

        } catch (SQLException e) {
            // Logging failures should never break the main flow.
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public List<AuditLog> findAll() {
        String sql = "SELECT al.*, CONCAT(u.first_name, ' ', u.last_name) AS user_name " +
                     "FROM audit_logs al LEFT JOIN users u ON al.user_id = u.user_id " +
                     "ORDER BY al.logged_at DESC LIMIT 200";

        List<AuditLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) logs.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load audit logs", e);
        }
        return logs;
    }
}
