package com.sysadminanywhere.service;

import com.sysadminanywhere.client.incident.IncidentServiceClient;
import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        PageResponse<IncidentItem> response = incidentServiceClient.getIncidents(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            pageable.getSort().toString(),
            filters
        );
        return new PageImpl<>(response.content(), PageRequest.of(response.page(), response.size()), response.totalElements());
    }

    public IncidentItem updateIncident(Long id, Severity severity, IncidentStatus status) {
        return incidentServiceClient.updateIncident(id, severity.name(), status.name());
    }

    public IncidentItem closeIncident(Long id) {
        return incidentServiceClient.closeIncident(id);
    }

}
