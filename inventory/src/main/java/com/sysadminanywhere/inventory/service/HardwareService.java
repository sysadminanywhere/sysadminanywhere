package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.inventory.entity.Computer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class HardwareService {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Transactional
    public void scanHardware(Computer computer) {
        String hostName = computer.getName();
        log.info("Scanning hardware on computer {}", hostName);
    }

}
