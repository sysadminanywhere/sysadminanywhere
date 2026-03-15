package com.sysadminanywhere.incident.controller;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.incident.entity.IncidentEntity;
import com.sysadminanywhere.incident.mapper.IncidentMapper;
import com.sysadminanywhere.incident.repository.IncidentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private static final int DEFAULT_DAYS_BACK = 1;

    /**
     * Получение списка инцидентов с фильтрацией и постраничным выводом
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<IncidentItem> getIncidents(
            Pageable pageable,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status
    ) {
        if (severity.equalsIgnoreCase("ALL") && status.equalsIgnoreCase("ALL")) {
            return incidentRepository.findAll(pageable).map(IncidentMapper::toItem);
        }

        if (severity.equalsIgnoreCase("ALL")) {
            IncidentStatus statusFilter = Objects.requireNonNullElse(IncidentStatus.valueOf(status), IncidentStatus.OPEN);
            return incidentRepository.findWithStatus(statusFilter, pageable)
                    .map(IncidentMapper::toItem);
        }

        IncidentStatus statusFilter = Objects.requireNonNullElse(IncidentStatus.valueOf(status), IncidentStatus.OPEN);
        Severity severityFilter = Objects.requireNonNullElse(Severity.valueOf(severity), Severity.CRITICAL);

        return incidentRepository.findWithFilters(severityFilter, statusFilter, pageable)
                .map(IncidentMapper::toItem);
    }

    /**
     * Получение конкретного инцидента по ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentItem> getIncident(@PathVariable Long id) {
        return incidentRepository.findById(id)
                .map(entity -> ResponseEntity.ok(IncidentMapper.toItem(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создание нового инцидента
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentItem> createIncident(@Valid @RequestBody IncidentItem request) {
        IncidentEntity incident = IncidentMapper.toEntity(request);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setStatus(Objects.requireNonNullElse(incident.getStatus(), IncidentStatus.OPEN));

        IncidentEntity saved = incidentRepository.save(incident);
        log.info("Incident created: {}", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IncidentMapper.toItem(saved));
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentItem> updateIncident(@PathVariable Long id, @RequestParam String severity, @RequestParam String status) {
        Optional<IncidentEntity> incident = incidentRepository.findById(id);

        if (incident.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        incident.get().setSeverity(Severity.valueOf(severity.toUpperCase()));
        incident.get().setStatus(IncidentStatus.valueOf(status.toUpperCase().replace(" ", "_")));
        incident.get().setUpdatedAt(LocalDateTime.now());

        IncidentEntity saved = incidentRepository.save(incident.get());
        log.info("Incident updated: {}", saved.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(IncidentMapper.toItem(saved));
    }

    /**
     * Закрытие инцидента
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentItem> closeIncident(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return incidentRepository.findById(id)
                .map(incident -> {
                    incident.setStatus(IncidentStatus.CLOSED);
                    incident.setUpdatedAt(LocalDateTime.now());
                    incident.setClosedAt(LocalDateTime.now());
                    incident.setClosedBy(authentication.getName());
                    incidentRepository.save(incident);
                    log.info("Incident closed: {}", id);
                    return ResponseEntity.ok(IncidentMapper.toItem(incident));
                })
                .orElse(ResponseEntity.notFound().build());
    }

}