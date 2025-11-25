package com.sysadminanywhere.views.management.container;

import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.domain.SearchScope;
import com.sysadminanywhere.model.Entry;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.*;
import com.sysadminanywhere.views.management.computers.AddComputerDialog;
import com.sysadminanywhere.views.management.contacts.AddContactDialog;
import com.sysadminanywhere.views.management.groups.AddGroupDialog;
import com.sysadminanywhere.views.management.users.AddUserDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
    private final AuthenticatedUser authenticatedUser;
    private final LoginService loginService;
    private final SettingsService settingsService;
    private final UsersService usersService;
    private final ComputersService computersService;
    private final GroupsService groupsService;
    private final ContactsService contactsService;

    private Grid<Entry> grid;
    private String selected;

    public ContainersView(LdapService ldapService,
                          AuthenticatedUser authenticatedUser,
                          LoginService loginService,
                          SettingsService settingsService,
                          UsersService usersService,
                          ComputersService computersService,
                          GroupsService groupsService,
                          ContactsService contactsService) {

        this.ldapService = ldapService;
        this.authenticatedUser = authenticatedUser;
        this.loginService = loginService;
        this.settingsService = settingsService;
        this.usersService = usersService;
        this.computersService = computersService;
        this.groupsService = groupsService;
        this.contactsService = contactsService;

        addClassNames("gridwith-filters-view");
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
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(new ComponentRenderer<>(item -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);

            SvgIcon icon = new SvgIcon("icons/object.svg");

            switch (item.getType().toLowerCase()){
                case "user":
                    icon = new SvgIcon("icons/user.svg");
                    break;
                case "computer":
                    icon = new SvgIcon("icons/computer.svg");
                    break;
                case "group":
                    icon = new SvgIcon("icons/group.svg");
                    break;
                case "printer":
                    icon = new SvgIcon("icons/printer.svg");
                    break;
                case "contact":
                    icon = new SvgIcon("icons/contact.svg");
                    break;
            }

            icon.setColor("grey");

            Span text = new Span(item.getCn());

            layout.add(icon, text);
            layout.setSpacing(true);
            return layout;
        })).setHeader("cn").setAutoWidth(true);

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
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);

        MenuHelper.createIconItem(menuBar, "/icons/refresh.svg", menuItemClickEvent -> {
            refreshGrid();
        });

        MenuItem menuAdd = menuBar.addItem("New");

        SubMenu subMenu = menuAdd.getSubMenu();
        subMenu.addItem("User", menuItemClickEvent -> {
            addUserDialog(this::refreshGrid).open();
        });
        subMenu.addItem("Computer", menuItemClickEvent -> {
            addComputerDialog(this::refreshGrid).open();
        });
        subMenu.addItem("Group", menuItemClickEvent -> {
            addComputerDialog(this::refreshGrid).open();
        });
        subMenu.addItem("Contact", menuItemClickEvent -> {
            addComputerDialog(this::refreshGrid).open();
        });

        return menuBar;
    }

    private Dialog addUserDialog(Runnable onSearch) {
        return new AddUserDialog(usersService, loginService, authenticatedUser, settingsService, onSearch);
    }

    private Dialog addComputerDialog(Runnable onSearch) {
        return new AddComputerDialog(computersService, onSearch);
    }

    private Dialog addGroupDialog(Runnable onSearch) {
        return new AddGroupDialog(groupsService, onSearch);
    }

    private Dialog addContactDialog(Runnable onSearch) {
        return new AddContactDialog(contactsService, onSearch);
    }

}