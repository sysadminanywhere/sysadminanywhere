package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.service.UsersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.dialog.Dialog;

import java.awt.*;

@PageTitle("User details")
@Route(value = "management/users/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class UserDetailsView extends Div implements BeforeEnterObserver {

    private String id;
    private final UsersService usersService;
    UserEntry user;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    Binder<UserEntry> binder = new Binder<>(UserEntry.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        updateView();
    }

    private void updateView() {
        if (id != null) {
            user = usersService.getByCN(id);

            if (user != null) {
                binder.readBean(user);

                lblName.setText(user.getCn());
                lblDescription.setText(user.getDescription());
            }
        }
    }

    public UserDetailsView(UsersService usersService) {
        this.usersService = usersService;

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText("Name");
        lblName.setWidth("100%");

        lblDescription.setText("Description");
        lblDescription.setWidth("100%");

        add(verticalLayout);

        MenuBar menuBar = new MenuBar();
        menuBar.addItem("Update", event -> {
            updateForm().open();
        });
        menuBar.addItem("Delete", event -> {
            deleteDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        VerticalLayout verticalLayout2 = new VerticalLayout();
        verticalLayout2.add(lblName);
        verticalLayout2.add(lblDescription);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.add(verticalLayout2, menuBar);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtDisplayName = new TextField("Display name");
        txtDisplayName.setReadOnly(true);
        binder.bind(txtDisplayName, UserEntry::getDisplayName, null);

        TextField txtCompany = new TextField("Company");
        txtCompany.setReadOnly(true);
        binder.bind(txtCompany, UserEntry::getCompany, null);

        TextField txtTitle = new TextField("Title");
        txtTitle.setReadOnly(true);
        binder.bind(txtTitle, UserEntry::getTitle, null);

        TextField txtEmail = new TextField("Email");
        txtEmail.setReadOnly(true);
        binder.bind(txtEmail, UserEntry::getEmailAddress, null);

        TextField txtMobilePhone = new TextField("Mobile phone");
        txtMobilePhone.setReadOnly(true);
        binder.bind(txtMobilePhone, UserEntry::getMobilePhone, null);

        TextField txtOfficePhone = new TextField("Office phone");
        txtOfficePhone.setReadOnly(true);
        binder.bind(txtOfficePhone, UserEntry::getOfficePhone, null);

        TextField txtHomePhone = new TextField("Home phone");
        txtHomePhone.setReadOnly(true);
        binder.bind(txtHomePhone, UserEntry::getOfficePhone, null);

        formLayout.add(txtDisplayName, txtCompany, txtTitle, txtEmail, txtMobilePhone, txtOfficePhone, txtHomePhone);

        verticalLayout.add(formLayout);
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure you want to permanently delete this user?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            usersService.delete(user.getDistinguishedName());
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

    private Dialog updateForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Updating user");
        dialog.setMaxWidth("800px");

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

        dialog.add(tabSheet);

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
                user = usersService.update(entry);
                updateView();

                Notification notification = Notification.show("User updated");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            dialog.close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

}