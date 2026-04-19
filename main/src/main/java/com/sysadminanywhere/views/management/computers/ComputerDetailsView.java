package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.control.MemberOf;
import com.sysadminanywhere.model.wmi.ComputerSystemEntity;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RolesAllowed("ADMIN")
@Route(value = "management/computers/:id?/details")
@Uses(Icon.class)
@Uses(ListBox.class)
public class ComputerDetailsView extends Div implements BeforeEnterObserver, MenuControl, HasDynamicTitle {

    private String id;
    private final ComputersService computersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    ComputerEntry computer;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    MemberOf memberOf;

    Binder<ComputerEntry> binder = new Binder<>(ComputerEntry.class);
    Binder<String> binder2 = new Binder<>(String.class);
    Binder<ComputerSystemEntity> binder3 = new Binder<>(ComputerSystemEntity.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        getComputerInfo();
        updateView();
    }

    public void getComputerInfo() {
        try {
            String address = InetAddress.getByName(id).getHostAddress();
            binder2.readBean(address);
            binder3.readBean(computersService.getComputerSystem(id));
        } catch (UnknownHostException e) {

        }
    }

    private Runnable updateRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        };
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private void updateView() {
        if (id != null) {
            computer = computersService.getByCN(id);

            if (computer != null) {
                binder.readBean(computer);

                lblName.setText(computer.getCn());
                lblDescription.setText(computer.getDescription());

                memberOf.update(computersService.getLdapService(), id);
            }
        }
    }

    public ComputerDetailsView(ComputersService computersService, MessageSource messageSource, LocaleService localeService) {
        this.computersService = computersService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        memberOf = new MemberOf(messageSource, localeService);

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText(getMessage("common.name"));
        lblName.setWidth("100%");

        lblDescription.setText(getMessage("common.description"));
        lblDescription.setWidth("100%");

        add(verticalLayout);


        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        horizontalLayout.add(verticalLayout2);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtLocation = new TextField(getMessage("computer_details_view.location"));
        txtLocation.setReadOnly(true);
        binder.bind(txtLocation, ComputerEntry::getLocation, null);

        TextField txtHostName = new TextField(getMessage("computer_details_view.host_name"));
        txtHostName.setReadOnly(true);
        binder.bind(txtHostName, ComputerEntry::getDnsHostName, null);

        TextField txtOperatingSystem = new TextField(getMessage("computer_details_view.operating_system"));
        txtOperatingSystem.setReadOnly(true);
        binder.bind(txtOperatingSystem, ComputerEntry::getOperatingSystem, null);

        TextField txtVersion = new TextField(getMessage("computer_details_view.version"));
        txtVersion.setReadOnly(true);
        binder.bind(txtVersion, ComputerEntry::getOperatingSystemVersion, null);

        TextField txtServicePack = new TextField(getMessage("computer_details_view.service_pack"));
        txtServicePack.setReadOnly(true);
        binder.bind(txtServicePack, ComputerEntry::getOperatingSystemServicePack, null);

        TextField txtIPAddress = new TextField(getMessage("computer_details_view.ip_address"));
        txtIPAddress.setReadOnly(true);
        binder2.bind(txtIPAddress, String::toLowerCase, null);


        TextField txtManufacturer = new TextField(getMessage("computer_details_view.manufacturer"));
        txtManufacturer.setReadOnly(true);
        binder3.bind(txtManufacturer, ComputerSystemEntity::getManufacturer, null);

        TextField txtModel = new TextField(getMessage("computer_details_view.model"));
        txtModel.setReadOnly(true);
        binder3.bind(txtModel, ComputerSystemEntity::getModel, null);

        formLayout.add(txtLocation, txtHostName, txtOperatingSystem, txtVersion, txtServicePack, txtIPAddress, txtManufacturer, txtModel);

        verticalLayout.add(formLayout);

        Card card = new Card();
        card.add(memberOf);
        verticalLayout.add(card);
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getMessage("common.delete"));
        dialog.setText(getMessage("common.delete_computer_confirmation"));

        dialog.setCancelable(true);

        dialog.setConfirmText(getMessage("common.delete"));
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            computersService.delete(computer.getDistinguishedName());
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

    private Dialog updateDialog() {
        return new UpdateComputerDialog(computersService, computer, messageSource, localeService, updateRunnable());
    }

    private ConfirmDialog rebootDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getMessage("common.reboot"));
        dialog.setText(getMessage("common.reboot_confirmation"));

        dialog.setCancelable(true);

        dialog.setConfirmText(getMessage("common.reboot"));
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            computersService.reboot(id);

            Notification notification = Notification.show(getMessage("computer_details_view.reboot_command_sent"));
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        return dialog;
    }

    private ConfirmDialog shutdownDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getMessage("common.shutdown"));
        dialog.setText(getMessage("common.shutdown_confirmation"));

        dialog.setCancelable(true);

        dialog.setConfirmText(getMessage("common.shutdown"));
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            computersService.shutdown(id);

            Notification notification = Notification.show(getMessage("computer_details_view.shutdown_command_sent"));
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        return dialog;
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);

        MenuHelper.createIconItem(menuBar, "/icons/pencil.svg", getMessage("computer_details_view.update"), event -> {
            updateDialog().open();
        });

        MenuItem menuManagement = menuBar.addItem(getMessage("computer_details_view.management"));

        MenuHelper.createIconItem(menuBar, "/icons/trash.svg", getMessage("computer_details_view.delete"), event -> {
            deleteDialog().open();
        });

        // Create reverse mapping from translated values to English names for URL navigation
        java.util.Map<String, String> translationToEnglishMap = new java.util.HashMap<>();
        translationToEnglishMap.put(getMessage("computer_details_view.processes"), "processes");
        translationToEnglishMap.put(getMessage("computer_details_view.services"), "services");
        translationToEnglishMap.put(getMessage("computer_details_view.events"), "events");
        translationToEnglishMap.put(getMessage("computer_details_view.software"), "software");
        translationToEnglishMap.put(getMessage("computer_details_view.hardware"), "hardware");
        translationToEnglishMap.put(getMessage("computer_details_view.performance"), "performance");

        ComponentEventListener<ClickEvent<MenuItem>> listener = e -> {
            if (computer != null) {
                String translatedText = e.getSource().getText();
                String englishName = translationToEnglishMap.getOrDefault(translatedText, translatedText.toLowerCase());
                e.getSource().getUI().ifPresent(ui ->
                        ui.navigate("management/computers/" + computer.getCn() + "/" + englishName));
            }
        };

        SubMenu subMenuManagement = menuManagement.getSubMenu();
        subMenuManagement.addItem(getMessage("computer_details_view.processes"), listener);
        subMenuManagement.addItem(getMessage("computer_details_view.services"), listener);
        subMenuManagement.addItem(getMessage("computer_details_view.events"), listener);
        subMenuManagement.addSeparator();
        subMenuManagement.addItem(getMessage("computer_details_view.software"), listener);
        subMenuManagement.addItem(getMessage("computer_details_view.hardware"), listener);
        subMenuManagement.addSeparator();
        subMenuManagement.addItem(getMessage("computer_details_view.performance"), listener);
        subMenuManagement.addSeparator();
        subMenuManagement.addItem(getMessage("computer_details_view.reboot"), menuItemClickEvent -> {
            rebootDialog().open();
        });
        subMenuManagement.addItem(getMessage("computer_details_view.shutdown"), menuItemClickEvent -> {
            shutdownDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        return menuBar;
    }

    public String getPageTitle() {
        return getMessage("computer_details_view.title");
    }

}
