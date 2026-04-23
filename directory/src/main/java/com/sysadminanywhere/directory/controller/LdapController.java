package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.*;
import com.sysadminanywhere.directory.service.LdapService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
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
@RequestMapping("/api/ldap")
@RequiredArgsConstructor
public class LdapController {

    private final LdapService ldapService;

    /**
     * Получение логов аудита с постраничным выводом
     */
    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditDto>> getAudit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sort,
            @RequestParam Map<String, String> filters) {

        try {
            validateAuditFilters(filters);
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<AuditDto> result = ldapService.getAudit(pageable, filters);
            log.info("Retrieved audit logs");

            PageResponse<AuditDto> response = new PageResponse<>(
                    result.getContent(),
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalElements(),
                    result.getTotalPages()
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid audit filters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving audit logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение списка логов аудита без постраничного вывода
     */
    @GetMapping("/audit/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditDto>> getAuditList(@RequestParam Map<String, String> filters) {
        try {
            validateAuditFilters(filters);
            List<AuditDto> result = ldapService.getAuditList(filters);
            log.info("Retrieved audit logs list");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid audit filters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving audit logs list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Поиск записей в LDAP с фильтром
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EntryDto>> search(@Valid @RequestBody SearchDto searchDto) {
        try {
            validateSearchDto(searchDto);

            Dn dn = new Dn(searchDto.getDistinguishedName());
            String filter = searchDto.getFilter();
            SearchScope searchScope = SearchScope.getSearchScope(searchDto.getSearchScope());

            String[] attributes = searchDto.getAttributes() != null ?
                    searchDto.getAttributes() : new String[]{"*"};

            List<EntryDto> result = ldapService.convertEntryList(
                    ldapService.searchWithAttributes(dn, filter, searchScope, attributes));

            log.info("LDAP search executed");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error executing LDAP search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Подсчет записей по фильтру
     */
    @PostMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> count(@Valid @RequestBody SearchDto searchDto) {
        try {
            validateSearchDto(searchDto);

            Dn dn = new Dn(searchDto.getDistinguishedName());
            String filter = searchDto.getFilter();
            SearchScope searchScope = SearchScope.getSearchScope(searchDto.getSearchScope());

            Long result = ldapService.count(dn, filter, searchScope);
            log.info("LDAP count executed, result: {}", result);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid count parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error executing LDAP count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение корневого DSE записи
     */
    @GetMapping("/rootdse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntryDto> getRootDse() {
        try {
            EntryDto result = ldapService.convertEntry(ldapService.getDomainEntry());
            log.info("Retrieved root DSE");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving root DSE", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Добавление члена в группу
     */
    @PostMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addMember(
            @RequestParam @NotBlank(message = "DN cannot be empty") String dn,
            @RequestParam @NotBlank(message = "Group cannot be empty") String group) {

        try {
            validateDn(dn);
            validateDn(group);

            boolean result = ldapService.addMember(dn, group);
            log.info("Member added to group: {}", group);

            return ResponseEntity.ok(Map.of("success", result));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid DN format: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error adding member to group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add member"));
        }
    }

    /**
     * Удаление члена из группы
     */
    @DeleteMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMember(
            @RequestParam @NotBlank(message = "DN cannot be empty") String dn,
            @RequestParam @NotBlank(message = "Group cannot be empty") String group) {

        try {
            validateDn(dn);
            validateDn(group);

            boolean result = ldapService.deleteMember(dn, group);
            log.info("Member removed from group: {}", group);

            return ResponseEntity.ok(Map.of("success", result));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid DN format: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error removing member from group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove member"));
        }
    }

    /**
     * Аутентификация пользователя
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getUsername().isBlank() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {

                log.warn("Authentication attempt with empty credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password"));
            }

            JwtResponse response = ldapService.authenticate(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.getService());

            if (response == null) {
                log.warn("Authentication failed for user: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password"));
            }

            log.info("User authenticated successfully: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed"));
        }
    }

    /**
     * Валидация SearchDto
     */
    private void validateSearchDto(SearchDto searchDto) {
        if (searchDto.getFilter() == null || searchDto.getFilter().isBlank()) {
            throw new IllegalArgumentException("Filter cannot be empty");
        }

        if (searchDto.getSearchScope() < 0 || searchDto.getSearchScope() > 2) {
            throw new IllegalArgumentException("Invalid search scope");
        }
    }

    /**
     * Валидация фильтров аудита
     */
    private void validateAuditFilters(Map<String, String> filters) {
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (key == null || key.isBlank()) {
                    throw new IllegalArgumentException("Filter key cannot be empty");
                }
            });
        }
    }

    /**
     * Валидация DN
     */
    private void validateDn(String dn) {
        if (dn == null || dn.isBlank()) {
            throw new IllegalArgumentException("DN cannot be empty");
        }

        try {
            new Dn(dn);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid DN format: " + e.getMessage());
        }
    }

}
