package com.kyotocuisine.dao;

import com.kyotocuisine.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogDAO {
    private final JdbcTemplate jdbc;

    private final RowMapper<AuditLog> rowMapper = (rs, rowNum) -> {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));
        log.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        log.setActionType(rs.getString("action_type"));
        log.setEntityName(rs.getString("entity_name"));
        log.setEntityId(rs.getInt("entity_id"));
        log.setActionDetails(rs.getString("action_details"));
        log.setLoggedAt(rs.getTimestamp("logged_at").toLocalDateTime());
        try { log.setUserName(rs.getString("user_name")); } catch (Exception e) {}
        return log;
    };

    public AuditLogDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertLog(Integer userId, String actionType, String entityName, int entityId, String details) {
        jdbc.update(
            "INSERT INTO audit_logs (user_id, action_type, entity_name, entity_id, action_details) VALUES (?, ?, ?, ?, ?)",
            userId, actionType, entityName, entityId, details);
    }

    public List<AuditLog> findAll() {
        return jdbc.query(
            "SELECT al.*, CONCAT(u.first_name, ' ', u.last_name) AS user_name " +
            "FROM audit_logs al LEFT JOIN users u ON al.user_id = u.user_id " +
            "ORDER BY al.logged_at DESC LIMIT 200",
            rowMapper);
    }
}
