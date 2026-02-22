package com.sysadminanywhere.incident.service;

import io.cloudsoft.winrm4j.client.*;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.AuthSchemes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventsService {

    @Value("${wef.server}")
    private String hostname;

    @Value("${wef.port}")
    private int port;

    @Value("${wef.username}")
    private String username;

    @Value("${wef.password}")
    private String password;

    @Value("${wef.use.ssl}")
    private boolean useSsl;

    /*

     ┌───────────── second (0-59)
     │ ┌───────────── minute (0 - 59)
     │ │ ┌───────────── hour (0 - 23)
     │ │ │ ┌───────────── day of the month (1 - 31)
     │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)
     │ │ │ │ │ ┌───────────── day of the week (0 - 7)
     │ │ │ │ │ │          (0 or 7 is Sunday, or MON-SUN)
     │ │ │ │ │ │
     * * * * * *

    "0 0 12 * * *" every day at 12:00

    */

    @SneakyThrows
    @Scheduled(cron = "${cron.expression}")
    public void load() {
        log.info("Load events started");

        execute(100l);

        log.info("Events loaded successfully");
    }

    @SneakyThrows
    private void execute(long lastRecordId) {
        WinRmClientContext context = WinRmClientContext.newInstance();

        WinRmTool tool = WinRmTool.Builder.builder(hostname, username, password)
                .authenticationScheme(AuthSchemes.BASIC)
                .port(port)
                .useHttps(useSsl)
                .context(context)
                //.disableCertificateChecks(true)
                .build();

//        String psScript = """
//                Get-WinEvent -FilterHashtable @{
//                    LogName='ForwardedEvents';
//                    Id=4624,4625,4740,4720,4726,4728,4732,5136
//                } -MaxEvents 500 |
//                Select-Object RecordId, Id, TimeCreated, MachineName, LevelDisplayName, Message |
//                ConvertTo-Json -Depth 3
//                """;

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
                        EventData   = $eventData
                    }
                } | ConvertTo-Json -Depth 6
                """;

        WinRmToolResponse response = tool.executePs(psScript);

        if (response.getStdOut() != null && !response.getStdOut().isEmpty()) {
            System.out.println(response.getStdOut());
        }

        if (response.getStdErr() != null && !response.getStdErr().isEmpty()) {
            if (!response.getStdErr().contains("<Objs") && !response.getStdErr().contains("progress")) {
                log.error("Real errors: {}", response.getStdErr());
            }
        }

        context.shutdown();
    }

}