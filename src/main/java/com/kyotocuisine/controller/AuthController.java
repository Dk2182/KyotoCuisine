package com.kyotocuisine.controller;

import com.kyotocuisine.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = userService.register(
                body.get("firstName"),
                body.get("lastName"),
                body.get("email"),
                body.get("password"),
                body.getOrDefault("phone", null)
            );
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = userService.login(body.get("email"), body.get("password"));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null) userService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String token) {
        return userService.getUserFromToken(token)
            .map(user -> ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "role", user.getRoleName(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail()
            )))
            .orElse(ResponseEntity.status(401).body(Map.of("error", "Not authenticated")));
    }
}
