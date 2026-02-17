package com.sysadminanywhere.incident.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.incident.client.ComputersServiceClient;
import com.sysadminanywhere.incident.client.WmiServiceClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class EventsService {

    @Value("${ldap.host.username}")
    private String username;

    @Value("${ldap.host.password}")
    private String password;

    private final AuthService authService;
    private final ComputersServiceClient computersServiceClient;
    private final WmiServiceClient wmiServiceClient;

    public EventsService(AuthService authService, ComputersServiceClient computersServiceClient, WmiServiceClient wmiServiceClient) {
        this.authService = authService;
        this.computersServiceClient = computersServiceClient;
        this.wmiServiceClient = wmiServiceClient;
    }

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

        log.info("Logging in to directory service");
        boolean authenticated = authenticate();
        if (!authenticated) {
            log.error("Failed to authenticate with directory service. Aborting scan.");
            return;
        }

        log.info("Requesting controllers from directory service");
        List<ComputerEntry> controllers = getControllers();

        if (controllers == null) {
            log.error("Received null controllers list from directory service. Aborting scan.");
            return;
        }

        log.info("Events loaded successfully");
    }

    private boolean authenticate() {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            log.error("Username or password for directory service is not set. Aborting scan.");
            return false;
        }

        try {
            JwtResponse jwtResponse = authService.authenticate(username, password);

            if (jwtResponse == null || jwtResponse.token() == null) {
                log.error("Received null JWT response or token");
                return false;
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());
            auth.setDetails(jwtResponse.token());
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info("Successfully authenticated with directory service");
            return true;
        } catch (Exception ex) {
            log.error("Failed to authenticate with directory service: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @SneakyThrows
    private List<ComputerEntry> getControllers() {
        try {
            List<ComputerEntry> computers = computersServiceClient.getList("", "cn", "useraccountcontrol", "dnshostname", "primarygroupid");
            return computers.stream()
                    .filter(c -> c.isDomainController())
                    .toList();
        } catch (Exception ex) {
            log.error("Error getting computers from directory service: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }


}