package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
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
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public AddComputerDialog(ComputersService computersService, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.computersService = computersService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("add_computer_dialog.title"));
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(computersService.getLdapService());
        containerField.setValue(computersService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtName = new TextField(getMessage("add_computer_dialog.name"));
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField(getMessage("add_computer_dialog.description"));
        formLayout.setColspan(txtDescription, 2);

        TextField txtLocation = new TextField(getMessage("add_computer_dialog.location"));
        formLayout.setColspan(txtLocation, 2);

        VerticalLayout checkboxGroup = new VerticalLayout();
        formLayout.setColspan(checkboxGroup, 2);
        Checkbox chkAccountEnabled = new Checkbox(getMessage("add_computer_dialog.account_enabled"));

        checkboxGroup.add(chkAccountEnabled);

        formLayout.add(containerField, txtName, txtDescription, txtLocation, checkboxGroup);
        add(formLayout);

        Button saveButton = new Button(getMessage("common.save"), e -> {
            ComputerEntry computer = new ComputerEntry();
            computer.setCn(txtName.getValue());
            computer.setDescription(txtDescription.getValue());
            computer.setLocation(txtLocation.getValue());
            try {
                ComputerEntry newComputer = computersService.add(containerField.getValue(), computer, chkAccountEnabled.getValue());

                onSearch.run();

                Notification notification = Notification.show(getMessage("add_computer_dialog.computer_added"));
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
