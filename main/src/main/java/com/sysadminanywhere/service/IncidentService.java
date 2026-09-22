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
    private final WebhookService webhookService;

    public IncidentService(IncidentServiceClient incidentServiceClient, WebhookService webhookService) {
        this.incidentServiceClient = incidentServiceClient;
        this.webhookService = webhookService;
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
        IncidentItem updated = incidentServiceClient.updateIncident(id, severity.name(), status.name());
        if (updated != null) webhookService.publish("incident.updated", updated);
        return updated;
    }

    public IncidentItem createIncident(IncidentItem incident) {
        IncidentItem created = incidentServiceClient.createIncident(incident);
        if (created != null) webhookService.publish("incident.created", created);
        return created;
    }

    public IncidentItem closeIncident(Long id) {
        IncidentItem closed = incidentServiceClient.closeIncident(id);
        if (closed != null) webhookService.publish("incident.closed", closed);
        return closed;
    }

}
