package com.kyotocuisine.service;

import com.kyotocuisine.dao.AdminDAO;
import com.kyotocuisine.dao.AuditLogDAO;
import com.kyotocuisine.model.AuditLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final AdminDAO adminDAO;
    private final AuditLogDAO auditLogDAO;

    public AdminService(AdminDAO adminDAO, AuditLogDAO auditLogDAO) {
        this.adminDAO = adminDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public Map<String, Object> getStats() {
        return adminDAO.getStats();
    }

    public List<AuditLog> getLogs() {
        return auditLogDAO.findAll();
    }
}
