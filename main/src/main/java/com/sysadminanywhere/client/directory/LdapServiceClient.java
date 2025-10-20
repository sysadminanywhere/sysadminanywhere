package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "ldap",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface LdapServiceClient {

    @GetMapping("/api/ldap/audit")
    Page<AuditDto> getAudit(Pageable pageable, @RequestParam("filters") Map<String, Object> filters);

    @GetMapping("/api/ldap/audit/list")
    List<AuditDto> getAuditList(@RequestParam("filters") Map<String, Object> filters);

}
