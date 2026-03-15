package com.sysadminanywhere.client.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "incident",
        url = "${app.services.incident.uri}",
        configuration = FeignConfiguration.class
)
public interface IncidentServiceClient {

    @GetMapping("/ping")
    String ping();

    @GetMapping("/api/incidents")
    Page<IncidentItem> getIncidents(Pageable pageable, @RequestParam("filters") Map<String, Object> filters);

    @PutMapping("/api/incidents/{id}/update")
    IncidentItem updateIncident(@PathVariable Long id, @RequestParam("severity") String severity, @RequestParam("status") String status);

}
