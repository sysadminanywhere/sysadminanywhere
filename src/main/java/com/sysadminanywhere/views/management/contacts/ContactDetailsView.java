package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.ad.ContactEntry;
import com.sysadminanywhere.service.ContactsService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Contact details")
@Route(value = "management/contacts/:id?/details")
@PermitAll
@Uses(Upload.class)
@Uses(Icon.class)
public class ContactDetailsView extends Div implements BeforeEnterObserver, MenuControl {

    private String id;
    private final ContactsService contactsService;
    private ContactEntry contact;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    Avatar avatar = new Avatar();

    Binder<ContactEntry> binder = new Binder<>(ContactEntry.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        updateView();
    }

    private Runnable updateRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        };
    }

   private void updateView() {
        if (id != null) {
            contact = contactsService.getByCN(id);

            if (contact != null) {
                binder.readBean(contact);

                lblName.setText(contact.getCn());
                lblDescription.setText(contact.getDescription());

                avatar.setName(contact.getName());
            }
        }
    }

    public ContactDetailsView(ContactsService contactsService) {
        this.contactsService = contactsService;

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText("Name");
        lblName.setWidth("100%");

        lblDescription.setText("Description");
        lblDescription.setWidth("100%");

        avatar.setThemeName("xlarge");

        add(verticalLayout);

        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        horizontalLayout.add(avatar, verticalLayout2);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtDisplayName = new TextField("Display name");
        txtDisplayName.setReadOnly(true);
        binder.bind(txtDisplayName, ContactEntry::getDisplayName, null);

        TextField txtCompany = new TextField("Company");
        txtCompany.setReadOnly(true);
        binder.bind(txtCompany, ContactEntry::getCompany, null);

        TextField txtTitle = new TextField("Title");
        txtTitle.setReadOnly(true);
        binder.bind(txtTitle, ContactEntry::getTitle, null);

        TextField txtEmail = new TextField("Email");
        txtEmail.setReadOnly(true);
        binder.bind(txtEmail, ContactEntry::getEmailAddress, null);

        TextField txtMobilePhone = new TextField("Mobile phone");
        txtMobilePhone.setReadOnly(true);
        binder.bind(txtMobilePhone, ContactEntry::getMobilePhone, null);

        TextField txtOfficePhone = new TextField("Office phone");
        txtOfficePhone.setReadOnly(true);
        binder.bind(txtOfficePhone, ContactEntry::getOfficePhone, null);

        TextField txtHomePhone = new TextField("Home phone");
        txtHomePhone.setReadOnly(true);
        binder.bind(txtHomePhone, ContactEntry::getHomePhone, null);

        formLayout.add(txtDisplayName, txtCompany, txtTitle, txtEmail, txtMobilePhone, txtOfficePhone, txtHomePhone);

        verticalLayout.add(formLayout);
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure you want to permanently delete this contact?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            contactsService.delete(contact.getDistinguishedName());
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

    private Dialog updateDialog() {
        return new UpdateContactDialog(contactsService, contact, updateRunnable());
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();
        MenuHelper.createIconItem(menuBar, "/icons/pencil.svg", "Update", event -> {
            updateDialog().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/trash.svg", "Delete", event -> {
            deleteDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        return menuBar;
    }
}