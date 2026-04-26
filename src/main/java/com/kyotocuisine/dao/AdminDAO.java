package com.kyotocuisine.dao;

import com.kyotocuisine.db.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

// Admin stats queries.
@Repository
public class AdminDAO {

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Orders today.
            stats.put("ordersToday", runIntQuery(conn,
                "SELECT COUNT(*) FROM orders WHERE DATE(placed_at) = CURDATE()"));

            // Revenue today.
            stats.put("revenueToday", runDecimalQuery(conn,
                "SELECT COALESCE(SUM(amount), 0) FROM payments " +
                "WHERE payment_status = 'PAID' AND DATE(paid_at) = CURDATE()"));

            // Bestselling item.
            String bestsellerSql =
                "SELECT mi.item_name, SUM(oi.quantity) AS total_qty " +
                "FROM order_items oi " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.menu_item_id " +
                "GROUP BY mi.item_name ORDER BY total_qty DESC LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(bestsellerSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("bestsellerItem", rs.getString("item_name"));
                    stats.put("bestsellerQty", rs.getInt("total_qty"));
                } else {
                    stats.put("bestsellerItem", "N/A");
                    stats.put("bestsellerQty", 0);
                }
            }

            // Active reservations.
            stats.put("activeReservations", runIntQuery(conn,
                "SELECT COUNT(*) FROM reservations r " +
                "JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
                "WHERE rs.status_name IN ('PENDING','CONFIRMED')"));

            // Registered customers.
            stats.put("totalCustomers", runIntQuery(conn,
                "SELECT COUNT(*) FROM users WHERE role_id = 1"));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate admin stats", e);
        }

        return stats;
    }

    // Returns a single integer.
    private int runIntQuery(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    }

    // Returns a single decimal.
    private BigDecimal runDecimalQuery(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal value = rs.getBigDecimal(1);
                return value != null ? value : BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        }
    }
}
