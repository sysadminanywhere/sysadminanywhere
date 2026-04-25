package com.sysadminanywhere.service;

import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocaleService {

    private final SettingsService settingsService;
    private final AuthenticatedUser authenticatedUser;

    public LocaleService(SettingsService settingsService, AuthenticatedUser authenticatedUser) {
        this.settingsService = settingsService;
        this.authenticatedUser = authenticatedUser;
    }

    public Locale getCurrentLocale() {
        Settings settings = settingsService.getSettings();
        if (settings == null || settings.getLanguage() == null) {
            return new Locale("en");
        }
        
        String language = settings.getLanguage().toLowerCase();
        return switch (language) {
            case "ru" -> new Locale("ru");
            case "de" -> new Locale("de");
            case "fr" -> new Locale("fr");
            case "es" -> new Locale("es");
            case "pt" -> new Locale("pt");
            case "zh" -> new Locale("zh");
            case "ja" -> new Locale("ja");
            default -> new Locale("en");
        };
    }
}
