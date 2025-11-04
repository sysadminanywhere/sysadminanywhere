package com.sysadminanywhere.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.Installation;
import com.sysadminanywhere.inventory.entity.Software;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.InstallationRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import com.sysadminanywhere.inventory.wmi.HardwareEntity;
import com.sysadminanywhere.inventory.wmi.SoftwareEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class InventoryService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;
    private final InstallationRepository installationRepository;

    public InventoryService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper mapper,
                            ComputerRepository computerRepository,
                            SoftwareRepository softwareRepository,
                            InstallationRepository installationRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
        this.computerRepository = computerRepository;
        this.softwareRepository = softwareRepository;
        this.installationRepository = installationRepository;
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
    public void scan() {

        log.info("Scan started");

        Message<String> kafkaMessage = MessageBuilder
                .withPayload(mapper.writeValueAsString(new SearchDto("", "(objectClass=computer)", 2, "cn", "useraccountcontrol", "dnshostname")))
                .setHeader(KafkaHeaders.TOPIC, "directory-request")
                .setHeader("correlationId", UUID.randomUUID().toString())
                .setHeader("action", "ldap.search")
                .setHeader("sender", "inventory")
                .setHeader("recipient", "directory")
                .setHeader("method", "computers")
                .build();

        kafkaTemplate.send(kafkaMessage);
    }

    @KafkaListener(topics = "directory-response", groupId = "inventory")
    void listener(@Headers MessageHeaders headers, @Payload String message) {

        String action = headers.get("action").toString();
        String correlationId = headers.get("correlationId").toString();
        String sender = headers.get("sender").toString();
        String recipient = headers.get("recipient").toString();
        String method = headers.get("method").toString();

        if (!recipient.equalsIgnoreCase("inventory"))
            return;

        switch (method) {
            case "computers":
                List<ComputerEntry> computers = getComputers(message);

                log.info("Found {} computers", computers.size());

                for (ComputerEntry computerEntry : computers) {
                    if (!computerEntry.isDisabled()) {
                        requestSoftware(computerEntry.getCn());
                        //scanHardware(computer);
                    }
                }
                break;

            case "software":
                scanSoftware(message);
                break;
        }
    }

    @SneakyThrows
    private void scanSoftware(String message) {

        List<Map<String, Object>> list = mapper.readValue(message, new TypeReference<List<Map<String, Object>>>() {});

        WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
        List<SoftwareEntity> software = wmiResolveService.getValues(list);

        if (!software.isEmpty()) {
            Computer computer = checkComputer(software.get(0).getHostName());
            log.info("On computer {} found {} applications", computer.getName(), software.size());
            for (SoftwareEntity softwareEntity : software) {
                checkSoftware(computer, softwareEntity);
            }

            checkForDeletedSoftware(computer, software);
        }
    }


    private void scanHardware(Computer computer) {
        List<HardwareEntity> hardware = getHardware(computer.getName());
        for (HardwareEntity hardwareEntity : hardware) {
            checkHardware(computer, hardwareEntity);
        }

        checkForDeletedHardware(computer, hardware);
    }

    private void checkHardware(Computer computer, HardwareEntity hardwareEntity) {

    }

    private void checkForDeletedHardware(Computer computer, List<HardwareEntity> hardware) {

    }

    @Transactional
    public void checkSoftware(Computer computer, SoftwareEntity softwareEntity) {
        Software software = checkSoftware(softwareEntity);

        List<Installation> installs = installationRepository.findAllByComputerAndSoftware(computer, software);

        try {

            if (installs.isEmpty()) {
                Installation installation = new Installation();
                installation.setComputer(computer);
                installation.setSoftware(software);
                installation.setCheckingDate(LocalDateTime.now());
                installation.setInstallDate(getLocalDateTime(softwareEntity.getInstallDate()));

                installationRepository.save(installation);
            } else {
                Installation existingInstallation = installs.get(0);
                existingInstallation.setCheckingDate(LocalDateTime.now());
                installationRepository.save(existingInstallation);
            }

        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }

    @Transactional
    public void checkForDeletedSoftware(Computer computer, List<SoftwareEntity> software) {
        List<Installation> installs = installationRepository.findAllByComputer(computer);

        for (Installation installation : installs) {
            List<SoftwareEntity> list = software.stream().filter(c ->
                    c.getName().equalsIgnoreCase(installation.getSoftware().getName())
                            && c.getVendor().equalsIgnoreCase(installation.getSoftware().getVendor())
                            && c.getVersion().equalsIgnoreCase(installation.getSoftware().getVersion())
            ).toList();

            if (list.isEmpty()) {
                log.info("Software {} not found on computer {}", installation.getSoftware(), computer.getName());
                installationRepository.delete(installation);
            }
        }
    }

    public Computer checkComputer(String hostName) {
        List<Computer> computers = computerRepository.findAllByName(hostName);
        if (computers.isEmpty()) {
            Computer computer = new Computer();
            computer.setName(hostName);
            return computerRepository.save(computer);
        } else {
            return computers.get(0);
        }
    }

    public Software checkSoftware(SoftwareEntity softwareEntity) {
        List<Software> software = softwareRepository.findByNameAndVendor(softwareEntity.getName(), softwareEntity.getVendor());
        if (software.isEmpty()) {
            Software soft = new Software();
            soft.setName(softwareEntity.getName());
            soft.setVersion(softwareEntity.getVersion());
            soft.setVendor(softwareEntity.getVendor());

            return softwareRepository.save(soft);
        } else {
            Software soft = software.get(0);
            if (!softwareEntity.getVersion().equalsIgnoreCase(soft.getVersion())) {
                soft.setVersion(softwareEntity.getVersion());
                return softwareRepository.save(soft);
            } else {
                return soft;
            }
        }
    }

    @SneakyThrows
    private void requestSoftware(String hostName) {
        String query = "Select * From Win32_Product";
        Message<String> kafkaMessage = MessageBuilder
                .withPayload(mapper.writeValueAsString(new ExecuteDto(hostName, query)))
                .setHeader(KafkaHeaders.TOPIC, "directory-request")
                .setHeader("correlationId", UUID.randomUUID().toString())
                .setHeader("action", "wmi.execute")
                .setHeader("sender", "inventory")
                .setHeader("recipient", "directory")
                .setHeader("method", "software")
                .build();

        kafkaTemplate.send(kafkaMessage);
    }

    @SneakyThrows
    private List<ComputerEntry> getComputers(String message) {
        List<ComputerEntry> list = new ArrayList<>();

        List<EntryDto> result = List.of(mapper.readValue(message, EntryDto[].class));

        if(result != null) {
            for (EntryDto entry : result) {

                ComputerEntry computerEntry = new ComputerEntry();
                computerEntry.setUserAccountControl(Integer.parseInt(entry.getAttributes().get("useraccountcontrol").toString()));
                computerEntry.setCn(entry.getAttributes().get("cn").toString());

                if (entry.getAttributes().get("dnshostname") != null) {
                    computerEntry.setDnsHostName(entry.getAttributes().get("dnshostname").toString());
                } else {
                    computerEntry.setDnsHostName("");
                }

                list.add(computerEntry);
            }
        }

        return list;
    }

    private LocalDateTime getLocalDateTime(String installDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate ld = LocalDate.parse(installDate, dateTimeFormatter);
        return ld.atStartOfDay();
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable, Map<String, String> filters) {
        return softwareRepository.getSoftwareOnComputer(computerId, pageable);
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, Map<String, String> filters) {
        String name = filters.get("name") + "%";
        String vendor = filters.get("vendor") + "%";
        return softwareRepository.getSoftwareInstallationCount(name, vendor, pageable);
    }

    public Page<ComputerItem> getComputersWithSoftware(Long softwareId, Pageable pageable, Map<String, String> filters) {
        String name = filters.get("name") + "%";
        return computerRepository.getComputersWithSoftware(softwareId, name, pageable);
    }

    private List<HardwareEntity> getHardware(String hostName) {
        return new ArrayList<>();
    }

}