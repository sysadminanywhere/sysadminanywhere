package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Computer details")
@Route(value = "management/computers/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
@Uses(ListBox.class)
public class ComputerDetailsView extends Div implements BeforeEnterObserver {

    private String id;
    private final ComputersService computersService;
    ComputerEntry computer;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    ListBox<String> listMemberOf = new ListBox<>();

    Binder<ComputerEntry> binder = new Binder<>(ComputerEntry.class);
    Binder<String> binder2 = new Binder<>(String.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        try {
            String address = InetAddress.getByName(id).getHostAddress();
            binder2.readBean(address);
        } catch (UnknownHostException e) {

        }

        updateView();
    }

    private void updateView() {
        if (id != null) {
            computer = computersService.getByCN(id);

            if (computer != null) {
                binder.readBean(computer);

                lblName.setText(computer.getCn());
                lblDescription.setText(computer.getDescription());

                listMemberOf.clear();
                if (computer.getMemberOf() != null) {
                    List<String> items = new ArrayList<>();
                    if (computer.getPrimaryGroupId() != 0)
                        items.add(ADHelper.getPrimaryGroup(computer.getPrimaryGroupId()));
                    for (String item : computer.getMemberOf()) {
                        items.add(ADHelper.ExtractCN(item));
                    }
                    listMemberOf.setItems(items);
                }
            }
        }
    }

    public ComputerDetailsView(ComputersService computersService) {
        this.computersService = computersService;

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText("Name");
        lblName.setWidth("100%");

        lblDescription.setText("Description");
        lblDescription.setWidth("100%");

        add(verticalLayout);

        MenuBar menuBar = new MenuBar();
        //menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);

        MenuHelper.createIconItem(menuBar, VaadinIcon.EDIT, "Update", event -> {
            updateForm().open();
        });

        MenuItem menuManagement = menuBar.addItem("Management");

        MenuHelper.createIconItem(menuBar, VaadinIcon.TRASH, "Delete", event -> {
            deleteDialog().open();
        });

        ComponentEventListener<ClickEvent<MenuItem>> listener = e -> {
            if (computer != null) {
                e.getSource().getUI().ifPresent(ui ->
                        ui.navigate("management/computers/" + computer.getCn() + "/" + e.getSource().getText().toLowerCase()));
            }
        };

        SubMenu subMenuManagement = menuManagement.getSubMenu();
        subMenuManagement.addItem("Processes", listener);
        subMenuManagement.addItem("Services", listener);
        subMenuManagement.addItem("Events", listener);
        subMenuManagement.add(new Hr());
        subMenuManagement.addItem("Software", listener);
//        subMenuManagement.addItem("Hardware", listener);
        subMenuManagement.add(new Hr());
        subMenuManagement.addItem("Performance", listener);
        subMenuManagement.add(new Hr());
        subMenuManagement.addItem("Reboot", menuItemClickEvent -> {
            rebootDialog().open();
        });
        subMenuManagement.addItem("Shutdown", menuItemClickEvent -> {
            shutdownDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout horizontalLayout2 = new HorizontalLayout(menuBar);
        horizontalLayout2.setWidthFull();
        horizontalLayout2.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        horizontalLayout.add(verticalLayout2, horizontalLayout2);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtLocation = new TextField("Location");
        txtLocation.setReadOnly(true);
        binder.bind(txtLocation, ComputerEntry::getLocation, null);

        TextField txtHostName = new TextField("Host name");
        txtHostName.setReadOnly(true);
        binder.bind(txtHostName, ComputerEntry::getDnsHostName, null);

        TextField txtOperatingSystem = new TextField("Operating system");
        txtOperatingSystem.setReadOnly(true);
        binder.bind(txtOperatingSystem, ComputerEntry::getOperatingSystem, null);

        TextField txtVersion = new TextField("Version");
        txtVersion.setReadOnly(true);
        binder.bind(txtVersion, ComputerEntry::getOperatingSystemVersion, null);

        TextField txtServicePack = new TextField("Service pack");
        txtServicePack.setReadOnly(true);
        binder.bind(txtServicePack, ComputerEntry::getOperatingSystemServicePack, null);

        TextField txtIPAddress = new TextField("IP address");
        txtServicePack.setReadOnly(true);
        binder2.bind(txtIPAddress, String::toLowerCase, null);

        formLayout.add(txtLocation, txtHostName, txtOperatingSystem, txtVersion, txtServicePack, txtIPAddress);

        verticalLayout.add(formLayout);

        verticalLayout.add(new Hr(), new H5("Member of"), listMemberOf);
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure you want to permanently delete this computer?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            computersService.delete(computer.getDistinguishedName());
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

    private Dialog updateForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Updating user");
        dialog.setWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(computer.getDescription());
        formLayout.setColspan(txtDescription, 2);

        TextField txtLocation = new TextField("Location");
        txtLocation.setValue(computer.getLocation());
        formLayout.setColspan(txtLocation, 2);

        formLayout.add(txtDescription, txtLocation);
        dialog.add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
            ComputerEntry entry = computer;
            entry.setDescription(txtDescription.getValue());
            entry.setLocation(txtLocation.getValue());

            try {
                computer = computersService.update(entry);
                updateView();

                Notification notification = Notification.show("Computer updated");
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

    private ConfirmDialog rebootDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Reboot");
        dialog.setText("Are you sure you want to reboot this computer?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Reboot");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            computersService.reboot(id);

            Notification notification = Notification.show("Reboot command sent");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        return dialog;
    }

    private ConfirmDialog shutdownDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Shutdown");
        dialog.setText("Are you sure you want to shutdown this computer?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Shutdown");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            computersService.shutdown(id);

            Notification notification = Notification.show("Shutdown command sent");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        return dialog;
    }

}