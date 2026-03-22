package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.inventory.client.ComputersServiceClient;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class InventoryService {

    @Value("${ldap.host.username}")
    private String username;

    @Value("${ldap.host.password}")
    private String password;

    private final AuthService authService;
    private final ComputersServiceClient computersServiceClient;

    private final ComputerRepository computerRepository;
    private final SoftwareService softwareService;
    private final HardwareService hardwareService;

    public InventoryService(AuthService authService,
                            ComputersServiceClient computersServiceClient,
                            ComputerRepository computerRepository,
                            SoftwareService softwareService,
                            HardwareService hardwareService) {

        this.authService = authService;
        this.computersServiceClient = computersServiceClient;
        this.computerRepository = computerRepository;
        this.softwareService = softwareService;
        this.hardwareService = hardwareService;
    }

    /*
     ┌───────────── second (0-59)
     │ ┌───────────── minute (0 - 59)
     │ │ ┌───────────── hour (0 - 23)
     │ │ │ ┌───────────── day of month (1 - 31)
     │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)
     │ │ │ │ │ ┌───────────── day of week (0 - 7)
     │ │ │ │ │          (0 or 7 is Sunday, or MON-SUN)
     * * * * * *

    "0 0 12 * * *" every day at 12:00

    */

    @SneakyThrows
    @Scheduled(cron = "${cron.expression}")
    public void scan() {
        log.info("Scan started");

        log.info("Logging in to directory service");
        boolean authenticated = authenticate();
        if (!authenticated) {
            log.error("Failed to authenticate with directory service. Aborting scan.");
            return;
        }

        log.info("Requesting computers from directory service");
        List<ComputerEntry> computers = getComputers();

        if (computers == null) {
            log.error("Received null computers list from directory service. Aborting scan.");
            return;
        }

        log.info("Found {} computers", computers.size());

        for (ComputerEntry computerEntry : computers) {
            if (computerEntry != null && !computerEntry.isDisabled()) {
                try {
                    Computer computer = checkComputer(computerEntry.getCn());
                    if (computerEntry.getCn() == null || computerEntry.getCn().isEmpty()) {
                        log.error("Host name is null or empty for software scan");
                        return;
                    } else {
                        softwareService.scanSoftware(computer);
                        hardwareService.scanHardware(computer);

                        computer.setCheckingDate(LocalDateTime.now());
                        computerRepository.save(computer);
                    }
                } catch (Exception ex) {
                    log.error("Error scanning on computer {}: {}",
                            computerEntry.getCn(), ex.getMessage(), ex);
                }
            }
        }

        log.info("Scan stopped");
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
    private List<ComputerEntry> getComputers() {
        try {
            return computersServiceClient.getList("", "cn", "useraccountcontrol", "dnshostname");
        } catch (Exception ex) {
            log.error("Error getting computers from directory service: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public Computer checkComputer(String hostName) {
        if (hostName == null || hostName.isEmpty()) {
            log.error("Host name is null or empty in checkComputer");
            return null;
        }

        try {
            List<Computer> computers = computerRepository.findAllByName(hostName);

            if (computers == null || computers.isEmpty()) {
                Computer computer = new Computer();
                computer.setName(hostName);
                computer.setCheckingDate(LocalDateTime.now());
                Computer savedComputer = computerRepository.save(computer);
                log.debug("Created new computer record for {}", hostName);
                return savedComputer;
            } else {
                log.debug("Found existing computer record for {}", hostName);
                return computers.get(0);
            }
        } catch (Exception ex) {
            log.error("Error checking computer {}: {}", hostName, ex.getMessage(), ex);
            return null;
        }
    }

}