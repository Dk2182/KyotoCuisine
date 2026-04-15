package com.kyotocuisine.service;

import com.kyotocuisine.dao.AuditLogDAO;
import com.kyotocuisine.dao.OrderDAO;
import com.kyotocuisine.model.Order;
import com.kyotocuisine.model.OrderItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderDAO orderDAO;
    private final AuditLogDAO auditLogDAO;

    public OrderService(OrderDAO orderDAO, AuditLogDAO auditLogDAO) {
        this.orderDAO = orderDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public int createOrder(Order order, List<OrderItem> items) {
        int orderId = orderDAO.createOrder(order, items);
        auditLogDAO.insertLog(null, "CREATE_ORDER", "orders", orderId, "Order placed with " + items.size() + " items");
        return orderId;
    }

    public List<Order> getActiveOrders() {
        List<Order> orders = orderDAO.findActiveOrders();
        for (Order o : orders) {
            o.setItems(orderDAO.findOrderItems(o.getOrderId()));
        }
        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderDAO.findAllOrders();
        for (Order o : orders) {
            o.setItems(orderDAO.findOrderItems(o.getOrderId()));
        }
        return orders;
    }

    public List<Order> getCustomerOrders(int customerId) {
        List<Order> orders = orderDAO.findOrdersByCustomerId(customerId);
        for (Order o : orders) {
            o.setItems(orderDAO.findOrderItems(o.getOrderId()));
        }
        return orders;
    }

    public Order getOrder(int orderId) {
        Order order = orderDAO.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setItems(orderDAO.findOrderItems(orderId));
        return order;
    }

    public void advanceOrderStatus(int orderId, Integer userId) {
        int currentDisplayOrder = orderDAO.getCurrentStatusOrder(orderId);
        if (currentDisplayOrder >= 4) {
            throw new RuntimeException("Order is already completed");
        }
        int nextDisplayOrder = currentDisplayOrder + 1;
        int newStatusId = orderDAO.getStatusIdByDisplayOrder(nextDisplayOrder);
        orderDAO.updateOrderStatus(orderId, newStatusId);

        String[] statusNames = {"", "NOT_PREPARING", "PREPARING", "READY", "COMPLETED"};
        String details = "Status changed from " + statusNames[currentDisplayOrder] + " to " + statusNames[nextDisplayOrder];
        auditLogDAO.insertLog(userId, "UPDATE_ORDER_STATUS", "orders", orderId, details);
    }
}
