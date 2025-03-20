package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.model.ad.ContactEntry;
import com.sysadminanywhere.service.ContactsService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;

public class AddContactDialog extends Dialog {

    private final ContactsService contactsService;

    public AddContactDialog(ContactsService contactsService, Runnable onSearch) {
        this.contactsService = contactsService;

        setHeaderTitle("New contact");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(contactsService.getLdapService());
        containerField.setValue(contactsService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtDisplayName = new TextField("Display name");
        formLayout.setColspan(txtDisplayName, 2);
        txtDisplayName.setRequired(true);

        TextField txtFirstName = new TextField("First name");
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField("Initials");
        TextField txtLastName = new TextField("Last name");
        txtLastName.setRequired(true);

        formLayout.add(containerField,txtDisplayName, txtFirstName, txtInitials, txtLastName);
        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            ContactEntry contact = new ContactEntry();
            contact.setCn(txtDisplayName.getValue());
            contact.setDisplayName(txtDisplayName.getValue());
            contact.setFirstName(txtFirstName.getValue());
            contact.setInitials(txtInitials.getValue());
            contact.setLastName(txtLastName.getValue());
            try {
                ContactEntry newContact = contactsService.add(containerField.getValue(), contact);

                onSearch.run();

                Notification notification = Notification.show("Contact added");
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