package com.kyotocuisine.dao;

import com.kyotocuisine.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class PaymentDAO {
    private final JdbcTemplate jdbc;

    public PaymentDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int recordPayment(Payment payment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO payments (order_id, payment_method, payment_status, transaction_reference, amount, paid_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, payment.getOrderId());
            ps.setString(2, payment.getPaymentMethod());
            ps.setString(3, payment.getPaymentStatus());
            ps.setString(4, payment.getTransactionReference());
            ps.setBigDecimal(5, payment.getAmount());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public boolean isOrderPaid(int orderId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM payments WHERE order_id = ? AND payment_status = 'PAID'",
            Integer.class, orderId);
        return count != null && count > 0;
    }
}
