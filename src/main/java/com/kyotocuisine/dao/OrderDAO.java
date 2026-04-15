package com.kyotocuisine.dao;

import com.kyotocuisine.model.Order;
import com.kyotocuisine.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderDAO {
    private final JdbcTemplate jdbc;

    private final RowMapper<Order> orderMapper = (rs, rowNum) -> {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getObject("customer_id") != null ? rs.getInt("customer_id") : null);
        o.setOrderStatusId(rs.getInt("order_status_id"));
        o.setOrderType(rs.getString("order_type"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setPlacedAt(rs.getTimestamp("placed_at").toLocalDateTime());
        o.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        Timestamp pt = rs.getTimestamp("pickup_time");
        if (pt != null) o.setPickupTime(pt.toLocalDateTime());
        o.setNotes(rs.getString("notes"));
        try { o.setStatusName(rs.getString("status_name")); } catch (Exception e) {}
        try { o.setCustomerName(rs.getString("customer_name")); } catch (Exception e) {}
        return o;
    };

    private final RowMapper<OrderItem> itemMapper = (rs, rowNum) -> {
        OrderItem oi = new OrderItem();
        oi.setOrderItemId(rs.getInt("order_item_id"));
        oi.setOrderId(rs.getInt("order_id"));
        oi.setMenuItemId(rs.getInt("menu_item_id"));
        oi.setQuantity(rs.getInt("quantity"));
        oi.setUnitPrice(rs.getBigDecimal("unit_price"));
        oi.setLineTotal(rs.getBigDecimal("line_total"));
        oi.setSpecialInstruction(rs.getString("special_instruction"));
        try { oi.setItemName(rs.getString("item_name")); } catch (Exception e) {}
        return oi;
    };

    public OrderDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public int createOrder(Order order, List<OrderItem> items) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO orders (customer_id, order_status_id, order_type, total_amount, notes) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            if (order.getCustomerId() != null) ps.setInt(1, order.getCustomerId());
            else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, 1); // NOT_PREPARING
            ps.setString(3, order.getOrderType());
            ps.setBigDecimal(4, order.getTotalAmount());
            ps.setString(5, order.getNotes());
            return ps;
        }, keyHolder);

        int orderId = keyHolder.getKey().intValue();

        for (OrderItem item : items) {
            jdbc.update(
                "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, line_total, special_instruction) VALUES (?, ?, ?, ?, ?, ?)",
                orderId, item.getMenuItemId(), item.getQuantity(),
                item.getUnitPrice(), item.getLineTotal(), item.getSpecialInstruction());
        }

        return orderId;
    }

    public List<Order> findActiveOrders() {
        return jdbc.query(
            "SELECT o.*, os.status_name, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
            "FROM orders o " +
            "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
            "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
            "LEFT JOIN users u ON cp.user_id = u.user_id " +
            "WHERE os.status_name != 'COMPLETED' ORDER BY o.placed_at DESC",
            orderMapper);
    }

    public List<Order> findAllOrders() {
        return jdbc.query(
            "SELECT o.*, os.status_name, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
            "FROM orders o " +
            "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
            "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
            "LEFT JOIN users u ON cp.user_id = u.user_id " +
            "ORDER BY o.placed_at DESC",
            orderMapper);
    }

    public List<Order> findOrdersByCustomerId(int customerId) {
        return jdbc.query(
            "SELECT o.*, os.status_name, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
            "FROM orders o " +
            "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
            "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
            "LEFT JOIN users u ON cp.user_id = u.user_id " +
            "WHERE o.customer_id = ? ORDER BY o.placed_at DESC",
            orderMapper, customerId);
    }

    public Optional<Order> findById(int orderId) {
        List<Order> orders = jdbc.query(
            "SELECT o.*, os.status_name, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
            "FROM orders o " +
            "JOIN order_statuses os ON o.order_status_id = os.order_status_id " +
            "LEFT JOIN customer_profiles cp ON o.customer_id = cp.customer_id " +
            "LEFT JOIN users u ON cp.user_id = u.user_id " +
            "WHERE o.order_id = ?",
            orderMapper, orderId);
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }

    public List<OrderItem> findOrderItems(int orderId) {
        return jdbc.query(
            "SELECT oi.*, mi.item_name FROM order_items oi " +
            "JOIN menu_items mi ON oi.menu_item_id = mi.menu_item_id " +
            "WHERE oi.order_id = ?",
            itemMapper, orderId);
    }

    public int getCurrentStatusOrder(int orderId) {
        return jdbc.queryForObject(
            "SELECT os.display_order FROM orders o JOIN order_statuses os ON o.order_status_id = os.order_status_id WHERE o.order_id = ?",
            Integer.class, orderId);
    }

    public void updateOrderStatus(int orderId, int newStatusId) {
        jdbc.update("UPDATE orders SET order_status_id = ? WHERE order_id = ?", newStatusId, orderId);
    }

    public int getStatusIdByDisplayOrder(int displayOrder) {
        return jdbc.queryForObject(
            "SELECT order_status_id FROM order_statuses WHERE display_order = ?",
            Integer.class, displayOrder);
    }
}
