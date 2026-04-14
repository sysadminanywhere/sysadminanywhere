package com.sysadminanywhere.views.settings;

import com.sysadminanywhere.control.ThemeSwitcher;
import com.sysadminanywhere.model.DisplayNamePattern;
import com.sysadminanywhere.model.LoginPattern;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.SettingsService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@RolesAllowed("ADMIN")
@PageTitle("settings_view.title")
@Route(value = "settings/settings")
public class SettingsView extends VerticalLayout {

    private final SettingsService settingsService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    private Settings settings;

    public SettingsView(SettingsService settingsService,
                        MessageSource messageSource,
                        LocaleService localeService) {

        this.settingsService = settingsService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        settings = settingsService.getSettings();
        if (settings == null) settings = new Settings();

        Button saveButton = new Button(getMessage("common.save"), e -> {
            settingsService.setSettings(settings);

            Notification notification = Notification.show(getMessage("common.settings_saved"));
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        add(getColorMode(), getUserPatterns(), getLanguageSelector(), saveButton);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private Card getColorMode() {
        Card card = new Card();
        card.setTitle(getMessage("settings_view.theme"));
        card.setWidthFull();

        card.add(new ThemeSwitcher());

        return card;
    }

    private Card getUserPatterns() {
        Card card = new Card();
        card.setTitle(getMessage("settings_view.user_patterns"));
        card.setWidthFull();

        ComboBox<String> cmbDisplayNamePattern = new ComboBox<>(getMessage("settings_view.display_name_pattern"));
        cmbDisplayNamePattern.setMinWidth("400px");
        cmbDisplayNamePattern.setItems(Arrays.stream(DisplayNamePattern.values()).map(DisplayNamePattern::getTitle).collect(Collectors.toList()));

        ComboBox<String> cmbLoginPattern = new ComboBox<>(getMessage("settings_view.user_account_name_pattern"));
        cmbLoginPattern.setMinWidth("400px");
        cmbLoginPattern.setItems(Arrays.stream(LoginPattern.values()).map(LoginPattern::getTitle).collect(Collectors.toList()));

        TextField txtDefaultPassword = new TextField(getMessage("settings_view.set_default_password"));
        txtDefaultPassword.setMinWidth("400px");

        cmbDisplayNamePattern.setValue(DisplayNamePattern.valueOf(settings.getDisplayNamePattern()).getTitle());
        cmbLoginPattern.setValue(LoginPattern.valueOf(settings.getLoginPattern()).getTitle());
        txtDefaultPassword.setValue(settings.getDefaultPassword());

        cmbDisplayNamePattern.addValueChangeListener(event -> {
            String displayNamePattern = DisplayNamePattern.NONE.name();
            for (DisplayNamePattern pattern : DisplayNamePattern.values()) {
                if (pattern.getTitle().equalsIgnoreCase(cmbDisplayNamePattern.getValue()))
                    displayNamePattern = pattern.name();
            }
            settings.setDisplayNamePattern(displayNamePattern);
        });

        cmbLoginPattern.addValueChangeListener(event -> {
            String loginPattern = LoginPattern.NONE.name();
            for (LoginPattern pattern : LoginPattern.values()) {
                if (pattern.getTitle().equalsIgnoreCase(cmbLoginPattern.getValue()))
                    loginPattern = pattern.name();
            }
            settings.setLoginPattern(loginPattern);
        });

        txtDefaultPassword.addValueChangeListener(event -> {
            String defaultPassword = txtDefaultPassword.getValue();
            settings.setDefaultPassword(defaultPassword);
        });

        card.add(new VerticalLayout(cmbDisplayNamePattern, cmbLoginPattern, txtDefaultPassword));
        return card;
    }

    private Card getLanguageSelector() {
        Card card = new Card();
        card.setTitle(getMessage("settings_view.language"));
        card.setWidthFull();

        ComboBox<String> cmbLanguage = new ComboBox<>(getMessage("settings_view.select_language"));
        cmbLanguage.setMinWidth("400px");
        cmbLanguage.setItems("en", "ru");
        cmbLanguage.setItemLabelGenerator(lang -> {
            switch (lang) {
                case "en":
                    return getMessage("settings_view.english");
                case "ru":
                    return getMessage("settings_view.russian");
                default:
                    return lang;
            }
        });

        cmbLanguage.setValue(settings.getLanguage());

        cmbLanguage.addValueChangeListener(event -> {
            settings.setLanguage(cmbLanguage.getValue());
        });

        card.add(new VerticalLayout(cmbLanguage));
        return card;
    }

}
