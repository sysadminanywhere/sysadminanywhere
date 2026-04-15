package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.UsersService;
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

public class UpdateUserDialog extends Dialog {

    private final UsersService usersService;
    private final UserEntry user;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public UpdateUserDialog(UsersService usersService, UserEntry userEntry, MessageSource messageSource, LocaleService localeService, Runnable updateView) {
        this.usersService = usersService;
        this.user = userEntry;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("update_user_dialog.title"));
        setWidth("800px");

        TabSheet tabSheet = new TabSheet();

        Tab tabName = new Tab(getMessage("update_user_dialog.tab_name"));
        Tab tabMain = new Tab(getMessage("update_user_dialog.tab_main"));
        Tab tabAddress = new Tab(getMessage("update_user_dialog.tab_address"));
        Tab tabTelephones = new Tab(getMessage("update_user_dialog.tab_telephones"));

        FormLayout formName = new FormLayout();
        FormLayout formMain = new FormLayout();
        FormLayout formAddress = new FormLayout();
        FormLayout formTelephones = new FormLayout();

        // Name

        TextField txtDisplayName = new TextField(getMessage("update_user_dialog.display_name"));
        formMain.setColspan(txtDisplayName, 2);
        txtDisplayName.setValue(user.getDisplayName());
        txtDisplayName.setRequired(true);

        TextField txtFirstName = new TextField(getMessage("update_user_dialog.first_name"));
        txtFirstName.setValue(user.getFirstName());
        txtFirstName.setRequired(true);
        TextField txtInitials = new TextField(getMessage("update_user_dialog.initials"));
        txtInitials.setValue(user.getInitials());
        TextField txtLastName = new TextField(getMessage("update_user_dialog.last_name"));
        txtLastName.setValue(user.getLastName());
        txtLastName.setRequired(true);

        formName.add(txtDisplayName, txtFirstName, txtInitials, txtLastName);

        // Main

        TextField txtTitle = new TextField(getMessage("update_user_dialog.job_title"));
        txtTitle.setValue(user.getTitle());

        TextField txtOffice = new TextField(getMessage("update_user_dialog.office"));
        txtOffice.setValue(user.getOffice());

        TextField txtDepartment = new TextField(getMessage("update_user_dialog.department"));
        txtDepartment.setValue(user.getDepartment());

        TextField txtCompany = new TextField(getMessage("update_user_dialog.company"));
        txtCompany.setValue(user.getCompany());

        TextField txtTelephone = new TextField(getMessage("update_user_dialog.telephone"));
        txtTelephone.setValue(user.getOfficePhone());

        TextField txtEmailAddress = new TextField(getMessage("update_user_dialog.email"));
        txtEmailAddress.setValue(user.getEmailAddress());

        TextField txtHomePage = new TextField(getMessage("update_user_dialog.home_page"));
        txtHomePage.setValue(user.getHomePage());

        TextField txtDescription = new TextField(getMessage("update_user_dialog.description"));
        txtDescription.setValue(user.getDescription());

        formMain.add(txtTitle, txtOffice, txtDepartment, txtCompany, txtTelephone, txtEmailAddress, txtHomePage, txtDescription);

        // Address

        TextField txtStreetAddress = new TextField(getMessage("update_user_dialog.street"));
        txtStreetAddress.setValue(user.getStreetAddress());

        TextField txtPOBox = new TextField(getMessage("update_user_dialog.po_box"));
        txtPOBox.setValue(user.getPOBox());

        TextField txtCity = new TextField(getMessage("update_user_dialog.city"));
        txtCity.setValue(user.getCity());

        TextField txtState = new TextField(getMessage("update_user_dialog.state"));
        txtState.setValue(user.getState());

        TextField txtPostalCode = new TextField(getMessage("update_user_dialog.postal_code"));
        txtPostalCode.setValue(user.getPostalCode());

        formAddress.add(txtStreetAddress, txtPOBox, txtCity, txtState, txtPostalCode);

        // Telephones

        TextField txtHomePhone = new TextField(getMessage("update_user_dialog.home_phone"));
        txtHomePhone.setValue(user.getHomePhone());

        TextField txtMobilePhone = new TextField(getMessage("update_user_dialog.mobile_phone"));
        txtMobilePhone.setValue(user.getMobilePhone());

        TextField txtFax = new TextField(getMessage("update_user_dialog.fax"));
        txtFax.setValue(user.getFax());

        formTelephones.add(txtHomePhone, txtMobilePhone, txtFax);

        tabSheet.add(tabName, formName);
        tabSheet.add(tabMain, formMain);
        tabSheet.add(tabAddress, formAddress);
        tabSheet.add(tabTelephones, formTelephones);

        add(tabSheet);

        Button saveButton = new com.vaadin.flow.component.button.Button(getMessage("common.save"), e -> {
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

                Notification notification = Notification.show(getMessage("update_user_dialog.user_updated"));
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
