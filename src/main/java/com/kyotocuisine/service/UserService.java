package com.kyotocuisine.service;

import com.kyotocuisine.dao.AuditLogDAO;
import com.kyotocuisine.dao.UserDAO;
import com.kyotocuisine.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    // Simple in-memory session store: token -> userId
    private final ConcurrentHashMap<String, Integer> sessions = new ConcurrentHashMap<>();

    public UserService(UserDAO userDAO, AuditLogDAO auditLogDAO) {
        this.userDAO = userDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public Map<String, Object> register(String firstName, String lastName, String email, String password, String phone) {
        if (userDAO.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setRoleId(1); // CUSTOMER
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setPhone(phone);

        int userId = userDAO.createUser(user);
        userDAO.createCustomerProfile(userId);

        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);

        auditLogDAO.insertLog(userId, "REGISTER", "users", userId, "New customer registered");

        User created = userDAO.findById(userId).orElseThrow();
        return Map.of(
            "token", token,
            "userId", userId,
            "role", created.getRoleName(),
            "firstName", created.getFirstName(),
            "lastName", created.getLastName(),
            "email", created.getEmail()
        );
    }

    public Map<String, Object> login(String email, String password) {
        User user = userDAO.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getUserId());

        auditLogDAO.insertLog(user.getUserId(), "LOGIN", "users", user.getUserId(), "User logged in");

        return Map.of(
            "token", token,
            "userId", user.getUserId(),
            "role", user.getRoleName(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "email", user.getEmail()
        );
    }

    public void logout(String token) {
        sessions.remove(token);
    }

    public Optional<User> getUserFromToken(String token) {
        if (token == null || token.isEmpty()) return Optional.empty();
        Integer userId = sessions.get(token);
        if (userId == null) return Optional.empty();
        return userDAO.findById(userId);
    }

    public Optional<Integer> getCustomerIdFromToken(String token) {
        Optional<User> user = getUserFromToken(token);
        if (user.isEmpty()) return Optional.empty();
        return userDAO.getCustomerIdByUserId(user.get().getUserId());
    }
}
