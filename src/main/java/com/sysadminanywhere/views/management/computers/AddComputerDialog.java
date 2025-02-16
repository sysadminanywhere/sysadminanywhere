package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class AddComputerDialog extends Dialog {

    private final ComputersService computersService;

    public AddComputerDialog(ComputersService computersService, Runnable onSearch) {
        this.computersService = computersService;

        setHeaderTitle("New computer");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(computersService.getLdapService());
        containerField.setValue(computersService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtName = new TextField("Name");
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField("Description");
        formLayout.setColspan(txtDescription, 2);

        TextField txtLocation = new TextField("Location");
        formLayout.setColspan(txtLocation, 2);

        VerticalLayout checkboxGroup = new VerticalLayout();
        formLayout.setColspan(checkboxGroup, 2);
        Checkbox chkAccountEnabled = new Checkbox("Account enabled");

        checkboxGroup.add(chkAccountEnabled);

        formLayout.add(containerField, txtName, txtDescription, txtLocation, checkboxGroup);
        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            ComputerEntry computer = new ComputerEntry();
            computer.setCn(txtName.getValue());
            computer.setDescription(txtDescription.getValue());
            computer.setLocation(txtLocation.getValue());
            try {
                ComputerEntry newComputer = computersService.add(containerField.getValue(), computer, chkAccountEnabled.getValue());

                onSearch.run();

                Notification notification = Notification.show("Computer added");
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