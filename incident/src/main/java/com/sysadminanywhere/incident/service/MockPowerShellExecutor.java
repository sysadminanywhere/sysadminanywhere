package com.sysadminanywhere.incident.service;

import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;

@Component
@Profile("mock") // Активируется только при профиле "mock"
@Slf4j
public class MockPowerShellExecutor implements PowerShellExecutor {

    private static final String MOCK_RESPONSE_FILE = "mock/winrm-response.json";
    private static final int SUCCESS_STATUS_CODE = 0;     // Успешное выполнение PowerShell
    private static final int ERROR_STATUS_CODE = 1;

    @Override
    public WinRmToolResponse execute(String script) {
        log.info("MOCK: Executing PowerShell (returning test data)");

        try {
            String mockOutput = Files.readString(new ClassPathResource(MOCK_RESPONSE_FILE).getFile().toPath());
            return new WinRmToolResponse(mockOutput, "", SUCCESS_STATUS_CODE);
        } catch (Exception e) {
            log.error("Failed to load mock response", e);
            return new WinRmToolResponse("", "Error loading mock data", ERROR_STATUS_CODE);
        }
    }

}