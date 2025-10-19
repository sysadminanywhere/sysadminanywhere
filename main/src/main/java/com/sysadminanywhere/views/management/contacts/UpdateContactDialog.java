package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.common.directory.model.ContactEntry;
import com.sysadminanywhere.service.ContactsService;
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

    public UpdateContactDialog(ContactsService contactsService, ContactEntry contactEntry, Runnable updateView) {
        this.contactsService = contactsService;
        this.contact = contactEntry;

        setHeaderTitle("Updating contact");
        setWidth("800px");

        TabSheet tabSheet = new TabSheet();

        Tab tabName = new Tab("Name");
        Tab tabMain = new Tab("Main");
        Tab tabAddress = new Tab("Address");
        Tab tabTelephones = new Tab("Telephones");

        FormLayout formName = new FormLayout();
        FormLayout formMain = new FormLayout();
        FormLayout formAddress = new FormLayout();
        FormLayout formTelephones = new FormLayout();

        // Name

        TextField txtDisplayName = new TextField("Display name");
        formMain.setColspan(txtDisplayName, 2);
        txtDisplayName.setValue(contact.getDisplayName());
        txtDisplayName.setRequired(true);

        TextField txtFirstName = new TextField("First name");
        txtFirstName.setValue(contact.getFirstName());
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField("Initials");
        txtInitials.setValue(contact.getInitials());
        TextField txtLastName = new TextField("Last name");
        txtLastName.setValue(contact.getLastName());
        txtLastName.setRequired(true);

        formName.add(txtDisplayName, txtFirstName, txtInitials, txtLastName);

        // Main

        TextField txtTitle = new TextField("Title");
        txtTitle.setValue(contact.getTitle());

        TextField txtOffice = new TextField("Office");
        txtOffice.setValue(contact.getOffice());

        TextField txtDepartment = new TextField("Department");
        txtDepartment.setValue(contact.getDepartment());

        TextField txtCompany = new TextField("Company");
        txtCompany.setValue(contact.getCompany());

        TextField txtTelephone = new TextField("Telephone");
        txtTelephone.setValue(contact.getOfficePhone());

        TextField txtEmailAddress = new TextField("E-mail");
        txtEmailAddress.setValue(contact.getEmailAddress());

        TextField txtHomePage = new TextField("Home page");
        txtHomePage.setValue(contact.getHomePage());

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(contact.getDescription());

        formMain.add(txtTitle, txtOffice, txtDepartment, txtCompany, txtTelephone, txtEmailAddress, txtHomePage, txtDescription);

        // Address

        TextField txtStreetAddress = new TextField("Street");
        txtStreetAddress.setValue(contact.getStreetAddress());

        TextField txtPOBox = new TextField("P.O. Box");
        txtPOBox.setValue(contact.getPOBox());

        TextField txtCity = new TextField("City");
        txtCity.setValue(contact.getCity());

        TextField txtState = new TextField("State");
        txtState.setValue(contact.getState());

        TextField txtPostalCode = new TextField("Postal code");
        txtPostalCode.setValue(contact.getPostalCode());

        formAddress.add(txtStreetAddress, txtPOBox, txtCity, txtState, txtPostalCode);

        // Telephones

        TextField txtHomePhone = new TextField("Home phone");
        txtHomePhone.setValue(contact.getHomePhone());

        TextField txtMobilePhone = new TextField("Mobile phone");
        txtMobilePhone.setValue(contact.getMobilePhone());

        TextField txtFax = new TextField("Fax");
        txtFax.setValue(contact.getFax());

        formTelephones.add(txtHomePhone, txtMobilePhone, txtFax);

        tabSheet.add(tabName, formName);
        tabSheet.add(tabMain, formMain);
        tabSheet.add(tabAddress, formAddress);
        tabSheet.add(tabTelephones, formTelephones);

        add(tabSheet);

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
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

                Notification notification = Notification.show("Contact updated");
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