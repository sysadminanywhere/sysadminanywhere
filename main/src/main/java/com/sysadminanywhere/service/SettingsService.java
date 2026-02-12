package com.sysadminanywhere.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.User;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.repository.UserRepository;
import com.sysadminanywhere.security.AuthenticatedUser;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SettingsService {

    private final UserRepository userRepository;
    private final AuthenticatedUser authenticatedUser;

    public SettingsService(UserRepository userRepository, AuthenticatedUser authenticatedUser) {
        this.userRepository = userRepository;
        this.authenticatedUser = authenticatedUser;
    }

    public Settings getSettings() {
        if (authenticatedUser.get().isPresent()) {
            User user = authenticatedUser.get().get();
            String json = user.getSettings();
            if (json == null || json.isEmpty()) {
                Settings settings = new Settings();
                setSettings(settings);
                return settings;
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    Settings settings = objectMapper.readValue(json, Settings.class);
                    return settings;
                } catch (JsonProcessingException e) {
                    return new Settings();
                }
            }
        }
        return new Settings();
    }

    @SneakyThrows
    public void setSettings(Settings settings) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(settings);

        if (authenticatedUser.get().isPresent()) {
            Optional<User> user = userRepository.findByUsername(authenticatedUser.get().get().getUsername());
            if (user.isPresent()) {
                user.get().setSettings(json);
            }
        }
    }

}