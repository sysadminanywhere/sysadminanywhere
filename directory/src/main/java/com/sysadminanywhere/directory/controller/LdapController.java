package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.directory.service.LdapService;
import com.sysadminanywhere.directory.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LdapController {

    private final UsersService usersService;
    private final LdapService ldapService;

    public LdapController(UsersService usersService, LdapService ldapService) {
        this.usersService = usersService;
        this.ldapService = ldapService;
    }

    @GetMapping("/me")
    ResponseEntity<UserEntry> getMe() {
        return new ResponseEntity<>(usersService.getByCN("admin"), HttpStatus.OK);
    }

    @GetMapping("/login")
    ResponseEntity<Boolean> login(String userName, String password) {
        return new ResponseEntity<>(ldapService.checkUser(userName, password), HttpStatus.OK);
    }

}