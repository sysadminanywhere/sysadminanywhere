package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.incident.entity.EventEntity;
import com.sysadminanywhere.incident.model.Event;
import com.sysadminanywhere.incident.repository.EventRepository;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EventsService {

    private final PowerShellExecutor powerShellExecutor;
    private final ObjectMapper objectMapper;
    private final EventRepository eventRepository;
    private final IncidentService incidentService;

    public EventsService(PowerShellExecutor powerShellExecutor,
                         ObjectMapper objectMapper,
                         EventRepository eventRepository,
                         IncidentService incidentService) {

        this.powerShellExecutor = powerShellExecutor;
        this.objectMapper = objectMapper;
        this.eventRepository = eventRepository;
        this.incidentService = incidentService;
    }

    @Scheduled(cron = "${cron.expression}")
    public void load() {
        log.info("Load events started");

        execute();

        log.info("Events loaded successfully");
    }

    private void execute() {

        Long lastRecordId = Optional.ofNullable(eventRepository.findTopByOrderByRecordIdDesc())
                .map(EventEntity::getRecordId)
                .orElse(0L);

        String psScript = """
                Get-WinEvent -FilterHashtable @{
                    LogName='ForwardedEvents';
                    Id=4624,4625,4740,4720,4726,4728,4732,5136
                } -MaxEvents 1000 |
                ForEach-Object {
                
                    $xml = [xml]$_.ToXml()
                
                    $eventData = @{}
                    foreach ($d in $xml.Event.EventData.Data) {
                        $eventData[$d.Name] = $d.'#text'
                    }
                
                    [PSCustomObject]@{
                        RecordId    = $_.RecordId
                        EventId     = $_.Id
                        TimeCreated = $_.TimeCreated.ToString("o")
                        MachineName = $_.MachineName
                        LevelDisplayName = $_.LevelDisplayName
                        Message = $_.Message
                        EventData   = $eventData
                    }
                } | ConvertTo-Json -Depth 6 -Compress
                """;

        try {

            WinRmToolResponse response = powerShellExecutor.execute(psScript);

            if (response.getStdOut() != null && !response.getStdOut().isEmpty()) {
                List<Event> events = objectMapper.readValue(response.getStdOut(), new TypeReference<>() {
                });

                log.info("Loaded {} events", events.size());

                for (Event event : events) {
                    if (event.getRecordId() > lastRecordId) {
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
                }
            }

            if (response.getStdErr() != null && !response.getStdErr().isEmpty()) {
                if (!response.getStdErr().contains("<Objs") && !response.getStdErr().contains("progress")) {
                    log.error("Real errors: {}", response.getStdErr());
                }
            }

        } catch (Exception e) {
            log.error("Error loading events", e);
        }

        incidentService.processEvents();
    }

}