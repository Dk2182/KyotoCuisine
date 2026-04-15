package com.kyotocuisine.model;

import java.math.BigDecimal;

public class MenuItem {
    private int menuItemId;
    private int menuCategoryId;
    private String itemName;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private boolean isBestseller;
    private boolean isAvailable;
    private String categoryName;

    public int getMenuItemId() { return menuItemId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }
    public int getMenuCategoryId() { return menuCategoryId; }
    public void setMenuCategoryId(int menuCategoryId) { this.menuCategoryId = menuCategoryId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isBestseller() { return isBestseller; }
    public void setBestseller(boolean bestseller) { isBestseller = bestseller; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
