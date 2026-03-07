package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.inventory.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "ldap",
        url = "${app.services.directory.uri}",
        configuration = FeignConfiguration.class
)
public interface LdapServiceClient {

    @GetMapping("/api/ldap/audit")
    ResponseEntity<Page<AuditDto>> getAudit(Pageable pageable, @RequestParam("filters") Map<String, String> filters);

    @GetMapping("/api/ldap/audit/list")
    ResponseEntity<List<AuditDto>> getAuditList(@RequestParam("filters") Map<String, String> filters);

    @PostMapping("/api/ldap/search")
    ResponseEntity<List<EntryDto>> getSearch(@RequestBody SearchDto searchDto);

    @PostMapping("/api/ldap/count")
    ResponseEntity<Long> count(@RequestBody SearchDto searchDto);

    @GetMapping("/api/ldap/rootdse")
    ResponseEntity<EntryDto> getRootDse();

    @PostMapping("/api/ldap/members")
    ResponseEntity<?> addMember(@RequestParam("dn") String dn, @RequestParam("group") String group);

    @DeleteMapping("/api/ldap/members")
    ResponseEntity<?> deleteMember(@RequestParam("dn") String dn, @RequestParam("group") String group);

}
