package com.kyotocuisine.dao;

import com.kyotocuisine.db.DatabaseConnection;
import com.kyotocuisine.model.Payment;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;

// Payments DAO.
@Repository
public class PaymentDAO {

    public int recordPayment(Payment payment) {
        String sql = "INSERT INTO payments (order_id, payment_method_id, payment_status_id, " +
                     "transaction_reference, amount, paid_at) VALUES (?, " +
                     "(SELECT payment_method_id FROM payment_methods WHERE method_name = ?), " +
                     "(SELECT payment_status_id FROM payment_statuses WHERE status_name = ?), " +
                     "?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, payment.getOrderId());
            ps.setString(2, payment.getPaymentMethod());
            ps.setString(3, payment.getPaymentStatus());
            ps.setString(4, payment.getTransactionReference());
            ps.setBigDecimal(5, payment.getAmount());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No payment ID generated");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to record payment", e);
        }
    }

    public boolean isOrderPaid(int orderId) {
        String sql = "SELECT COUNT(*) FROM payments p " +
                     "JOIN payment_statuses ps ON p.payment_status_id = ps.payment_status_id " +
                     "WHERE p.order_id = ? AND ps.status_name = 'PAID'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check payment status", e);
        }
    }
}
