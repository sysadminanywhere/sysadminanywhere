package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.inventory.client.ComputersServiceClient;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.InventoryScanRunRepository;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final InventoryScanRunRepository scanRunRepository;
    private final AtomicBoolean scanRunning = new AtomicBoolean();
    private volatile String lastScanError;
    private volatile LocalDateTime scanStartedAt;
    private volatile LocalDateTime scanFinishedAt;
    private volatile Set<String> scanTargets = Set.of();
    private volatile int scanProcessed;
    private volatile int scanTotal;
    private volatile boolean cancelRequested;

    public InventoryService(AuthService authService,
                            ComputersServiceClient computersServiceClient,
                            ComputerRepository computerRepository,
                            SoftwareService softwareService,
                            HardwareService hardwareService,
                            InventoryScanRunRepository scanRunRepository) {

        this.authService = authService;
        this.computersServiceClient = computersServiceClient;
        this.computerRepository = computerRepository;
        this.softwareService = softwareService;
        this.hardwareService = hardwareService;
        this.scanRunRepository = scanRunRepository;
    }

    public boolean startScan() {
        return startScan(List.of());
    }

    public boolean startScan(List<String> computerNames) {
        if (!scanRunning.compareAndSet(false, true)) {
            return false;
        }
        scanTargets = computerNames == null ? Set.of() : computerNames.stream()
                .filter(Objects::nonNull).map(String::trim).filter(name -> !name.isEmpty())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        cancelRequested = false;
        scanStartedAt = LocalDateTime.now();
        scanFinishedAt = null;
        lastScanError = null;
        scanProcessed = 0;
        scanTotal = 0;
        com.sysadminanywhere.inventory.entity.InventoryScanRun run = new com.sysadminanywhere.inventory.entity.InventoryScanRun();
        run.setStartedAt(scanStartedAt);
        run.setStatus("RUNNING");
        run.setTotal((int) computerRepository.count());
        scanRunRepository.save(run);
        CompletableFuture.runAsync(() -> {
            try {
                scan();
                run.setStatus(lastScanError == null ? "COMPLETED" : "FAILED");
            } catch (Exception exception) {
                lastScanError = exception.getMessage();
                run.setStatus("FAILED");
                run.setError(lastScanError);
                log.error("Inventory scan failed", exception);
            } finally {
                scanFinishedAt = LocalDateTime.now();
                scanRunning.set(false);
                scanTargets = Set.of();
                run.setFinishedAt(scanFinishedAt);
                run.setProcessed(scanProcessed);
                run.setTotal(scanTotal);
                if (run.getError() == null) {
                    run.setError(lastScanError);
                }
                scanRunRepository.save(run);
            }
        });
        return true;
    }

    public boolean cancelScan() {
        if (!scanRunning.get()) return false;
        cancelRequested = true;
        return true;
    }

    public com.sysadminanywhere.common.inventory.model.InventoryScanStatus getScanStatus() {
        return new com.sysadminanywhere.common.inventory.model.InventoryScanStatus(
                scanRunning.get(), scanProcessed, scanTotal, scanStartedAt, scanFinishedAt, lastScanError);
    }

    public List<com.sysadminanywhere.common.inventory.model.InventoryScanRun> getScanHistory() {
        return scanRunRepository.findTop20ByOrderByStartedAtDesc().stream()
                .map(run -> new com.sysadminanywhere.common.inventory.model.InventoryScanRun(
                        run.getId(), run.getStartedAt(), run.getFinishedAt(), run.getStatus(),
                        run.getProcessed(), run.getTotal(), run.getError()))
                .toList();
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

        scanTotal = (int) computers.stream()
                .filter(computer -> computer != null && !computer.isDisabled())
                .filter(computer -> scanTargets.isEmpty() || scanTargets.contains(computer.getCn()))
                .count();
        log.info("Found {} computers to scan", scanTotal);

        for (ComputerEntry computerEntry : computers) {
            if (cancelRequested) {
                lastScanError = "Scan cancelled by administrator";
                break;
            }
            if (computerEntry != null && !computerEntry.isDisabled()
                    && (scanTargets.isEmpty() || scanTargets.contains(computerEntry.getCn()))) {
                Computer computer = null;
                try {
                    computer = checkComputer(computerEntry.getCn());
                    if (computerEntry.getCn() == null || computerEntry.getCn().isEmpty()) {
                        log.error("Host name is null or empty for software scan");
                        throw new IllegalArgumentException("Host name is empty");
                    } else {
                        computer.setLastScanStatus("RUNNING");
                        computer.setLastScanError(null);
                        computerRepository.save(computer);
                        softwareService.scanSoftware(computer);
                        hardwareService.scanHardware(computer);

                        computer.setCheckingDate(LocalDateTime.now());
                        computer.setLastScanStatus("SUCCESS");
                        computerRepository.save(computer);
                    }
            } catch (Exception ex) {
                if (computer != null) {
                    computer.setLastScanStatus("ERROR");
                    computer.setLastScanError(ex.getMessage());
                    computerRepository.save(computer);
                }
                log.error("Error scanning on computer {}: {}",
                        computerEntry.getCn(), ex.getMessage(), ex);
            } finally {
                if (computerEntry != null && !computerEntry.isDisabled()
                        && (scanTargets.isEmpty() || scanTargets.contains(computerEntry.getCn()))) {
                    scanProcessed++;
                }
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
