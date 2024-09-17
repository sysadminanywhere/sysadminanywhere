package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.views.MainLayout;
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

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        if (id != null) {
            ComputerEntry computer = computersService.getByCN(id);

            if (computer != null) {
                lblName.setText(computer.getCn());
                lblDescription.setText(computer.getDescription());
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
        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        menuBar.addItem("Edit", event -> {
            menuBar.getUI().ifPresent(ui ->
                    ui.navigate("management/computers/test/edit"));
        });
        MenuItem menuManagement = menuBar.addItem("Management");
        menuBar.addItem("Delete", event -> {
            menuBar.getUI().ifPresent(ui ->
                    ui.navigate("management/computers/test/edit"));
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

        VerticalLayout verticalLayout2 = new VerticalLayout();
        verticalLayout2.add(lblName);
        verticalLayout2.add(lblDescription);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.add(verticalLayout2, menuBar);

        verticalLayout.add(horizontalLayout);

    }

}
