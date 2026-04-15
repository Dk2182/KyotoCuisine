package com.kyotocuisine.dao;

import com.kyotocuisine.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDAO {
    private final JdbcTemplate jdbc;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setRoleId(rs.getInt("role_id"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPhone(rs.getString("phone"));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        try { u.setRoleName(rs.getString("role_name")); } catch (Exception e) {}
        return u;
    };

    public UserDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = jdbc.query(
            "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.email = ?",
            rowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findById(int userId) {
        List<User> users = jdbc.query(
            "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.user_id = ?",
            rowMapper, userId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public int createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (role_id, first_name, last_name, email, password_hash, phone) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, user.getRoleId());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getPhone());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void createCustomerProfile(int userId) {
        jdbc.update("INSERT INTO customer_profiles (user_id) VALUES (?)", userId);
    }

    public Optional<Integer> getCustomerIdByUserId(int userId) {
        List<Integer> ids = jdbc.queryForList(
            "SELECT customer_id FROM customer_profiles WHERE user_id = ?", Integer.class, userId);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    public List<User> findAllUsers() {
        return jdbc.query(
            "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id ORDER BY u.created_at DESC",
            rowMapper);
    }
}
