package com.kyotocuisine.controller;

import com.kyotocuisine.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> body) {
        try {
            int orderId = ((Number) body.get("orderId")).intValue();
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            Map<String, Object> result = paymentService.createPaymentIntent(orderId, amount);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> body) {
        try {
            int orderId = ((Number) body.get("orderId")).intValue();
            String transactionReference = (String) body.get("transactionReference");
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            paymentService.confirmPayment(orderId, transactionReference, amount);
            return ResponseEntity.ok(Map.of("message", "Payment confirmed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
