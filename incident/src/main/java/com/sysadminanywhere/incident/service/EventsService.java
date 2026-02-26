package com.sysadminanywhere.incident.service;

import io.cloudsoft.winrm4j.client.*;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventsService {

    private final PowerShellExecutor powerShellExecutor;

    public EventsService(PowerShellExecutor powerShellExecutor) {
        this.powerShellExecutor = powerShellExecutor;
    }

    @Scheduled(cron = "${cron.expression}")
    public void load() {
        log.info("Load events started");

        execute(100L);

        log.info("Events loaded successfully");
    }

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
            System.out.println(response.getStdOut());
        }

        if (response.getStdErr() != null && !response.getStdErr().isEmpty()) {
            if (!response.getStdErr().contains("<Objs") && !response.getStdErr().contains("progress")) {
                log.error("Real errors: {}", response.getStdErr());
            }
        }
    }

}