package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.wmi.SoftwareEntity;
import com.sysadminanywhere.service.ComputersService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

@PageTitle("Hardware")
@Route(value = "management/computers/:id?/hardware")
@PermitAll
@Uses(Icon.class)
public class ComputerHardwareView extends Div implements BeforeEnterObserver {

    private String id;

    private Grid<SoftwareEntity> grid;

    private final ComputersService computersService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerHardwareView(ComputersService computersService) {
        this.computersService = computersService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Computer system", "BIOS", "Base board", "Disk drive", "Operating system", "Disk partition", "Processor", "Video controller", "Physical memory", "Logical disk");

//        listBox.addValueChangeListener(event -> {
//
//        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        layout.add(listBox, createGrid());

        add(layout);
    }

    private Component createGrid() {
        grid = new Grid<>(SoftwareEntity.class, false);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("value").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}