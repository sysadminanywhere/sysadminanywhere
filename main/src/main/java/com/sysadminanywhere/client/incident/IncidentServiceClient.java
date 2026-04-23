package com.sysadminanywhere.client.incident;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.Map;

public interface IncidentServiceClient {

    @GetExchange("/ping")
    String ping();

    @GetExchange("/api/incidents")
    PageResponse<IncidentItem> getIncidents(@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam Map<String, Object> filters);

    @PutExchange("/api/incidents/{id}/update")
    IncidentItem updateIncident(@PathVariable Long id, @RequestParam String severity, @RequestParam String status);

    @PostExchange("/api/incidents/{id}/close")
    IncidentItem closeIncident(@PathVariable Long id);
}
