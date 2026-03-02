package com.sysadminanywhere.incident.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.incident.model.Signal;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

@Service
public class SignalLoader {

    private final ObjectMapper mapper;
    private Map<String, Signal> signals;

    public SignalLoader(ObjectMapper mapper) {
        this.mapper = mapper;
        load();
    }

    private void load() {
        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("signals.json")) {

            Map<String, Signal> raw =
                    mapper.readValue(is, new TypeReference<>() {});

            raw.forEach((id, signal) -> signal.setId(id));

            this.signals = raw;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load signals", e);
        }
    }

    public Collection<Signal> getAll() {
        return signals.values();
    }

    public Signal get(String id) {
        return signals.get(id);
    }

}