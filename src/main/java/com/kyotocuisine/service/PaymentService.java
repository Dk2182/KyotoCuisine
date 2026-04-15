package com.kyotocuisine.service;

import com.kyotocuisine.dao.AuditLogDAO;
import com.kyotocuisine.dao.OrderDAO;
import com.kyotocuisine.dao.PaymentDAO;
import com.kyotocuisine.model.Payment;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentService {
    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;
    private final AuditLogDAO auditLogDAO;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public PaymentService(PaymentDAO paymentDAO, OrderDAO orderDAO, AuditLogDAO auditLogDAO) {
        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
        this.auditLogDAO = auditLogDAO;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Map<String, Object> createPaymentIntent(int orderId, BigDecimal amount) {
        if (paymentDAO.isOrderPaid(orderId)) {
            throw new RuntimeException("Order has already been paid");
        }

        try {
            // Stripe expects amount in cents
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .putMetadata("orderId", String.valueOf(orderId))
                .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return Map.of(
                "clientSecret", intent.getClientSecret(),
                "paymentIntentId", intent.getId()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    public void confirmPayment(int orderId, String transactionReference, BigDecimal amount) {
        if (paymentDAO.isOrderPaid(orderId)) {
            throw new RuntimeException("Order has already been paid");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod("CARD");
        payment.setPaymentStatus("PAID");
        payment.setTransactionReference(transactionReference);
        payment.setAmount(amount);

        paymentDAO.recordPayment(payment);
        auditLogDAO.insertLog(null, "PAYMENT_RECEIVED", "payments", orderId,
            "Payment of $" + amount + " received for order #" + orderId);
    }
}
