package com.kyotocuisine.dao;

import com.kyotocuisine.db.DatabaseConnection;
import com.kyotocuisine.model.Order;
import com.kyotocuisine.model.OrderItem;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Orders and order items.
@Repository
public class OrderDAO {

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        int cid = rs.getInt("customer_id");
        o.setCustomerId(rs.wasNull() ? null : cid);
        o.setOrderStatusId(rs.getInt("order_status_id"));
        o.setOrderType(rs.getString("order_type"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setPlacedAt(rs.getTimestamp("placed_at").toLocalDateTime());
        o.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        Timestamp pt = rs.getTimestamp("pickup_time");
        if (pt != null) o.setPickupTime(pt.toLocalDateTime());
        o.setNotes(rs.getString("notes"));
        try { o.setStatusName(rs.getString("status_name")); } catch (SQLException ignore) {}
        try { o.setCustomerName(rs.getString("customer_name")); } catch (SQLException ignore) {}
        return o;
    }

    private OrderItem mapItem(ResultSet rs) throws SQLException {
        OrderItem oi = new OrderItem();
        oi.setOrderItemId(rs.getInt("order_item_id"));
        oi.setOrderId(rs.getInt("order_id"));
        oi.setMenuItemId(rs.getInt("menu_item_id"));
        oi.setQuantity(rs.getInt("quantity"));
        oi.setUnitPrice(rs.getBigDecimal("unit_price"));
        oi.setLineTotal(rs.getBigDecimal("line_total"));
        oi.setSpecialInstruction(rs.getString("special_instruction"));
        try { oi.setItemName(rs.getString("item_name")); } catch (SQLException ignore) {}
        return oi;
    }

    // Create order in transaction.
    public int createOrder(Order order, List<OrderItem> items) {
        String insertOrderSql =
            "INSERT INTO orders (customer_id, order_status_id, order_type_id, total_amount, notes) " +
            "VALUES (?, ?, (SELECT order_type_id FROM order_types WHERE type_name = ?), ?, ?)";

        String insertItemSql =
            "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, line_total, special_instruction) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // <-- start transaction

            int orderId;

            // Insert order header.
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                if (order.getCustomerId() != null) ps.setInt(1, order.getCustomerId());
                else ps.setNull(1, Types.INTEGER);
                ps.setInt(2, 1); // NOT_PREPARING
                ps.setString(3, order.getOrderType());
                ps.setBigDecimal(4, order.getTotalAmount());
                ps.setString(5, order.getNotes());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No order ID generated");
                    orderId = keys.getInt(1);
                }
            }

            // Insert each item.
            try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                for (OrderItem item : items) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getMenuItemId());
                    ps.setInt(3, item.getQuantity());
                    ps.setBigDecimal(4, item.getUnitPrice());
                    ps.setBigDecimal(5, item.getLineTotal());
                    ps.setString(6, item.getSpecialInstruction());
                    ps.executeUpdate();
                }
            }

            conn.commit(); // <-- all good, save changes
            return orderId;

        } catch (SQLException e) {
            // Roll back on failure.
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            throw new RuntimeException("Failed to create order", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    public List<Order> findActiveOrders() {
        String sql = "SELECT o.*, os.status_name, ot.type_name AS order_type, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
                     "FROM orders o " +
                     "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
                     "JOIN order_types ot ON o.order_type_id = ot.order_type_id " +
                     "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
                     "LEFT JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE os.status_name != 'COMPLETED' " +
                     "ORDER BY o.placed_at DESC";
        return runOrderQuery(sql);
    }

    public List<Order> findAllOrders() {
        String sql = "SELECT o.*, os.status_name, ot.type_name AS order_type, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
                     "FROM orders o " +
                     "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
                     "JOIN order_types ot ON o.order_type_id = ot.order_type_id " +
                     "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
                     "LEFT JOIN users u ON cp.user_id = u.user_id " +
                     "ORDER BY o.placed_at DESC";
        return runOrderQuery(sql);
    }

    public List<Order> findOrdersByCustomerId(int customerId) {
        String sql = "SELECT o.*, os.status_name, ot.type_name AS order_type, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
                     "FROM orders o " +
                     "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
                     "JOIN order_types ot ON o.order_type_id = ot.order_type_id " +
                     "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
                     "LEFT JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE o.customer_id = ? ORDER BY o.placed_at DESC";

        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load customer orders", e);
        }
        return orders;
    }

    // Run a parameterless order query.
    private List<Order> runOrderQuery(String sql) {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) orders.add(mapOrder(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load orders", e);
        }
        return orders;
    }

    public Optional<Order> findById(int orderId) {
        String sql = "SELECT o.*, os.status_name, ot.type_name AS order_type, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
                     "FROM orders o " +
                     "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
                     "JOIN order_types ot ON o.order_type_id = ot.order_type_id " +
                     "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
                     "LEFT JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE o.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapOrder(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order", e);
        }
    }

    public List<OrderItem> findOrderItems(int orderId) {
        String sql = "SELECT oi.*, mi.item_name FROM order_items oi " +
                     "JOIN menu_items mi ON oi.menu_item_id = mi.menu_item_id " +
                     "WHERE oi.order_id = ?";

        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load order items", e);
        }
        return items;
    }

    public int getCurrentStatusOrder(int orderId) {
        String sql = "SELECT os.display_order FROM orders o " +
                     "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
                     "WHERE o.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("display_order");
                throw new SQLException("Order not found: " + orderId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get status order", e);
        }
    }

    public void updateOrderStatus(int orderId, int newStatusId) {
        String sql = "UPDATE orders SET order_status_id = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newStatusId);
            ps.setInt(2, orderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status", e);
        }
    }

    public int getStatusIdByDisplayOrder(int displayOrder) {
        String sql = "SELECT order_status_id FROM order_statuses WHERE display_order = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, displayOrder);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("order_status_id");
                throw new SQLException("No status with display order " + displayOrder);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get status id", e);
        }
    }
}
