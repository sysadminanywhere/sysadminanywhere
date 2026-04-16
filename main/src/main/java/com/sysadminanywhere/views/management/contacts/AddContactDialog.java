package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.common.directory.model.ContactEntry;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.service.ContactsService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;

public class AddContactDialog extends Dialog {

    private final ContactsService contactsService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public AddContactDialog(ContactsService contactsService, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.contactsService = contactsService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("add_contact_dialog.title"));
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(contactsService.getLdapService(), messageSource, localeService);
        containerField.setValue(contactsService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtDisplayName = new TextField(getMessage("add_contact_dialog.display_name"));
        formLayout.setColspan(txtDisplayName, 2);
        txtDisplayName.setRequired(true);

        TextField txtFirstName = new TextField(getMessage("add_contact_dialog.first_name"));
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField(getMessage("add_contact_dialog.initials"));
        TextField txtLastName = new TextField(getMessage("add_contact_dialog.last_name"));
        txtLastName.setRequired(true);

        formLayout.add(containerField,txtDisplayName, txtFirstName, txtInitials, txtLastName);
        add(formLayout);

        Button saveButton = new Button(getMessage("common.save"), e -> {
            ContactEntry contact = new ContactEntry();
            contact.setCn(txtDisplayName.getValue());
            contact.setDisplayName(txtDisplayName.getValue());
            contact.setFirstName(txtFirstName.getValue());
            contact.setInitials(txtInitials.getValue());
            contact.setLastName(txtLastName.getValue());
            try {
                ContactEntry newContact = contactsService.add(containerField.getValue(), contact);

                onSearch.run();

                Notification notification = Notification.show(getMessage("add_contact_dialog.contact_added"));
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
