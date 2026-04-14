package com.sysadminanywhere.views.settings;

import com.sysadminanywhere.control.ThemeSwitcher;
import com.sysadminanywhere.model.DisplayNamePattern;
import com.sysadminanywhere.model.LoginPattern;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.security.AuthenticatedUser;
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
@PageTitle("Settings")
@Route(value = "settings/settings")
public class SettingsView extends VerticalLayout {

    private final SettingsService settingsService;
    private final MessageSource messageSource;

    private Settings settings;

    public SettingsView(SettingsService settingsService,
                        MessageSource messageSource) {

        this.settingsService = settingsService;
        this.messageSource = messageSource;

        settings = settingsService.getSettings();
        if (settings == null) settings = new Settings();

        Button saveButton = new Button("Save", e -> {
            settingsService.setSettings(settings);

            Notification notification = Notification.show("Settings saved");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        add(getColorMode(), getUserPatterns(), getLanguageSelector(), saveButton);
    }

    private Card getColorMode() {
        Card card = new Card();
        card.setTitle("Theme");
        card.setWidthFull();

        card.add(new ThemeSwitcher());

        return card;
    }

    private Card getUserPatterns() {
        Card card = new Card();
        card.setTitle("User patterns");
        card.setWidthFull();

        ComboBox<String> cmbDisplayNamePattern = new ComboBox<>("Display name pattern");
        cmbDisplayNamePattern.setMinWidth("400px");
        cmbDisplayNamePattern.setItems(Arrays.stream(DisplayNamePattern.values()).map(DisplayNamePattern::getTitle).collect(Collectors.toList()));

        ComboBox<String> cmbLoginPattern = new ComboBox<>("User account name pattern");
        cmbLoginPattern.setMinWidth("400px");
        cmbLoginPattern.setItems(Arrays.stream(LoginPattern.values()).map(LoginPattern::getTitle).collect(Collectors.toList()));

        TextField txtDefaultPassword = new TextField("Set default password for new users");
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
        card.setTitle("Language");
        card.setWidthFull();

        ComboBox<String> cmbLanguage = new ComboBox<>("Select Language");
        cmbLanguage.setMinWidth("400px");
        cmbLanguage.setItems("en", "ru", "de");
        cmbLanguage.setItemLabelGenerator(lang -> {
            switch (lang) {
                case "en":
                    return "English";
                case "ru":
                    return "Русский";
                case "de":
                    return "Deutsch";
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