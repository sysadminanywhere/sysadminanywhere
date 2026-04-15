package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.common.directory.model.ContactEntry;
import com.sysadminanywhere.service.ContactsService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;

public class UpdateContactDialog extends Dialog {

    private final ContactsService contactsService;
    private ContactEntry contact;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public UpdateContactDialog(ContactsService contactsService, ContactEntry contactEntry, MessageSource messageSource, LocaleService localeService, Runnable updateView) {
        this.contactsService = contactsService;
        this.contact = contactEntry;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("update_contact_dialog.title"));
        setWidth("800px");

        TabSheet tabSheet = new TabSheet();

        Tab tabName = new Tab(getMessage("update_contact_dialog.tab_name"));
        Tab tabMain = new Tab(getMessage("update_contact_dialog.tab_main"));
        Tab tabAddress = new Tab(getMessage("update_contact_dialog.tab_address"));
        Tab tabTelephones = new Tab(getMessage("update_contact_dialog.tab_telephones"));

        FormLayout formName = new FormLayout();
        FormLayout formMain = new FormLayout();
        FormLayout formAddress = new FormLayout();
        FormLayout formTelephones = new FormLayout();

        // Name

        TextField txtDisplayName = new TextField(getMessage("update_contact_dialog.display_name"));
        formMain.setColspan(txtDisplayName, 2);
        txtDisplayName.setValue(contact.getDisplayName());
        txtDisplayName.setRequired(true);

        TextField txtFirstName = new TextField(getMessage("update_contact_dialog.first_name"));
        txtFirstName.setValue(contact.getFirstName());
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField(getMessage("update_contact_dialog.initials"));
        txtInitials.setValue(contact.getInitials());
        TextField txtLastName = new TextField(getMessage("update_contact_dialog.last_name"));
        txtLastName.setValue(contact.getLastName());
        txtLastName.setRequired(true);

        formName.add(txtDisplayName, txtFirstName, txtInitials, txtLastName);

        // Main

        TextField txtTitle = new TextField(getMessage("update_contact_dialog.job_title"));
        txtTitle.setValue(contact.getTitle());

        TextField txtOffice = new TextField(getMessage("update_contact_dialog.office"));
        txtOffice.setValue(contact.getOffice());

        TextField txtDepartment = new TextField(getMessage("update_contact_dialog.department"));
        txtDepartment.setValue(contact.getDepartment());

        TextField txtCompany = new TextField(getMessage("update_contact_dialog.company"));
        txtCompany.setValue(contact.getCompany());

        TextField txtTelephone = new TextField(getMessage("update_contact_dialog.telephone"));
        txtTelephone.setValue(contact.getOfficePhone());

        TextField txtEmailAddress = new TextField(getMessage("update_contact_dialog.email"));
        txtEmailAddress.setValue(contact.getEmailAddress());

        TextField txtHomePage = new TextField(getMessage("update_contact_dialog.home_page"));
        txtHomePage.setValue(contact.getHomePage());

        TextField txtDescription = new TextField(getMessage("update_contact_dialog.description"));
        txtDescription.setValue(contact.getDescription());

        formMain.add(txtTitle, txtOffice, txtDepartment, txtCompany, txtTelephone, txtEmailAddress, txtHomePage, txtDescription);

        // Address

        TextField txtStreetAddress = new TextField(getMessage("update_contact_dialog.street"));
        txtStreetAddress.setValue(contact.getStreetAddress());

        TextField txtPOBox = new TextField(getMessage("update_contact_dialog.po_box"));
        txtPOBox.setValue(contact.getPOBox());

        TextField txtCity = new TextField(getMessage("update_contact_dialog.city"));
        txtCity.setValue(contact.getCity());

        TextField txtState = new TextField(getMessage("update_contact_dialog.state"));
        txtState.setValue(contact.getState());

        TextField txtPostalCode = new TextField(getMessage("update_contact_dialog.postal_code"));
        txtPostalCode.setValue(contact.getPostalCode());

        formAddress.add(txtStreetAddress, txtPOBox, txtCity, txtState, txtPostalCode);

        // Telephones

        TextField txtHomePhone = new TextField(getMessage("update_contact_dialog.home_phone"));
        txtHomePhone.setValue(contact.getHomePhone());

        TextField txtMobilePhone = new TextField(getMessage("update_contact_dialog.mobile_phone"));
        txtMobilePhone.setValue(contact.getMobilePhone());

        TextField txtFax = new TextField(getMessage("update_contact_dialog.fax"));
        txtFax.setValue(contact.getFax());

        formTelephones.add(txtHomePhone, txtMobilePhone, txtFax);

        tabSheet.add(tabName, formName);
        tabSheet.add(tabMain, formMain);
        tabSheet.add(tabAddress, formAddress);
        tabSheet.add(tabTelephones, formTelephones);

        add(tabSheet);

        Button saveButton = new com.vaadin.flow.component.button.Button(getMessage("common.save"), e -> {
            ContactEntry entry = contact;

            entry.setDisplayName(txtDisplayName.getValue());
            entry.setFirstName(txtFirstName.getValue());
            entry.setInitials(txtInitials.getValue());
            entry.setLastName(txtLastName.getValue());

            entry.setTitle(txtTitle.getValue());
            entry.setOffice(txtOffice.getValue());
            entry.setDepartment(txtDepartment.getValue());
            entry.setCompany(txtCompany.getValue());
            entry.setOfficePhone(txtTelephone.getValue());
            entry.setEmailAddress(txtEmailAddress.getValue());
            entry.setHomePage(txtHomePage.getValue());
            entry.setDescription(txtDescription.getValue());

            entry.setStreetAddress(txtStreetAddress.getValue());
            entry.setPOBox(txtPOBox.getValue());
            entry.setCity(txtCity.getValue());
            entry.setState(txtState.getValue());
            entry.setPostalCode(txtPostalCode.getValue());

            entry.setHomePhone(txtHomePhone.getValue());
            entry.setMobilePhone(txtMobilePhone.getValue());
            entry.setFax(txtFax.getValue());

            try {
                contact = contactsService.update(entry);
                updateView.run();

                Notification notification = Notification.show(getMessage("update_contact_dialog.contact_updated"));
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
