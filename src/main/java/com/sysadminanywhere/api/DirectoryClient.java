package com.sysadminanywhere.api;

import com.sysadminanywhere.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "localhost", url = "http://localhost:8081")
public interface DirectoryClient {

    // Users

    @GetMapping("/api/users")
    public ResponseEntity<Page<UserEntry>> getAllUsers(Pageable pageable, @RequestParam("filters") String filters);

    @GetMapping("/api/users/{cn}")
    public ResponseEntity<UserEntry> getUserByCN(@PathVariable("cn") String cn);

    // Groups

    @GetMapping("/api/groups")
    public ResponseEntity<Page<GroupEntry>> getAllGroups(Pageable pageable, @RequestParam("filters") String filters);

    // Computers

    @GetMapping("/api/computers")
    public ResponseEntity<Page<ComputerEntry>> getAllComputers(Pageable pageable, @RequestParam("filters") String filters);

    // Contacts

    @GetMapping("/api/contacts")
    public ResponseEntity<Page<ContactEntry>> getAllContacts(Pageable pageable, @RequestParam("filters") String filters);

    // Printers

    @GetMapping("/api/printers")
    public ResponseEntity<Page<PrinterEntry>> getAllPrinters(Pageable pageable, @RequestParam("filters") String filters);

    // Others

    @GetMapping("/api/login")
    public ResponseEntity<Boolean> login(@RequestParam String userName, @RequestParam String password);

}
