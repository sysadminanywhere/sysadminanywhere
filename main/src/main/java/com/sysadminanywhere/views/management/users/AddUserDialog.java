package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.model.DisplayNamePattern;
import com.sysadminanywhere.model.LoginPattern;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.SettingsService;
import com.sysadminanywhere.service.UsersService;
import org.springframework.context.MessageSource;
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

import java.util.regex.Pattern;

public class AddUserDialog extends Dialog {

    private final UsersService usersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    private final AuthenticatedUser authenticatedUser;
    private final SettingsService settingsService;

    private final Settings settings;

    public AddUserDialog(UsersService usersService, MessageSource messageSource, LocaleService localeService, AuthenticatedUser authenticatedUser, SettingsService settingsService, Runnable onSearch) {
        this.usersService = usersService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        this.settingsService = settingsService;
        this.authenticatedUser = authenticatedUser;

        Pattern userDisplayNameFormat = Pattern.compile("");
        Pattern userLoginPattern = Pattern.compile("");
        String userLoginFormat = "";

        settings = settingsService.getSettings();

        userDisplayNameFormat = Pattern.compile(DisplayNamePattern.valueOf(settings.getDisplayNamePattern()).getPattern());
        LoginPattern loginPattern = LoginPattern.valueOf(settings.getLoginPattern());
        userLoginPattern = Pattern.compile(loginPattern.getPattern());
        userLoginFormat = loginPattern.getFormat();

        setHeaderTitle(getMessage("add_user_dialog.title"));
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(usersService.getLdapService(), messageSource, localeService);
        containerField.setValue(usersService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtDisplayName = new TextField(getMessage("add_user_dialog.display_name"));
        txtDisplayName.setRequired(true);
        formLayout.setColspan(txtDisplayName, 2);

        TextField txtFirstName = new TextField(getMessage("add_user_dialog.first_name"));
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField(getMessage("add_user_dialog.initials"));
        TextField txtLastName = new TextField(getMessage("add_user_dialog.last_name"));
        txtLastName.setRequired(true);

        TextField txtAccountName = new TextField(getMessage("add_user_dialog.account_name"));
        txtAccountName.setRequired(true);

        PasswordField txtPassword = new PasswordField(getMessage("add_user_dialog.password"));
        txtPassword.setRequired(true);
        PasswordField txtConfirmPassword = new PasswordField(getMessage("add_user_dialog.confirm_password"));
        txtConfirmPassword.setRequired(true);

        VerticalLayout checkboxGroup = new VerticalLayout();
        formLayout.setColspan(checkboxGroup, 2);
        Checkbox chkUserMustChangePassword = new Checkbox(getMessage("add_user_dialog.user_must_change_password"));
        chkUserMustChangePassword.setValue(true);
        Checkbox chkUserCannotChangePassword = new Checkbox(getMessage("add_user_dialog.user_cannot_change_password"));
        Checkbox chkPasswordNeverExpires = new Checkbox(getMessage("add_user_dialog.password_never_expires"));
        chkPasswordNeverExpires.setEnabled(false);
        Checkbox chkAccountDisabled = new Checkbox(getMessage("add_user_dialog.account_disabled"));

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

        if (!finalUserDisplayNameFormat.pattern().isEmpty()
                && !finalUserLoginPattern.pattern().isEmpty()
                && !finalUserLoginFormat.isEmpty()) {
            txtDisplayName.addValueChangeListener(event -> {
                txtFirstName.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${FirstName}"));
                txtLastName.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${LastName}"));

                if (finalUserDisplayNameFormat.toString().contains("<Middle>"))
                    txtInitials.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${Middle}"));

                txtAccountName.setValue(finalUserLoginPattern.matcher(txtDisplayName.getValue()).replaceAll(finalUserLoginFormat).toLowerCase());
            });
        }

        if (settings != null && !settings.getDefaultPassword().isEmpty()) {
            txtPassword.setValue(settings.getDefaultPassword());
            txtConfirmPassword.setValue(settings.getDefaultPassword());
        }

        formLayout.add(containerField, txtDisplayName, txtFirstName, txtInitials, txtLastName, txtAccountName, txtPassword, txtConfirmPassword, checkboxGroup);
        add(formLayout);

        Button saveButton = new Button(getMessage("common.save"), e -> {
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

                Notification notification = Notification.show(getMessage("add_user_dialog.user_added"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button(getMessage("common.cancel"), e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

}
