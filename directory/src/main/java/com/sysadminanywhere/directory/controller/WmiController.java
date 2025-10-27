package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.wmi.SoftwareEntity;
import com.sysadminanywhere.directory.service.WmiResolveService;
import com.sysadminanywhere.directory.service.WmiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/wmi")
public class WmiController {

    private final WmiService wmiService;

    public WmiController(WmiService wmiService) {
        this.wmiService = wmiService;
    }

    @GetMapping("/software")
    public ResponseEntity<List<SoftwareEntity>> getSoftware(@PathVariable String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return new ResponseEntity<>(wmiResolveService.getValues(wmiService.execute(hostName, "Select * From Win32_Product")), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NO_CONTENT);
        }

    }

}
