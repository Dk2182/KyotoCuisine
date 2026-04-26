package com.kyotocuisine.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    // Background email sender.
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(2);

    public EmailService(JavaMailSender mailSender,
                        org.springframework.core.env.Environment env) {
        this.mailSender = mailSender;
        this.fromAddress = env.getProperty("spring.mail.username", "");
    }

    @PostConstruct
    public void logStartup() {
        if (fromAddress == null || fromAddress.isEmpty()) {
            System.out.println("[EmailService] Gmail credentials NOT configured - emails will be skipped");
        } else {
            System.out.println("[EmailService] Ready - sending emails from " + fromAddress);
        }
    }

    public void sendOrderConfirmation(String toEmail, int orderId, String totalAmount) {
        if (fromAddress == null || fromAddress.isEmpty()) {
            System.out.println("[EmailService] Skipping order email (no Gmail config)");
            return;
        }

        // Run in background.
        emailExecutor.submit(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(toEmail);
                message.setSubject("Kyoto Cuisine - Order #" + orderId + " Confirmed");
                message.setText(
                    "Thank you for your order at Kyoto Cuisine!\n\n" +
                    "Order #: " + orderId + "\n" +
                    "Total: $" + totalAmount + " (tax included)\n\n" +
                    "We are preparing your order. You will receive updates as it progresses.\n\n" +
                    "Thank you for choosing Kyoto Cuisine!\n" +
                    "- The Kyoto Cuisine Team"
                );
                mailSender.send(message);
                System.out.println("[EmailService] Order email sent to " + toEmail);
            } catch (Exception e) {
                System.err.println("[EmailService] FAILED to send order email to " + toEmail + ": " + e.getMessage());
            }
        });
    }

    public void sendReservationConfirmation(String toEmail, int reservationId, String date, String time, int guests, String tableLabel) {
        if (fromAddress == null || fromAddress.isEmpty()) {
            System.out.println("[EmailService] Skipping reservation email (no Gmail config)");
            return;
        }

        emailExecutor.submit(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(toEmail);
                message.setSubject("Kyoto Cuisine - Reservation #" + reservationId + " Confirmed");
                message.setText(
                    "Your reservation at Kyoto Cuisine has been confirmed!\n\n" +
                    "Reservation #: " + reservationId + "\n" +
                    "Date: " + date + "\n" +
                    "Time: " + time + "\n" +
                    "Guests: " + guests + "\n" +
                    "Table: " + tableLabel + "\n\n" +
                    "We look forward to welcoming you!\n\n" +
                    "If you need to cancel or modify, please contact us.\n" +
                    "- The Kyoto Cuisine Team"
                );
                mailSender.send(message);
                System.out.println("[EmailService] Reservation email sent to " + toEmail);
            } catch (Exception e) {
                System.err.println("[EmailService] FAILED to send reservation email to " + toEmail + ": " + e.getMessage());
            }
        });
    }
}
