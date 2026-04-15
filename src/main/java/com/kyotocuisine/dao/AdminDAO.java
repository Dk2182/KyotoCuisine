package com.kyotocuisine.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
public class AdminDAO {
    private final JdbcTemplate jdbc;

    public AdminDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Orders today
        Integer ordersToday = jdbc.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE DATE(placed_at) = CURDATE()", Integer.class);
        stats.put("ordersToday", ordersToday != null ? ordersToday : 0);

        // Revenue today
        BigDecimal revenueToday = jdbc.queryForObject(
            "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status = 'PAID' AND DATE(paid_at) = CURDATE()",
            BigDecimal.class);
        stats.put("revenueToday", revenueToday);

        // Bestselling item
        try {
            Map<String, Object> bestseller = jdbc.queryForMap(
                "SELECT mi.item_name, SUM(oi.quantity) AS total_qty FROM order_items oi " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.menu_item_id " +
                "GROUP BY mi.item_name ORDER BY total_qty DESC LIMIT 1");
            stats.put("bestsellerItem", bestseller.get("item_name"));
            stats.put("bestsellerQty", bestseller.get("total_qty"));
        } catch (Exception e) {
            stats.put("bestsellerItem", "N/A");
            stats.put("bestsellerQty", 0);
        }

        // Active reservations
        Integer activeReservations = jdbc.queryForObject(
            "SELECT COUNT(*) FROM reservations r JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
            "WHERE rs.status_name IN ('PENDING', 'CONFIRMED')", Integer.class);
        stats.put("activeReservations", activeReservations != null ? activeReservations : 0);

        // Total customers
        Integer totalCustomers = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE role_id = 1", Integer.class);
        stats.put("totalCustomers", totalCustomers != null ? totalCustomers : 0);

        return stats;
    }
}
