package com.kyotocuisine.controller;

import com.kyotocuisine.model.User;
import com.kyotocuisine.service.OrderService;
import com.kyotocuisine.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final OrderService orderService;
    private final UserService userService;

    public StaffController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getActiveOrders(@RequestHeader(value = "Authorization", required = false) String token) {
        User user = userService.getUserFromToken(token).orElse(null);
        if (user == null || (!"STAFF".equals(user.getRoleName()) && !"ADMIN".equals(user.getRoleName()))) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    @PutMapping("/orders/{id}/advance")
    public ResponseEntity<?> advanceOrderStatus(
            @PathVariable int id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            User user = userService.getUserFromToken(token).orElse(null);
            if (user == null || (!"STAFF".equals(user.getRoleName()) && !"ADMIN".equals(user.getRoleName()))) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
            orderService.advanceOrderStatus(id, user.getUserId());
            return ResponseEntity.ok(Map.of("message", "Order status updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
