package com.sysadminanywhere.views.management.container;

import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.SearchScope;
import com.sysadminanywhere.model.Entry;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.vaadin.tatu.Tree;

@RolesAllowed("admins")
@PageTitle("Containers")
@Route(value = "management/containers")
@PermitAll
@Uses(Icon.class)
public class ContainersView extends Div implements MenuControl {

    private final LdapService ldapService;

    private Grid<Entry> grid;
    private String selected;

    public ContainersView(LdapService ldapService) {
        this.ldapService = ldapService;

        setSizeFull();

        Tree<Container> tree = new Tree<>(Container::getName);
        tree.setHeightFull();

        Containers containers = ldapService.getContainers();
        tree.setItems(containers.getRootContainers(), containers::getChildContainers);

        tree.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selected = event.getValue().getDistinguishedName();
                refreshGrid();
            }
        });

        VerticalLayout layout = new VerticalLayout(createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        SplitLayout splitLayout = new SplitLayout(tree, layout);
        tree.setMinWidth("250px");
        splitLayout.setHeightFull();

        add(splitLayout);
    }

    private Component createGrid() {
        grid = new Grid<>(Entry.class, false);
        grid.addColumn("cn").setAutoWidth(true);
        grid.addColumn("type").setAutoWidth(true);
        grid.addColumn("description");

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("management/" + item.getItem().getType() + "s/" + item.getItem().getCn() + "/details"));
        });

        grid.setItems(query -> ldapService.search(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                selected, "(cn=*)", SearchScope.ONELEVEL).stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    @Override
    public MenuBar getMenu() {
        return new MenuBar();
    }

}