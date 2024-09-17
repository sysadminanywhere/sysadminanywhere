package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
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

@PageTitle("Computer details")
@Route(value = "management/computers/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ComputerDetailsView extends Div implements BeforeEnterObserver {

    private String id;
    private final ComputersService computersService;
    ComputerEntry computer;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    MenuBar menuBar;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        if (id != null) {
            computer = computersService.getByCN(id);

            if (computer != null) {
                lblName.setText(computer.getCn());
                lblDescription.setText(computer.getDescription());

                addMenu(computer);
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

    private void addMenu(ComputerEntry computer) {
        menuBar.addItem("Edit", event -> {
            menuBar.getUI().ifPresent(ui ->
                    ui.navigate("management/computers/" + computer.getSamAccountName() + "/edit"));
        });
        MenuItem menuManagement = menuBar.addItem("Management");
        menuBar.addItem("Delete", event -> {
            deleteDialog().open();
        });

        SubMenu subMenuManagement = menuManagement.getSubMenu();
        subMenuManagement.addItem("Processes");
        subMenuManagement.addItem("Services");
        subMenuManagement.addItem("Events");
        subMenuManagement.add(new Hr());
        subMenuManagement.addItem("Software");
        subMenuManagement.addItem("Hardware");
        subMenuManagement.add(new Hr());
        subMenuManagement.addItem("Performance");
        subMenuManagement.add(new Hr());
        subMenuManagement.addItem("Reboot");
        subMenuManagement.addItem("Shutdown");
    }

}