package com.kyotocuisine.controller;

import com.kyotocuisine.model.MenuItem;
import com.kyotocuisine.model.User;
import com.kyotocuisine.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final MenuService menuService;
    private final UserService userService;

    public AdminController(AdminService adminService, OrderService orderService,
                          ReservationService reservationService, MenuService menuService,
                          UserService userService) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.menuService = menuService;
        this.userService = userService;
    }

    private boolean isAdmin(String token) {
        User user = userService.getUserFromToken(token).orElse(null);
        return user != null && "ADMIN".equals(user.getRoleName());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestHeader(value = "Authorization", required = false) String token) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(@RequestHeader(value = "Authorization", required = false) String token) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/reservations")
    public ResponseEntity<?> getAllReservations(@RequestHeader(value = "Authorization", required = false) String token) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(@RequestHeader(value = "Authorization", required = false) String token) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        return ResponseEntity.ok(adminService.getLogs());
    }

    @GetMapping("/menu")
    public ResponseEntity<?> getAllMenuItems(@RequestHeader(value = "Authorization", required = false) String token) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        return ResponseEntity.ok(menuService.getAllMenu());
    }

    @PostMapping("/menu")
    public ResponseEntity<?> addMenuItem(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> body) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        try {
            MenuItem item = new MenuItem();
            item.setMenuCategoryId(((Number) body.get("menuCategoryId")).intValue());
            item.setItemName((String) body.get("itemName"));
            item.setDescription((String) body.get("description"));
            item.setPrice(new BigDecimal(body.get("price").toString()));
            item.setImageUrl((String) body.getOrDefault("imageUrl", "/assets/default.jpg"));
            item.setBestseller(Boolean.TRUE.equals(body.get("isBestseller")));
            item.setAvailable(body.get("isAvailable") == null || Boolean.TRUE.equals(body.get("isAvailable")));
            int id = menuService.addItem(item);
            return ResponseEntity.ok(Map.of("menuItemId", id, "message", "Item added"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable int id,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> body) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        try {
            MenuItem item = new MenuItem();
            item.setMenuItemId(id);
            item.setMenuCategoryId(((Number) body.get("menuCategoryId")).intValue());
            item.setItemName((String) body.get("itemName"));
            item.setDescription((String) body.get("description"));
            item.setPrice(new BigDecimal(body.get("price").toString()));
            item.setImageUrl((String) body.get("imageUrl"));
            item.setBestseller(Boolean.TRUE.equals(body.get("isBestseller")));
            item.setAvailable(body.get("isAvailable") == null || Boolean.TRUE.equals(body.get("isAvailable")));
            menuService.updateItem(item);
            return ResponseEntity.ok(Map.of("message", "Item updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/menu/{id}/toggle")
    public ResponseEntity<?> toggleMenuItemAvailability(
            @PathVariable int id,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> body) {
        if (!isAdmin(token)) return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        boolean available = Boolean.TRUE.equals(body.get("isAvailable"));
        menuService.toggleAvailability(id, available);
        return ResponseEntity.ok(Map.of("message", available ? "Item enabled" : "Item disabled"));
    }
}
