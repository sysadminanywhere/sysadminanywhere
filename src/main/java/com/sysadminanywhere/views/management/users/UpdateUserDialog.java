package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;

public class UpdateUserDialog extends Dialog {

    private final UsersService usersService;
    private final UserEntry user;

    public UpdateUserDialog(UsersService usersService, UserEntry userEntry, Runnable updateView) {
        this.usersService = usersService;
        this.user = userEntry;

        setHeaderTitle("Updating user");
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
        txtDisplayName.setValue(user.getDisplayName());
        txtDisplayName.setRequired(true);

        TextField txtFirstName = new TextField("First name");
        txtFirstName.setValue(user.getFirstName());
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField("Initials");
        TextField txtLastName = new TextField("Last name");
        txtLastName.setValue(user.getLastName());
        txtLastName.setRequired(true);

        formName.add(txtDisplayName, txtFirstName, txtInitials, txtLastName);

        // Main

        TextField txtTitle = new TextField("Title");
        txtTitle.setValue(user.getTitle());

        TextField txtOffice = new TextField("Office");
        txtOffice.setValue(user.getOffice());

        TextField txtDepartment = new TextField("Department");
        txtDepartment.setValue(user.getDepartment());

        TextField txtCompany = new TextField("Company");
        txtCompany.setValue(user.getCompany());

        TextField txtTelephone = new TextField("Telephone");
        txtTelephone.setValue(user.getOfficePhone());

        TextField txtEmailAddress = new TextField("E-mail");
        txtEmailAddress.setValue(user.getEmailAddress());

        TextField txtHomePage = new TextField("Home page");
        txtHomePage.setValue(user.getHomePage());

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(user.getDescription());

        formMain.add(txtTitle, txtOffice, txtDepartment, txtCompany, txtTelephone, txtEmailAddress, txtHomePage, txtDescription);

        // Address

        TextField txtStreetAddress = new TextField("Street");
        txtStreetAddress.setValue(user.getStreetAddress());

        TextField txtPOBox = new TextField("P.O. Box");
        txtPOBox.setValue(user.getPOBox());

        TextField txtCity = new TextField("City");
        txtCity.setValue(user.getCity());

        TextField txtState = new TextField("State");
        txtState.setValue(user.getState());

        TextField txtPostalCode = new TextField("Postal code");
        txtPostalCode.setValue(user.getPostalCode());

        formAddress.add(txtStreetAddress, txtPOBox, txtCity, txtState, txtPostalCode);

        // Telephones

        TextField txtHomePhone = new TextField("Home phone");
        txtHomePhone.setValue(user.getHomePhone());

        TextField txtMobilePhone = new TextField("Mobile phone");
        txtMobilePhone.setValue(user.getMobilePhone());

        TextField txtFax = new TextField("Fax");
        txtFax.setValue(user.getFax());

        formTelephones.add(txtHomePhone, txtMobilePhone, txtFax);

        tabSheet.add(tabName, formName);
        tabSheet.add(tabMain, formMain);
        tabSheet.add(tabAddress, formAddress);
        tabSheet.add(tabTelephones, formTelephones);

        add(tabSheet);

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
            UserEntry entry = user;

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
                usersService.update(entry);
                updateView.run();

                Notification notification = Notification.show("User updated");
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