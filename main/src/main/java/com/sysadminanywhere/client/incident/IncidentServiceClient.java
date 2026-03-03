package com.sysadminanywhere.client.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

}
