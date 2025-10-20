package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.directory.service.PrintersService;
import lombok.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/printers")
public class PrintersController {

    private final PrintersService printersService;

    public PrintersController(PrintersService printersService) {
        this.printersService = printersService;
    }

    @GetMapping()
    public ResponseEntity<Page<PrinterEntry>> getAll(@ParameterObject Pageable pageable, @RequestParam String filters) {
        return new ResponseEntity<>(printersService.getAll(pageable, filters), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<PrinterEntry>> getList(@RequestParam String filters) {
        return new ResponseEntity<>(printersService.getAll(filters), HttpStatus.OK);
    }

    @GetMapping("/{cn}")
    public ResponseEntity<PrinterEntry> getByCN(@PathVariable String cn) {
        return new ResponseEntity<>(printersService.getByCN(cn), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity delete(@NonNull @RequestParam String distinguishedName) {
        printersService.delete(distinguishedName);
        return new ResponseEntity(HttpStatus.OK);
    }


}