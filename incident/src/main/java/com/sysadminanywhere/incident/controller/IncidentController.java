package com.sysadminanywhere.incident.controller;

import com.sysadminanywhere.incident.entity.IncidentEntity;
import com.sysadminanywhere.incident.model.IncidentStatus;
import com.sysadminanywhere.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/incident")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentRepository incidentRepository;

    // -------------------------
    // Получение списка инцидентов с фильтрацией и постраничным выводом
    @GetMapping
    public Page<IncidentEntity> getIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) String signalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable
    ) {
        return incidentRepository.findWithFilters(status, signalId, from, to, pageable);
    }

    // -------------------------
    // Получение конкретного инцидента по ID
    @GetMapping("/{id}")
    public ResponseEntity<IncidentEntity> getIncident(@PathVariable Long id) {
        return incidentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------------
    // Создание нового инцидента (для тестирования)
    @PostMapping
    public IncidentEntity createIncident(@RequestBody IncidentEntity incident) {
        if (incident.getCreatedAt() == null) {
            incident.setCreatedAt(LocalDateTime.now());
        }
        if (incident.getStatus() == null) {
            incident.setStatus(IncidentStatus.OPEN);
        }
        return incidentRepository.save(incident);
    }

    // -------------------------
    // Закрытие инцидента
    @PostMapping("/{id}/close")
    public ResponseEntity<IncidentEntity> closeIncident(@PathVariable Long id) {
        return incidentRepository.findById(id).map(incident -> {
            incident.setStatus(IncidentStatus.CLOSED);
            incident.setUpdatedAt(LocalDateTime.now());
            incidentRepository.save(incident);
            return ResponseEntity.ok(incident);
        }).orElse(ResponseEntity.notFound().build());
    }

}