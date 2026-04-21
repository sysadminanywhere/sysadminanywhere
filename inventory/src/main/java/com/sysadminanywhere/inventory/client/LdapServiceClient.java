package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

public interface LdapServiceClient {

    @GetExchange("/api/ldap/audit")
    ResponseEntity<Page<AuditDto>> getAudit(Pageable pageable, Map<String, String> filters);

    @GetExchange("/api/ldap/audit/list")
    ResponseEntity<List<AuditDto>> getAuditList(Map<String, String> filters);

    @PostExchange("/api/ldap/search")
    ResponseEntity<List<EntryDto>> getSearch(SearchDto searchDto);

    @PostExchange("/api/ldap/count")
    ResponseEntity<Long> count(SearchDto searchDto);

    @GetExchange("/api/ldap/rootdse")
    ResponseEntity<EntryDto> getRootDse();

    @PostExchange("/api/ldap/members")
    ResponseEntity<?> addMember(String dn, String group);

    @DeleteExchange("/api/ldap/members")
    ResponseEntity<?> deleteMember(String dn, String group);

}
