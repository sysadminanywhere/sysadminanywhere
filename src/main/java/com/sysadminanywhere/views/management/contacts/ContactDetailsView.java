package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.model.ContactEntry;
import com.sysadminanywhere.service.ContactsService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Contact details")
@Route(value = "management/contacts/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ContactDetailsView extends Div implements BeforeEnterObserver {

    private String id;
    private final ContactsService contactsService;
    private ContactEntry contact;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    MenuBar menuBar;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        if (id != null) {
            contact = contactsService.getByCN(id);

            if (contact != null) {
                lblName.setText(contact.getCn());
                lblDescription.setText(contact.getDescription());

                addMenu(contact);
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

        add(verticalLayout);

        menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        VerticalLayout verticalLayout2 = new VerticalLayout();
        verticalLayout2.add(lblName);
        verticalLayout2.add(lblDescription);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.add(verticalLayout2, menuBar);

        verticalLayout.add(horizontalLayout);
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

    private void addMenu(ContactEntry contact) {
        menuBar.addItem("Update", event -> {
            menuBar.getUI().ifPresent(ui ->
                    ui.navigate("management/contacts/" + contact.getCn() + "/update"));
        });
        menuBar.addItem("Delete", event -> {
            deleteDialog().open();
        });
    }

}