package com.kyotocuisine.model;

import java.time.LocalDateTime;

public class Reservation {
    private int reservationId;
    private int customerId;
    private int tableId;
    private int reservationStatusId;
    private LocalDateTime reservationStart;
    private LocalDateTime reservationEnd;
    private int guestCount;
    private String specialRequest;
    private LocalDateTime createdAt;
    private String statusName;
    private String tableLabel;
    private String customerName;

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }
    public int getReservationStatusId() { return reservationStatusId; }
    public void setReservationStatusId(int reservationStatusId) { this.reservationStatusId = reservationStatusId; }
    public LocalDateTime getReservationStart() { return reservationStart; }
    public void setReservationStart(LocalDateTime reservationStart) { this.reservationStart = reservationStart; }
    public LocalDateTime getReservationEnd() { return reservationEnd; }
    public void setReservationEnd(LocalDateTime reservationEnd) { this.reservationEnd = reservationEnd; }
    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }
    public String getSpecialRequest() { return specialRequest; }
    public void setSpecialRequest(String specialRequest) { this.specialRequest = specialRequest; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public String getTableLabel() { return tableLabel; }
    public void setTableLabel(String tableLabel) { this.tableLabel = tableLabel; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
