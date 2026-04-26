package com.kyotocuisine.controller;

import com.kyotocuisine.model.Order;
import com.kyotocuisine.model.OrderItem;
import com.kyotocuisine.model.User;
import com.kyotocuisine.service.EmailService;
import com.kyotocuisine.service.OrderService;
import com.kyotocuisine.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final EmailService emailService;

    public OrderController(OrderService orderService, UserService userService, EmailService emailService) {
        this.orderService = orderService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> body) {
        try {
            Integer customerId = userService.getCustomerIdFromToken(token).orElse(null);

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setOrderType((String) body.getOrDefault("orderType", "TAKEAWAY"));
            order.setTotalAmount(new BigDecimal(body.get("totalAmount").toString()));
            order.setNotes((String) body.get("notes"));

            List<OrderItem> items = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) body.get("items");
            for (Map<String, Object> itemMap : itemMaps) {
                OrderItem oi = new OrderItem();
                oi.setMenuItemId(((Number) itemMap.get("menuItemId")).intValue());
                oi.setQuantity(((Number) itemMap.get("quantity")).intValue());
                oi.setUnitPrice(new BigDecimal(itemMap.get("unitPrice").toString()));
                oi.setLineTotal(new BigDecimal(itemMap.get("lineTotal").toString()));
                oi.setSpecialInstruction((String) itemMap.get("specialInstruction"));
                items.add(oi);
            }

            int orderId = orderService.createOrder(order, items);

            // Send email if logged in.
            User user = userService.getUserFromToken(token).orElse(null);
            if (user != null) {
                emailService.sendOrderConfirmation(user.getEmail(), orderId, order.getTotalAmount().toString());
            }

            return ResponseEntity.ok(Map.of("orderId", orderId, "message", "Order placed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@RequestHeader(value = "Authorization", required = false) String token) {
        Integer customerId = userService.getCustomerIdFromToken(token).orElse(null);
        if (customerId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(orderService.getCustomerOrders(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable int id) {
        try {
            return ResponseEntity.ok(orderService.getOrder(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
