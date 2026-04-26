package com.kyotocuisine.service;

import com.kyotocuisine.dao.AuditLogDAO;
import com.kyotocuisine.dao.ReservationDAO;
import com.kyotocuisine.model.Reservation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final AuditLogDAO auditLogDAO;

    // Operating hours.
    private static final LocalTime OPEN_TIME = LocalTime.of(11, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);

    public ReservationService(ReservationDAO reservationDAO, AuditLogDAO auditLogDAO) {
        this.reservationDAO = reservationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public Map<String, Object> createReservation(int customerId, LocalDateTime start, int guestCount, String specialRequest) {
        // Default 2 hour duration.
        LocalDateTime end = start.plusHours(2);

        // Check operating hours.
        LocalTime startTime = start.toLocalTime();
        if (startTime.isBefore(OPEN_TIME) || startTime.isAfter(CLOSE_TIME.minusHours(1))) {
            throw new RuntimeException("Reservations are only available between 11:00 AM and 9:00 PM");
        }

        // Reject past dates.
        if (start.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot make a reservation in the past");
        }

        // Find tables.
        List<Map<String, Object>> availableTables = reservationDAO.findAvailableTables(guestCount, start, end);
        if (availableTables.isEmpty()) {
            throw new RuntimeException("No tables available for " + guestCount + " guests at the requested time");
        }

        // Pick smallest fitting.
        Map<String, Object> assignedTable = availableTables.get(0);
        int tableId = (int) assignedTable.get("table_id");

        Reservation reservation = new Reservation();
        reservation.setCustomerId(customerId);
        reservation.setTableId(tableId);
        reservation.setReservationStart(start);
        reservation.setReservationEnd(end);
        reservation.setGuestCount(guestCount);
        reservation.setSpecialRequest(specialRequest);

        int reservationId = reservationDAO.createReservation(reservation);
        auditLogDAO.insertLog(null, "CREATE_RESERVATION", "reservations", reservationId,
            "Reservation for " + guestCount + " guests at table " + assignedTable.get("table_label"));

        return Map.of(
            "reservationId", reservationId,
            "tableLabel", assignedTable.get("table_label"),
            "start", start.toString(),
            "end", end.toString(),
            "guestCount", guestCount
        );
    }

    public List<Reservation> getCustomerReservations(int customerId) {
        return reservationDAO.findByCustomerId(customerId);
    }

    public List<Reservation> getAllReservations() {
        return reservationDAO.findAll();
    }

    public void cancelReservation(int reservationId) {
        reservationDAO.updateStatus(reservationId, 3); // CANCELLED
        auditLogDAO.insertLog(null, "CANCEL_RESERVATION", "reservations", reservationId, "Reservation cancelled");
    }

    public void confirmReservation(int reservationId, Integer userId) {
        reservationDAO.updateStatus(reservationId, 2); // CONFIRMED
        auditLogDAO.insertLog(userId, "CONFIRM_RESERVATION", "reservations", reservationId, "Reservation confirmed");
    }

    public void completeReservation(int reservationId, Integer userId) {
        reservationDAO.updateStatus(reservationId, 4); // COMPLETED
        auditLogDAO.insertLog(userId, "COMPLETE_RESERVATION", "reservations", reservationId, "Reservation marked completed");
    }

    public void cancelReservationAsStaff(int reservationId, Integer userId) {
        reservationDAO.updateStatus(reservationId, 3); // CANCELLED
        auditLogDAO.insertLog(userId, "CANCEL_RESERVATION", "reservations", reservationId, "Reservation cancelled by staff/admin");
    }
}
