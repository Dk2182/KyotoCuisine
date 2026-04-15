package com.kyotocuisine.service;

import com.kyotocuisine.dao.MenuDAO;
import com.kyotocuisine.model.MenuItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MenuService {
    private final MenuDAO menuDAO;

    public MenuService(MenuDAO menuDAO) {
        this.menuDAO = menuDAO;
    }

    public List<MenuItem> getAvailableMenu() {
        return menuDAO.findAllAvailable();
    }

    public List<MenuItem> getAllMenu() {
        return menuDAO.findAll();
    }

    public List<Map<String, Object>> getCategories() {
        return menuDAO.findCategories();
    }

    public MenuItem getItem(int id) {
        return menuDAO.findById(id).orElseThrow(() -> new RuntimeException("Menu item not found"));
    }

    public int addItem(MenuItem item) {
        return menuDAO.createItem(item);
    }

    public void updateItem(MenuItem item) {
        menuDAO.updateItem(item);
    }

    public void toggleAvailability(int id, boolean available) {
        menuDAO.toggleAvailability(id, available);
    }
}
