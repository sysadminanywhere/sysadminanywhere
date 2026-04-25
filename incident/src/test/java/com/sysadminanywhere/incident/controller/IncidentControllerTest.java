package com.sysadminanywhere.incident.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.incident.entity.IncidentEntity;
import com.sysadminanywhere.incident.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentControllerTest {

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private IncidentController incidentController;

    @Test
    void getIncidents_shouldReturnPageOfIncidents() {
        IncidentEntity entity = IncidentEntity.builder()
                .signalId("SIG-001")
                .name("Test")
                .severity(Severity.HIGH)
                .status(IncidentStatus.OPEN)
                .build();
        entity.setId(1L);
        entity.setCreatedAt(LocalDateTime.now());
        
        Page<IncidentEntity> page = new PageImpl<>(List.of(entity));
        when(incidentRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponse<IncidentItem> result = incidentController.getIncidents(0, 10, "", "ALL", "ALL");

        assertNotNull(result);
        verify(incidentRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getIncident_shouldReturnIncident() {
        IncidentEntity entity = IncidentEntity.builder()
                .signalId("SIG-001")
                .name("Test")
                .severity(Severity.HIGH)
                .status(IncidentStatus.OPEN)
                .build();
        entity.setId(1L);
        entity.setCreatedAt(LocalDateTime.now());
        
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(entity));

        ResponseEntity<IncidentItem> result = incidentController.getIncident(1L);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(incidentRepository).findById(1L);
    }

    @Test
    void getIncident_shouldReturnNotFound() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<IncidentItem> result = incidentController.getIncident(99L);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
