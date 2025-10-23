package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.directory.service.LdapService;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ldap")
public class LdapController {

    private final LdapService ldapService;

    public LdapController(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<AuditDto>> getAudit(@ParameterObject Pageable pageable, @RequestParam Map<String, String> filters) {
        return new ResponseEntity<>(ldapService.getAudit(pageable, filters), HttpStatus.OK);
    }

    @GetMapping("/audit/list")
    public ResponseEntity<List<AuditDto>> getAudit(@RequestParam Map<String, String> filters) {
        return new ResponseEntity<>(ldapService.getAuditList(filters), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping("/search")
    public ResponseEntity<List<EntryDto>> search(@RequestBody SearchDto searchDto) {
        Dn dn = new Dn(searchDto.getDistinguishedName());
        String filter = searchDto.getFilter();
        SearchScope searchScope = SearchScope.getSearchScope(searchDto.getSearchScope());
        return new ResponseEntity<>(ldapService.convertEntryList(ldapService.search(dn, filter, searchScope)), HttpStatus.OK);
    }

    @GetMapping("/rootdse")
    public ResponseEntity<EntryDto> getRootDse() {
        return new ResponseEntity<>(ldapService.convertEntry(ldapService.getDomainEntry()), HttpStatus.OK);
    }

    @PostMapping("/members")
    public ResponseEntity<Boolean> addMember(@RequestParam String dn, @RequestParam String group) {
        return new ResponseEntity<>(ldapService.addMember(dn, group), HttpStatus.OK);
    }

    @DeleteMapping("/members")
    public ResponseEntity<Boolean> deleteMember(@RequestParam String dn, @RequestParam String group) {
        return new ResponseEntity<>(ldapService.deleteMember(dn, group), HttpStatus.OK);
    }

}