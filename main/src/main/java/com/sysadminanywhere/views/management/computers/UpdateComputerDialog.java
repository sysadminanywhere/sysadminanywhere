package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;

public class UpdateComputerDialog extends Dialog {

    private final ComputersService computersService;
    private final ComputerEntry computer;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public UpdateComputerDialog(ComputersService computersService, ComputerEntry computerEntry, MessageSource messageSource, LocaleService localeService, Runnable updateView) {
        this.computersService = computersService;
        this.computer = computerEntry;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("update_computer_dialog.title"));
        setWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtDescription = new TextField(getMessage("update_computer_dialog.description"));
        txtDescription.setValue(computer.getDescription());
        formLayout.setColspan(txtDescription, 2);

        TextField txtLocation = new TextField(getMessage("update_computer_dialog.location"));
        txtLocation.setValue(computer.getLocation());
        formLayout.setColspan(txtLocation, 2);

        formLayout.add(txtDescription, txtLocation);
        add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button(getMessage("common.save"), e -> {
            ComputerEntry entry = computer;
            entry.setDescription(txtDescription.getValue());
            entry.setLocation(txtLocation.getValue());

            try {
                computersService.update(entry);
                updateView.run();

                Notification notification = Notification.show(getMessage("update_computer_dialog.computer_updated"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button(getMessage("common.cancel"), e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

}
