package com.sysadminanywhere.views.settings;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.model.DisplayNamePattern;
import com.sysadminanywhere.model.LoginPattern;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LoginService;
import com.sysadminanywhere.service.SettingsService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.button.Button;
import org.vaadin.addons.themeselect.ThemeRadioGroup;
import com.vaadin.flow.component.card.Card;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Settings")
@Route(value = "settings/settings")
@PermitAll
public class SettingsView extends VerticalLayout {

    private AuthenticatedUser authenticatedUser;
    private final LoginService loginService;
    private final SettingsService settingsService;

    private Optional<LoginEntity> loginEntity;
    private Settings settings;

    public SettingsView(AuthenticatedUser authenticatedUser,
                        LoginService loginService,
                        SettingsService settingsService) {
        this.loginService = loginService;
        this.authenticatedUser = authenticatedUser;
        this.settingsService = settingsService;

        Optional<UserEntry> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            UserEntry user = maybeUser.get();
            loginEntity = loginService.getLogin(user);
            if (loginEntity.isPresent()) {
                settings = settingsService.getSettings(loginEntity.get());
            }
        }

        add(getColorMode(), getUserPatterns());
    }

    private Card getColorMode() {
        Card card = new Card();
        card.setTitle("Theme");
        card.setWidthFull();

        card.add(new ThemeRadioGroup("Color Mode"));

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

        if (settings != null) {
            cmbDisplayNamePattern.setValue(DisplayNamePattern.valueOf(settings.getDisplayNamePattern()).getTitle());
            cmbLoginPattern.setValue(LoginPattern.valueOf(settings.getLoginPattern()).getTitle());
            txtDefaultPassword.setValue(settings.getDefaultPassword());
        }

        Button saveButton = new Button("Save", e -> {
            String displayNamePattern = DisplayNamePattern.NONE.name();
            for (DisplayNamePattern pattern : DisplayNamePattern.values()) {
                if (pattern.getTitle().equalsIgnoreCase(cmbDisplayNamePattern.getValue()))
                    displayNamePattern = pattern.name();
            }

            String loginPattern = LoginPattern.NONE.name();
            for (LoginPattern pattern : LoginPattern.values()) {
                if (pattern.getTitle().equalsIgnoreCase(cmbLoginPattern.getValue()))
                    loginPattern = pattern.name();
            }

            String defaultPassword = txtDefaultPassword.getValue();

            if (settings == null) settings = new Settings();

            settings.setDisplayNamePattern(displayNamePattern);
            settings.setLoginPattern(loginPattern);
            settings.setDefaultPassword(defaultPassword);

            if (loginEntity.isPresent()) {
                settingsService.setSettings(loginEntity.get(), settings);

                Notification notification = Notification.show("Settings saved");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        card.add(new VerticalLayout(cmbDisplayNamePattern, cmbLoginPattern, txtDefaultPassword, saveButton));
        return card;
    }

}