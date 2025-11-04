package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.model.DisplayNamePattern;
import com.sysadminanywhere.model.LoginPattern;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LoginService;
import com.sysadminanywhere.service.SettingsService;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import java.util.Optional;
import java.util.regex.Pattern;

public class AddUserDialog extends Dialog {

    private final UsersService usersService;

    private final AuthenticatedUser authenticatedUser;
    private final LoginService loginService;
    private final SettingsService settingsService;

    private Optional<LoginEntity> loginEntity;
    private Settings settings;

    public AddUserDialog(UsersService usersService, LoginService loginService, AuthenticatedUser authenticatedUser, SettingsService settingsService, Runnable onSearch) {
        this.usersService = usersService;
        this.loginService = loginService;
        this.settingsService = settingsService;
        this.authenticatedUser = authenticatedUser;

        Pattern userDisplayNameFormat = Pattern.compile("");
        Pattern userLoginPattern = Pattern.compile("");
        String userLoginFormat = "";

        Optional<UserEntry> maybeUser = authenticatedUser.getUser();
        if (maybeUser.isPresent()) {
            UserEntry user = maybeUser.get();
            loginEntity = loginService.getLogin(user);
            if (loginEntity.isPresent()) {
                settings = settingsService.getSettings(loginEntity.get());

                userDisplayNameFormat = Pattern.compile(DisplayNamePattern.valueOf(settings.getDisplayNamePattern()).getPattern());
                LoginPattern loginPattern = LoginPattern.valueOf(settings.getLoginPattern());
                userLoginPattern = Pattern.compile(loginPattern.getPattern());
                userLoginFormat = loginPattern.getFormat();
            }
        }

        setHeaderTitle("New user");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(usersService.getLdapService());
        containerField.setValue(usersService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtDisplayName = new TextField("Display name");
        txtDisplayName.setRequired(true);
        formLayout.setColspan(txtDisplayName, 2);

        TextField txtFirstName = new TextField("First name");
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField("Initials");
        TextField txtLastName = new TextField("Last name");
        txtLastName.setRequired(true);

        TextField txtAccountName = new TextField("Account name");
        txtAccountName.setRequired(true);

        PasswordField txtPassword = new PasswordField("Password");
        txtPassword.setRequired(true);
        PasswordField txtConfirmPassword = new PasswordField("Confirm password");
        txtConfirmPassword.setRequired(true);

        VerticalLayout checkboxGroup = new VerticalLayout();
        formLayout.setColspan(checkboxGroup, 2);
        Checkbox chkUserMustChangePassword = new Checkbox("User must change password at next logon");
        chkUserMustChangePassword.setValue(true);
        Checkbox chkUserCannotChangePassword = new Checkbox("User cannot change password");
        Checkbox chkPasswordNeverExpires = new Checkbox("Password never expires");
        chkPasswordNeverExpires.setEnabled(false);
        Checkbox chkAccountDisabled = new Checkbox("Account disabled");

        checkboxGroup.add(chkUserMustChangePassword, chkUserCannotChangePassword, chkPasswordNeverExpires, chkAccountDisabled);

        chkUserMustChangePassword.addValueChangeListener(event -> {
            if (chkUserMustChangePassword.getValue()) {
                chkPasswordNeverExpires.setEnabled(false);
                chkPasswordNeverExpires.setValue(false);
            } else {
                chkPasswordNeverExpires.setEnabled(true);
            }
        });

        chkPasswordNeverExpires.addValueChangeListener(event -> {
            if (chkPasswordNeverExpires.getValue()) {
                chkUserMustChangePassword.setEnabled(false);
                chkUserMustChangePassword.setValue(false);
            } else {
                chkUserMustChangePassword.setEnabled(true);
            }
        });

        Pattern finalUserDisplayNameFormat = userDisplayNameFormat;
        Pattern finalUserLoginPattern = userLoginPattern;
        String finalUserLoginFormat = userLoginFormat;

        txtDisplayName.addValueChangeListener(event -> {
            txtFirstName.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${FirstName}"));
            txtLastName.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${LastName}"));

            if (finalUserDisplayNameFormat.toString().contains("<Middle>"))
                txtInitials.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${Middle}"));

            txtAccountName.setValue(finalUserLoginPattern.matcher(txtDisplayName.getValue()).replaceAll(finalUserLoginFormat).toLowerCase());
        });

        if (settings != null && !settings.getDefaultPassword().isEmpty()) {
            txtPassword.setValue(settings.getDefaultPassword());
            txtConfirmPassword.setValue(settings.getDefaultPassword());
        }

        formLayout.add(containerField, txtDisplayName, txtFirstName, txtInitials, txtLastName, txtAccountName, txtPassword, txtConfirmPassword, checkboxGroup);
        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            UserEntry user = new UserEntry();
            user.setCn(txtDisplayName.getValue());
            user.setDisplayName(txtDisplayName.getValue());
            user.setFirstName(txtFirstName.getValue());
            user.setInitials(txtInitials.getValue());
            user.setLastName(txtLastName.getValue());
            user.setSamAccountName(txtAccountName.getValue());
            try {
                UserEntry newUser = usersService.add(
                        containerField.getValue(),
                        user,
                        txtPassword.getValue(),
                        chkUserCannotChangePassword.getValue(),
                        chkPasswordNeverExpires.getValue(),
                        chkAccountDisabled.getValue(),
                        chkUserMustChangePassword.getValue());

                onSearch.run();

                Notification notification = Notification.show("User added");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

}