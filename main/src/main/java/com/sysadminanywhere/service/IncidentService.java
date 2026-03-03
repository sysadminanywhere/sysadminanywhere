package com.sysadminanywhere.service;

import com.sysadminanywhere.client.incident.IncidentServiceClient;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class IncidentService {

    private final IncidentServiceClient incidentServiceClient;

    public IncidentService(IncidentServiceClient incidentServiceClient) {
        this.incidentServiceClient = incidentServiceClient;
    }

    public Boolean ping() {
        try {
            incidentServiceClient.ping();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Page<IncidentItem> getIncidents(Pageable pageable, Map<String, Object> filters) {
        return incidentServiceClient.getIncidents(pageable, null);
    }

}
