package com.kyotocuisine.model;

import java.math.BigDecimal;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int menuItemId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String specialInstruction;
    private String itemName;

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getMenuItemId() { return menuItemId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public String getSpecialInstruction() { return specialInstruction; }
    public void setSpecialInstruction(String specialInstruction) { this.specialInstruction = specialInstruction; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}
