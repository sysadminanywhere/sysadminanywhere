package com.sysadminanywhere.client.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.Map;

public interface IncidentServiceClient {

    @GetExchange("/ping")
    String ping();

    @GetExchange("/api/incidents")
    Page<IncidentItem> getIncidents(Pageable pageable, Map<String, Object> filters);

    @PutExchange("/api/incidents/{id}/update")
    IncidentItem updateIncident(Long id, String severity, String status);

    @PostExchange("/api/incidents/{id}/close")
    IncidentItem closeIncident(Long id);
}
