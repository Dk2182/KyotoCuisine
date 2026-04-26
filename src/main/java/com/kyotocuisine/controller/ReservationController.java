package com.kyotocuisine.controller;

import com.kyotocuisine.model.User;
import com.kyotocuisine.service.EmailService;
import com.kyotocuisine.service.ReservationService;
import com.kyotocuisine.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final UserService userService;
    private final EmailService emailService;

    public ReservationController(ReservationService reservationService, UserService userService, EmailService emailService) {
        this.reservationService = reservationService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> body) {
        try {
            Integer customerId = userService.getCustomerIdFromToken(token).orElse(null);
            if (customerId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Please log in to make a reservation"));
            }

            LocalDateTime start = LocalDateTime.parse((String) body.get("dateTime"));
            int guestCount = ((Number) body.get("guestCount")).intValue();
            String specialRequest = (String) body.get("specialRequest");

            Map<String, Object> result = reservationService.createReservation(customerId, start, guestCount, specialRequest);

            // Send email.
            User user = userService.getUserFromToken(token).orElse(null);
            if (user != null) {
                emailService.sendReservationConfirmation(
                    user.getEmail(),
                    (int) result.get("reservationId"),
                    start.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                    start.format(DateTimeFormatter.ofPattern("h:mm a")),
                    guestCount,
                    (String) result.get("tableLabel")
                );
            }

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getReservations(@RequestHeader(value = "Authorization", required = false) String token) {
        Integer customerId = userService.getCustomerIdFromToken(token).orElse(null);
        if (customerId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(reservationService.getCustomerReservations(customerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable int id) {
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok(Map.of("message", "Reservation cancelled"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
