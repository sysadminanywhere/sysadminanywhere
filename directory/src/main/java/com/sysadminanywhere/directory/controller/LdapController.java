package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.directory.service.LdapService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
