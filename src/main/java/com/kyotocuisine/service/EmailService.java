package com.kyotocuisine.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(String toEmail, int orderId, String totalAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
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
        } catch (Exception e) {
            System.err.println("Failed to send order email: " + e.getMessage());
        }
    }

    public void sendReservationConfirmation(String toEmail, int reservationId, String date, String time, int guests, String tableLabel) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
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
        } catch (Exception e) {
            System.err.println("Failed to send reservation email: " + e.getMessage());
        }
    }
}
