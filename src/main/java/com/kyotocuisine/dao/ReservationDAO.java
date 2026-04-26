package com.kyotocuisine.dao;

import com.kyotocuisine.db.DatabaseConnection;
import com.kyotocuisine.model.Reservation;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Reservations DAO.
@Repository
public class ReservationDAO {

    private Reservation mapRow(ResultSet rs) throws SQLException {
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
        try { r.setStatusName(rs.getString("status_name")); } catch (SQLException ignore) {}
        try { r.setTableLabel(rs.getString("table_label")); } catch (SQLException ignore) {}
        try { r.setCustomerName(rs.getString("customer_name")); } catch (SQLException ignore) {}
        return r;
    }

    // Find available tables.
    public List<Map<String, Object>> findAvailableTables(int guestCount, LocalDateTime start, LocalDateTime end) {
        String sql =
            "SELECT rt.table_id, rt.table_label, rt.capacity " +
            "FROM restaurant_tables rt " +
            "WHERE rt.is_active = TRUE AND rt.capacity >= ? " +
            "AND rt.table_id NOT IN (" +
            "  SELECT r.table_id FROM reservations r " +
            "  JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
            "  WHERE rs.status_name IN ('PENDING','CONFIRMED') " +
            "  AND r.reservation_start < ? AND r.reservation_end > ?" +
            ") " +
            "ORDER BY rt.capacity ASC";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, guestCount);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("table_id", rs.getInt("table_id"));
                    row.put("table_label", rs.getString("table_label"));
                    row.put("capacity", rs.getInt("capacity"));
                    rows.add(row);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find available tables", e);
        }

        return rows;
    }

    public int createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (customer_id, table_id, reservation_status_id, " +
                     "reservation_start, reservation_end, guest_count, special_request) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reservation.getCustomerId());
            ps.setInt(2, reservation.getTableId());
            ps.setInt(3, 1); // PENDING
            ps.setTimestamp(4, Timestamp.valueOf(reservation.getReservationStart()));
            ps.setTimestamp(5, Timestamp.valueOf(reservation.getReservationEnd()));
            ps.setInt(6, reservation.getGuestCount());
            ps.setString(7, reservation.getSpecialRequest());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No reservation ID generated");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create reservation", e);
        }
    }

    public List<Reservation> findByCustomerId(int customerId) {
        String sql = "SELECT r.*, rs.status_name, rt.table_label, " +
                     "CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
                     "FROM reservations r " +
                     "JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
                     "JOIN restaurant_tables rt ON r.table_id = rt.table_id " +
                     "JOIN customer_profiles cp ON r.customer_id = cp.customer_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE r.customer_id = ? ORDER BY r.reservation_start DESC";

        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load reservations", e);
        }
        return list;
    }

    public List<Reservation> findAll() {
        String sql = "SELECT r.*, rs.status_name, rt.table_label, " +
                     "CONCAT(u.first_name, ' ', u.last_name) AS customer_name " +
                     "FROM reservations r " +
                     "JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
                     "JOIN restaurant_tables rt ON r.table_id = rt.table_id " +
                     "JOIN customer_profiles cp ON r.customer_id = cp.customer_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "ORDER BY r.reservation_start DESC";

        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all reservations", e);
        }
        return list;
    }

    public void updateStatus(int reservationId, int statusId) {
        String sql = "UPDATE reservations SET reservation_status_id = ? WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, statusId);
            ps.setInt(2, reservationId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reservation status", e);
        }
    }

    public int countActiveReservations() {
        String sql = "SELECT COUNT(*) FROM reservations r " +
                     "JOIN reservation_statuses rs ON r.reservation_status_id = rs.reservation_status_id " +
                     "WHERE rs.status_name IN ('PENDING','CONFIRMED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count reservations", e);
        }
    }
}
