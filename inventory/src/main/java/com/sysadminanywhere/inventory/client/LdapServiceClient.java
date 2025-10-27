package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.inventory.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/api/ldap/search")
    List<EntryDto> getSearch(@RequestBody SearchDto searchDto);

    @GetMapping("/api/ldap/rootdse")
    EntryDto getRootDse();

    @PostMapping("/api/ldap/members")
    Boolean addMember(@RequestParam("dn") String dn, @RequestParam("group") String group);

    @DeleteMapping("/api/ldap/members")
    Boolean deleteMember(@RequestParam("dn") String dn, @RequestParam("group") String group);

}
