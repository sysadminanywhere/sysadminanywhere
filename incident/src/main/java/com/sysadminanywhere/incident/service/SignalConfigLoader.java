package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Service
public class SignalConfigLoader {

    private Map<String, Map<String, Object>> signalConfig;

    @PostConstruct
    public void loadConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Загрузка из classpath (resources)
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("signals.json");

        if (inputStream == null) {
            throw new FileNotFoundException("File 'signals.json' not found in classpath");
        }

        signalConfig = mapper.readValue(
                inputStream,
                new TypeReference<Map<String, Map<String, Object>>>(){}
        );
    }

    public Map<String, Object> getSignal(String signalId) {
        return signalConfig.get(signalId);
    }

    public Set<String> getAllSignalIds() {
        return signalConfig.keySet();
    }
}