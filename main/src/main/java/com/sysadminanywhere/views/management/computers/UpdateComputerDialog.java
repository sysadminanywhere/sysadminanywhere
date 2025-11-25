package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
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

    public UpdateComputerDialog(ComputersService computersService, ComputerEntry computerEntry, Runnable updateView) {
        this.computersService = computersService;
        this.computer = computerEntry;

        setHeaderTitle("Updating computer");
        setWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(computer.getDescription());
        formLayout.setColspan(txtDescription, 2);

        TextField txtLocation = new TextField("Location");
        txtLocation.setValue(computer.getLocation());
        formLayout.setColspan(txtLocation, 2);

        formLayout.add(txtDescription, txtLocation);
        add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
            ComputerEntry entry = computer;
            entry.setDescription(txtDescription.getValue());
            entry.setLocation(txtLocation.getValue());

            try {
                computersService.update(entry);
                updateView.run();

                Notification notification = Notification.show("Computer updated");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

}