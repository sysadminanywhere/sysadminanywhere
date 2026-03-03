package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.incident.entity.EventEntity;
import com.sysadminanywhere.incident.model.Event;
import com.sysadminanywhere.incident.repository.EventRepository;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.util.List;

@Component
@Profile("mock") // Активируется только при профиле "mock"
@Slf4j
public class MockPowerShellExecutor implements PowerShellExecutor {

    private final MockWinRMService mockWinRMService;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    private static final int SUCCESS_STATUS_CODE = 0;

    public MockPowerShellExecutor(MockWinRMService mockWinRMService,
                                  EventRepository eventRepository,
                                  ObjectMapper objectMapper) {

        this.mockWinRMService = mockWinRMService;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public WinRmToolResponse execute(String script) {
        log.info("MOCK EXECUTE");

        List<Event> mockEvents = mockWinRMService.generateEvents();

        log.info("Generated {} mock events", mockEvents.size());

        for (Event event : mockEvents) {
            EventEntity eventEntity = new EventEntity();
            eventEntity.setRecordId(event.getRecordId());
            eventEntity.setEventId(event.getEventId());
            eventEntity.setTimeCreated(event.getTimeCreated().toLocalDateTime());
            eventEntity.setMachineName(event.getMachineName());
            eventEntity.setLevelDisplayName(event.getLevelDisplayName());
            eventEntity.setMessage(event.getMessage());
            eventEntity.setExtra(objectMapper.writeValueAsString(event.getEventData()));
            eventRepository.save(eventEntity);
        }

        return new WinRmToolResponse("", "", SUCCESS_STATUS_CODE);
    }

}