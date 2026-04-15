package com.kyotocuisine.dao;

import com.kyotocuisine.model.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class ReservationDAO {
    private final JdbcTemplate jdbc;

    private final RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setCustomerId(rs.getInt("customer_id"));
        r.setTableId(rs.getInt("table_id"));
        r.setReservationStatusId(rs.getInt("reservation_status_id"));
        r.setReservationStart(rs.getTimestamp("reservation_start").toLocalDateTime());
        r.setReservationEnd(rs.getTimestamp("reservation_end").toLocalDateTime());
        r.setGuestCount(rs.getInt("guest_count"));
        r.setSpecialRequest(rs.getString("special_request"));
        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try { r.setStatusName(rs.getString("status_name")); } catch (Exception e) {}
        try { r.setTableLabel(rs.getString("table_label")); } catch (Exception e) {}
        try { r.setCustomerName(rs.getString("customer_name")); } catch (Exception e) {}
        return r;
    };

    public ReservationDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findAvailableTables(int guestCount, LocalDateTime start, LocalDateTime end) {
        return jdbc.queryForList(
            "SELECT rt.table_id, rt.table_label, rt.capacity FROM restaurant_tables rt " +
            "WHERE rt.is_active = TRUE AND rt.capacity >= ? " +
            "AND rt.table_id NOT IN (" +
            "  SELECT r.table_id FROM reservations r " +
            "  JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
            "  WHERE rs.status_name IN ('PENDING', 'CONFIRMED') " +
            "  AND r.reservation_start < ? AND r.reservation_end > ?" +
            ") ORDER BY rt.capacity ASC",
            guestCount, Timestamp.valueOf(end), Timestamp.valueOf(start));
    }

    public int createReservation(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO reservations (customer_id, table_id, reservation_status_id, reservation_start, reservation_end, guest_count, special_request) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, reservation.getCustomerId());
            ps.setInt(2, reservation.getTableId());
            ps.setInt(3, 1); // PENDING
            ps.setTimestamp(4, Timestamp.valueOf(reservation.getReservationStart()));
            ps.setTimestamp(5, Timestamp.valueOf(reservation.getReservationEnd()));
            ps.setInt(6, reservation.getGuestCount());
            ps.setString(7, reservation.getSpecialRequest());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public List<Reservation> findByCustomerId(int customerId) {
        return jdbc.query(
            "SELECT r.*, rs.status_name, rt.table_label, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
            "FROM reservations r " +
            "JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
            "JOIN restaurant_tables rt ON r.table_id = rt.table_id " +
            "JOIN customer_profiles cp ON r.customer_id = cp.customer_id " +
            "JOIN users u ON cp.user_id = u.user_id " +
            "WHERE r.customer_id = ? ORDER BY r.reservation_start DESC",
            rowMapper, customerId);
    }

    public List<Reservation> findAll() {
        return jdbc.query(
            "SELECT r.*, rs.status_name, rt.table_label, CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
            "FROM reservations r " +
            "JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
            "JOIN restaurant_tables rt ON r.table_id = rt.table_id " +
            "JOIN customer_profiles cp ON r.customer_id = cp.customer_id " +
            "JOIN users u ON cp.user_id = u.user_id " +
            "ORDER BY r.reservation_start DESC",
            rowMapper);
    }

    public void updateStatus(int reservationId, int statusId) {
        jdbc.update("UPDATE reservations SET reservation_status_id = ? WHERE reservation_id = ?", statusId, reservationId);
    }

    public int countActiveReservations() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM reservations r JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
            "WHERE rs.status_name IN ('PENDING', 'CONFIRMED')", Integer.class);
    }
}
