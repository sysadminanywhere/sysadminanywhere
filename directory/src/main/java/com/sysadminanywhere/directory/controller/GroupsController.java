package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddGroupDto;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.directory.service.GroupsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService groupsService;

    /**
     * Получение всех групп с постраничным выводом и фильтрацией
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<GroupEntry>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sort,
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<GroupEntry> result = groupsService.getAll(pageable, filters, attributes);
            log.info("Retrieved groups with filters");

            PageResponse<GroupEntry> response = new PageResponse<>(
                    result.getContent(),
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalElements(),
                    result.getTotalPages()
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение списка групп без постраничного вывода
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GroupEntry>> getList(
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            List<GroupEntry> result = groupsService.getAll(filters, attributes);
            log.info("Retrieved groups list with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving groups list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение группы по CN (Common Name)
     */
    @GetMapping("/{cn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupEntry> getByCN(@PathVariable String cn) {
        try {
            if (cn == null || cn.isBlank()) {
                log.warn("Invalid CN provided");
                return ResponseEntity.badRequest().build();
            }

            GroupEntry result = groupsService.getByCN(cn);
            log.info("Retrieved group by CN: {}", cn);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving group by CN: {}", cn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создание новой группы
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupEntry> add(@Valid @RequestBody AddGroupDto addGroup) {
        try {
            if (addGroup == null || addGroup.getDistinguishedName() == null || addGroup.getDistinguishedName().isBlank()) {
                log.warn("Invalid group data for creation");
                return ResponseEntity.badRequest().build();
            }

            if (addGroup.getCn() == null || addGroup.getCn().isBlank()) {
                log.warn("CN cannot be empty");
                return ResponseEntity.badRequest().build();
            }

            GroupEntry result = groupsService.add(
                    addGroup.getDistinguishedName(),
                    addGroup.getCn(),
                    addGroup.getDescription(),
                    addGroup.getGroupScope(),
                    addGroup.isSecurity()
            );

            log.info("Group created: {}", addGroup.getCn());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            log.error("Error creating group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновление группы
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GroupEntry> update(@Valid @RequestBody GroupEntry group) {
        try {
            if (group == null || group.getDistinguishedName() == null || group.getDistinguishedName().isBlank()) {
                log.warn("Invalid group data for update");
                return ResponseEntity.badRequest().build();
            }

            GroupEntry result = groupsService.update(group);
            log.info("Group updated: {}", group.getDistinguishedName());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error updating group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удаление группы
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @RequestParam @NotBlank(message = "DistinguishedName cannot be empty") String distinguishedName) {

        try {
            groupsService.delete(distinguishedName);
            log.info("Group deleted: {}", distinguishedName);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting group: {}", distinguishedName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete group"));
        }
    }

}
