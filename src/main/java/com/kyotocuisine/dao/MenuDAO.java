package com.kyotocuisine.dao;

import com.kyotocuisine.model.MenuItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MenuDAO {
    private final JdbcTemplate jdbc;

    private final RowMapper<MenuItem> rowMapper = (rs, rowNum) -> {
        MenuItem m = new MenuItem();
        m.setMenuItemId(rs.getInt("menu_item_id"));
        m.setMenuCategoryId(rs.getInt("menu_category_id"));
        m.setItemName(rs.getString("item_name"));
        m.setDescription(rs.getString("description"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setImageUrl(rs.getString("image_url"));
        m.setBestseller(rs.getBoolean("is_bestseller"));
        m.setAvailable(rs.getBoolean("is_available"));
        try { m.setCategoryName(rs.getString("category_name")); } catch (Exception e) {}
        return m;
    };

    public MenuDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MenuItem> findAllAvailable() {
        return jdbc.query(
            "SELECT mi.*, mc.category_name FROM menu_items mi " +
            "JOIN menu_categories mc ON mi.menu_category_id = mc.menu_category_id " +
            "WHERE mi.is_available = TRUE ORDER BY mc.category_name, mi.item_name",
            rowMapper);
    }

    public List<MenuItem> findAll() {
        return jdbc.query(
            "SELECT mi.*, mc.category_name FROM menu_items mi " +
            "JOIN menu_categories mc ON mi.menu_category_id = mc.menu_category_id " +
            "ORDER BY mc.category_name, mi.item_name",
            rowMapper);
    }

    public Optional<MenuItem> findById(int id) {
        List<MenuItem> items = jdbc.query(
            "SELECT mi.*, mc.category_name FROM menu_items mi " +
            "JOIN menu_categories mc ON mi.menu_category_id = mc.menu_category_id " +
            "WHERE mi.menu_item_id = ?", rowMapper, id);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    public List<Map<String, Object>> findCategories() {
        return jdbc.queryForList("SELECT * FROM menu_categories ORDER BY category_name");
    }

    public int createItem(MenuItem item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO menu_items (menu_category_id, item_name, description, price, image_url, is_bestseller, is_available) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, item.getMenuCategoryId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getDescription());
            ps.setBigDecimal(4, item.getPrice());
            ps.setString(5, item.getImageUrl());
            ps.setBoolean(6, item.isBestseller());
            ps.setBoolean(7, item.isAvailable());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void updateItem(MenuItem item) {
        jdbc.update(
            "UPDATE menu_items SET menu_category_id=?, item_name=?, description=?, price=?, image_url=?, is_bestseller=?, is_available=? WHERE menu_item_id=?",
            item.getMenuCategoryId(), item.getItemName(), item.getDescription(),
            item.getPrice(), item.getImageUrl(), item.isBestseller(), item.isAvailable(), item.getMenuItemId());
    }

    public void toggleAvailability(int id, boolean available) {
        jdbc.update("UPDATE menu_items SET is_available = ? WHERE menu_item_id = ?", available, id);
    }
}
