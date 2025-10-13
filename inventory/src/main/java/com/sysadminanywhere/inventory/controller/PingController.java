package com.sysadminanywhere.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping("")
    public ResponseEntity<String> getSoftwareOnComputer() {
        return ResponseEntity.ok("OK");
    }

}

