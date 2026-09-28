package com.sysadminanywhere.service;

import com.sysadminanywhere.model.DependencyHealth;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class DependencyHealthService {
    private final LdapService ldapService;
    private final InventoryService inventoryService;
    private final IncidentService incidentService;

    public DependencyHealthService(LdapService ldapService, InventoryService inventoryService, IncidentService incidentService) {
        this.ldapService = ldapService;
        this.inventoryService = inventoryService;
        this.incidentService = incidentService;
    }

    public DependencyHealth check() {
        var statuses = new LinkedHashMap<String, String>();
        try { statuses.put("directory", ldapService.getDomainHealth() == null ? "DOWN" : "UP"); }
        catch (Exception exception) { statuses.put("directory", "DOWN"); }
        statuses.put("inventory", Boolean.TRUE.equals(inventoryService.ping()) ? "UP" : "DOWN");
        statuses.put("incident", Boolean.TRUE.equals(incidentService.ping()) ? "UP" : "DOWN");
        return new DependencyHealth(statuses);
    }
}
