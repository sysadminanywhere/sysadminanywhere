package com.sysadminanywhere.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.entity.SettingEntity;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.repository.SettingsRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings getSettings(LoginEntity login) {
        Optional<SettingEntity> setting = settingsRepository.findByLogin(login);
        if (setting.isEmpty()) {
            Settings settings = new Settings();
            setSettings(login, settings);
            return settings;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = setting.get().getJson();
            try {
                Settings settings = objectMapper.readValue(json, Settings.class);
                return settings;
            } catch (JsonProcessingException e) {
                return new Settings();
            }
        }
    }

    @SneakyThrows
    public void setSettings(LoginEntity login, Settings settings) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(settings);

        Optional<SettingEntity> setting = settingsRepository.findByLogin(login);
        SettingEntity settingEntity;

        if (setting.isEmpty()) {
            settingEntity = new SettingEntity();
            settingEntity.setLogin(login);
        } else {
            settingEntity = setting.get();
        }

        settingEntity.setJson(json);

        settingsRepository.save(settingEntity);
    }

}