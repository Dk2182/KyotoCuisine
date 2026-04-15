package com.kyotocuisine.model;

import java.time.LocalDateTime;

public class AuditLog {
    private int logId;
    private Integer userId;
    private String actionType;
    private String entityName;
    private int entityId;
    private String actionDetails;
    private LocalDateTime loggedAt;
    private String userName;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public int getEntityId() { return entityId; }
    public void setEntityId(int entityId) { this.entityId = entityId; }
    public String getActionDetails() { return actionDetails; }
    public void setActionDetails(String actionDetails) { this.actionDetails = actionDetails; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
