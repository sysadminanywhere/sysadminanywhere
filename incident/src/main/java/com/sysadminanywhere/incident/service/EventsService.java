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

    @Value("${ldap.host.server}")
    private String hostname;

    @Value("${ldap.host.username}")
    private String username;

    @Value("${ldap.host.password}")
    private String password;

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

        execute();

        log.info("Events loaded successfully");
    }

    @SneakyThrows
    private void execute() {
        WinRmClientContext context = WinRmClientContext.newInstance();

        WinRmTool tool = WinRmTool.Builder.builder(hostname, username, password)
                .authenticationScheme(AuthSchemes.BASIC)
                .port(5985)
                .useHttps(false)
                .context(context)
                //.disableCertificateChecks(true)
                .build();

        // Чтение последних 10 событий из Forwarded Events
        String psCommand =
                "Get-WinEvent -LogName 'ForwardedEvents' -MaxEvents 10 | " +
                        "Select-Object TimeCreated, Id, LevelDisplayName, ProviderName, Message | " +
                        "ConvertTo-Json -Compress";

        WinRmToolResponse response = tool.executePs(psCommand);

        if (response.getStdOut() != null && !response.getStdOut().isEmpty()) {
            System.out.println("Forwarded Events:");
            System.out.println(response.getStdOut());
        }

        // Проверяем stderr, но игнорируем CLIXML прогресс
        if (response.getStdErr() != null && !response.getStdErr().isEmpty()) {
            // Проверяем, не является ли stderr просто CLIXML прогрессом
            if (!response.getStdErr().contains("<Objs") && !response.getStdErr().contains("progress")) {
                System.err.println("Real errors: " + response.getStdErr());
            }
        }

        context.shutdown();
    }

}