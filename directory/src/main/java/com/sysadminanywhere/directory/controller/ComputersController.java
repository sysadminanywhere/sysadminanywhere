package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.directory.controller.dto.AddComputerDto;
import com.sysadminanywhere.directory.model.ComputerEntry;
import com.sysadminanywhere.directory.model.UserEntry;
import com.sysadminanywhere.directory.service.ComputersService;
import lombok.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/computers")
public class ComputersController {

    private final ComputersService computersService;

    public ComputersController(ComputersService computersService) {
        this.computersService = computersService;
    }

    @GetMapping()
    public ResponseEntity<Page<ComputerEntry>> getAll(@ParameterObject Pageable pageable, @RequestParam String filters) {
        return new ResponseEntity<>(computersService.getAll(pageable, filters), HttpStatus.OK);
    }

    @GetMapping("/{cn}")
    public ResponseEntity<ComputerEntry> getByCN(@PathVariable String cn) {
        return new ResponseEntity<>(computersService.getByCN(cn), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<ComputerEntry> add(@NonNull @RequestBody AddComputerDto addComputer) {
        return new ResponseEntity<>(computersService.add(
                addComputer.getDistinguishedName(),
                addComputer.getComputer(),
                addComputer.isEnabled()
        ), HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<ComputerEntry> update(@NonNull @RequestBody ComputerEntry computer) {
        return new ResponseEntity<>(computersService.update(computer), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity delete(@NonNull @RequestParam String distinguishedName) {
        computersService.delete(distinguishedName);
        return new ResponseEntity(HttpStatus.OK);
    }

}