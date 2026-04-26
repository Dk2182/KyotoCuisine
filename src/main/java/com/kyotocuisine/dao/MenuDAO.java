package com.kyotocuisine.dao;

import com.kyotocuisine.db.DatabaseConnection;
import com.kyotocuisine.model.MenuItem;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Menu items and categories.
@Repository
public class MenuDAO {

    private MenuItem mapRow(ResultSet rs) throws SQLException {
        MenuItem m = new MenuItem();
        m.setMenuItemId(rs.getInt("menu_item_id"));
        m.setMenuCategoryId(rs.getInt("menu_category_id"));
        m.setItemName(rs.getString("item_name"));
        m.setDescription(rs.getString("description"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setImageUrl(rs.getString("image_url"));
        m.setBestseller(rs.getBoolean("is_bestseller"));
        m.setAvailable(rs.getBoolean("is_available"));
        try { m.setCategoryName(rs.getString("category_name")); } catch (SQLException ignore) {}
        return m;
    }

    public List<MenuItem> findAllAvailable() {
        String sql = "SELECT mi.*, mc.category_name FROM menu_items mi " +
                     "JOIN menu_categories mc ON mi.menu_category_id = mc.menu_category_id " +
                     "WHERE mi.is_available = TRUE " +
                     "ORDER BY mc.category_name, mi.item_name";

        List<MenuItem> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) items.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load available menu", e);
        }

        return items;
    }

    public List<MenuItem> findAll() {
        String sql = "SELECT mi.*, mc.category_name FROM menu_items mi " +
                     "JOIN menu_categories mc ON mi.menu_category_id = mc.menu_category_id " +
                     "ORDER BY mc.category_name, mi.item_name";

        List<MenuItem> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) items.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load menu", e);
        }

        return items;
    }

    public Optional<MenuItem> findById(int id) {
        String sql = "SELECT mi.*, mc.category_name FROM menu_items mi " +
                     "JOIN menu_categories mc ON mi.menu_category_id = mc.menu_category_id " +
                     "WHERE mi.menu_item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find menu item", e);
        }
    }

    public List<Map<String, Object>> findCategories() {
        String sql = "SELECT menu_category_id, category_name, description FROM menu_categories ORDER BY category_name";
        List<Map<String, Object>> categories = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("menu_category_id", rs.getInt("menu_category_id"));
                row.put("category_name", rs.getString("category_name"));
                row.put("description", rs.getString("description"));
                categories.add(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load categories", e);
        }

        return categories;
    }

    public int createItem(MenuItem item) {
        String sql = "INSERT INTO menu_items (menu_category_id, item_name, description, price, image_url, is_bestseller, is_available) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getMenuCategoryId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getDescription());
            ps.setBigDecimal(4, item.getPrice());
            ps.setString(5, item.getImageUrl());
            ps.setBoolean(6, item.isBestseller());
            ps.setBoolean(7, item.isAvailable());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated ID returned");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create menu item", e);
        }
    }

    public void updateItem(MenuItem item) {
        String sql = "UPDATE menu_items SET menu_category_id=?, item_name=?, description=?, " +
                     "price=?, image_url=?, is_bestseller=?, is_available=? WHERE menu_item_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getMenuCategoryId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getDescription());
            ps.setBigDecimal(4, item.getPrice());
            ps.setString(5, item.getImageUrl());
            ps.setBoolean(6, item.isBestseller());
            ps.setBoolean(7, item.isAvailable());
            ps.setInt(8, item.getMenuItemId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update menu item", e);
        }
    }

    public void toggleAvailability(int id, boolean available) {
        String sql = "UPDATE menu_items SET is_available = ? WHERE menu_item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to toggle availability", e);
        }
    }
}
