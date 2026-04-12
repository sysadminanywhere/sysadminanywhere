package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.incident.entity.EventEntity;
import com.sysadminanywhere.incident.repository.EventRepository;
import com.sysadminanywhere.incident.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private SignalLoader signalLoader;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private IncidentService incidentService;

    @Test
    void processEvents_shouldDoNothingWhenNoEvents() {
        when(eventRepository.findByIncidentIdIsNull()).thenReturn(List.of());

        incidentService.processEvents();

        verify(eventRepository).findByIncidentIdIsNull();
        verifyNoInteractions(signalLoader);
    }

    @Test
    void processEvents_shouldDoNothingWhenNoSignals() {
        EventEntity event = createEvent(1L, 1001, "PC001");
        when(eventRepository.findByIncidentIdIsNull()).thenReturn(new ArrayList<>(List.of(event)));
        when(signalLoader.getAll()).thenReturn(List.of());

        incidentService.processEvents();

        verify(eventRepository).findByIncidentIdIsNull();
        verify(signalLoader).getAll();
        verify(incidentRepository, never()).save(any());
    }

    private EventEntity createEvent(Long id, Integer eventId, String machineName) {
        EventEntity event = new EventEntity();
        event.setId(id);
        event.setEventId(eventId);
        event.setMachineName(machineName);
        event.setTimeCreated(LocalDateTime.now());
        event.setExtra("{}");
        return event;
    }
}
