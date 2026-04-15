package com.kyotocuisine.controller;

import com.kyotocuisine.service.MenuService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private final MenuService menuService;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<?> getMenu() {
        return ResponseEntity.ok(menuService.getAvailableMenu());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllMenu() {
        return ResponseEntity.ok(menuService.getAllMenu());
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(menuService.getCategories());
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of("stripePublishableKey", stripePublishableKey));
    }
}
