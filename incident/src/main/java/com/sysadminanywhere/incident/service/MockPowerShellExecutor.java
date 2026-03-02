package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@Profile("mock")
@Slf4j
public class MockPowerShellExecutor implements PowerShellExecutor {

    private static final String MOCK_RESPONSE_FILE = "mock/winrm-response.json";
    private static final int SUCCESS_STATUS_CODE = 0;
    private static final int ERROR_STATUS_CODE = 1;

    @Override
    public WinRmToolResponse execute(String script) {
        log.info("MOCK: Executing PowerShell (returning test data)");

        try {
            // Читаем JSON из ресурсов
            String mockJson = Files.readString(new ClassPathResource(MOCK_RESPONSE_FILE).getFile().toPath());

            // Десериализуем в список событий
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> events = mapper.readValue(mockJson, new TypeReference<>() {});

            // Текущая дата/время
            OffsetDateTime now = OffsetDateTime.now(ZoneId.systemDefault());
            long recordIdCounter = 1_000_000; // старт RecordId для свежих событий

            for (Map<String, Object> event : events) {
                // Обновляем дату/время на текущую + секундное смещение
                event.put("TimeCreated", now.plusSeconds(recordIdCounter).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                // Обновляем RecordId
                event.put("RecordId", recordIdCounter++);
            }

            // Сериализуем обратно в JSON
            String updatedJson = mapper.writeValueAsString(events);

            return new WinRmToolResponse(updatedJson, "", SUCCESS_STATUS_CODE);

        } catch (Exception e) {
            log.error("Failed to load mock response", e);
            return new WinRmToolResponse("", "Error loading mock data", ERROR_STATUS_CODE);
        }
    }

}