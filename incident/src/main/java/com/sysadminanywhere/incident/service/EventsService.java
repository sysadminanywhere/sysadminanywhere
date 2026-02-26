package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sysadminanywhere.incident.model.SecurityEvent;
import io.cloudsoft.winrm4j.client.*;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EventsService {

    private final PowerShellExecutor powerShellExecutor;
    private final ObjectMapper objectMapper;

    public EventsService(PowerShellExecutor powerShellExecutor, ObjectMapper objectMapper) {
        this.powerShellExecutor = powerShellExecutor;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "${cron.expression}")
    public void load() {
        log.info("Load events started");

        execute(100L);

        log.info("Events loaded successfully");
    }

    @SneakyThrows
    private void execute(long lastRecordId) {
        String psScript = """
                Get-WinEvent -FilterHashtable @{
                    LogName='ForwardedEvents';
                    Id=4624,4625,4740,4720,4726,4728,4732,5136
                } -MaxEvents 200 |
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
                } | ConvertTo-Json -Depth 6
                """;

        WinRmToolResponse response = powerShellExecutor.execute(psScript);

        if (response.getStdOut() != null && !response.getStdOut().isEmpty()) {
            List<SecurityEvent> events = objectMapper.readValue(response.getStdOut(), new TypeReference<>() {});

            log.info("Loaded {} events", events.size());
            events.forEach(event -> log.debug("Event: {}", event));
        }

        if (response.getStdErr() != null && !response.getStdErr().isEmpty()) {
            if (!response.getStdErr().contains("<Objs") && !response.getStdErr().contains("progress")) {
                log.error("Real errors: {}", response.getStdErr());
            }
        }
    }

}