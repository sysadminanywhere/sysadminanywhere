package com.sysadminanywhere.controller;

import com.sysadminanywhere.model.DependencyHealth;
import com.sysadminanywhere.service.DependencyHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class DependencyHealthController {
    private final DependencyHealthService service;

    public DependencyHealthController(DependencyHealthService service) { this.service = service; }

    @GetMapping("/dependencies")
    public DependencyHealth dependencies() { return service.check(); }
}
