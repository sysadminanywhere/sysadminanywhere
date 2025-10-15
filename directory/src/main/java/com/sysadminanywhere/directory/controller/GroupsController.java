package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.directory.controller.dto.AddGroupDto;
import com.sysadminanywhere.directory.model.GroupEntry;
import com.sysadminanywhere.directory.service.GroupsService;
import lombok.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/groups")
public class GroupsController {

    private final GroupsService groupsService;

    public GroupsController(GroupsService groupsService) {
        this.groupsService = groupsService;
    }

    @GetMapping()
    public ResponseEntity<Page<GroupEntry>> getAll(@ParameterObject Pageable pageable, @RequestParam String filters) {
        return new ResponseEntity<>(groupsService.getAll(pageable, filters), HttpStatus.OK);
    }

    @GetMapping("/{cn}")
    public ResponseEntity<GroupEntry> getByCN(@PathVariable String cn) {
        return new ResponseEntity<>(groupsService.getByCN(cn), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<GroupEntry> add(@NonNull @RequestBody AddGroupDto addGroup) {
        return new ResponseEntity<>(groupsService.add(
                addGroup.getDistinguishedName(),
                addGroup.getGroup(),
                addGroup.getGroupScope(),
                addGroup.isSecurity()
        ), HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<GroupEntry> update(@NonNull @RequestBody GroupEntry group) {
        return new ResponseEntity<>(groupsService.update(group), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity delete(@NonNull @RequestParam String distinguishedName) {
        groupsService.delete(distinguishedName);
        return new ResponseEntity(HttpStatus.OK);
    }

}