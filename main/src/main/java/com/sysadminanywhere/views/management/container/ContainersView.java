package com.sysadminanywhere.views.management.container;

import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import com.sysadminanywhere.common.directory.dto.BulkOperationResult;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.domain.SearchScope;
import com.sysadminanywhere.model.Entry;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.*;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.views.management.computers.AddComputerDialog;
import com.sysadminanywhere.views.management.contacts.AddContactDialog;
import com.sysadminanywhere.views.management.groups.AddGroupDialog;
import com.sysadminanywhere.views.management.users.AddUserDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.vaadin.tatu.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route(value = "management/containers")
@Uses(Icon.class)
public class ContainersView extends Div implements MenuControl, HasDynamicTitle {

    private final LdapService ldapService;
    private final AuthenticatedUser authenticatedUser;
    private final SettingsService settingsService;
    private final UsersService usersService;
    private final ComputersService computersService;
    private final GroupsService groupsService;
    private final ContactsService contactsService;
    private final PrintersService printersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    private Grid<Entry> grid;
    private String selected;

    public ContainersView(LdapService ldapService,
                          AuthenticatedUser authenticatedUser,
                          SettingsService settingsService,
                          UsersService usersService,
                          ComputersService computersService,
                          GroupsService groupsService,
                          ContactsService contactsService,
                          PrintersService printersService,
                          MessageSource messageSource,
                          LocaleService localeService) {

        this.ldapService = ldapService;
        this.authenticatedUser = authenticatedUser;
        this.settingsService = settingsService;
        this.usersService = usersService;
        this.computersService = computersService;
        this.groupsService = groupsService;
        this.contactsService = contactsService;
        this.printersService = printersService;
        this.messageSource = messageSource;
        this.localeService = localeService;

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

        SplitLayout splitLayout = new SplitLayout(tree, layout);
        tree.setMinWidth("250px");
        splitLayout.setHeightFull();

        add(splitLayout);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private String getMessage(String key, Object... arguments) {
        return messageSource.getMessage(key, arguments, localeService.getCurrentLocale());
    }

    private Component createGrid() {
        grid = new Grid<>(Entry.class, false);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
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

            icon.addClassName(item.isDisabled() ? "disabled-object-icon" : "enabled-object-icon");

            Span text = new Span(item.getCn());

            layout.add(icon, text);
            layout.setSpacing(true);
            return layout;
        })).setHeader(getMessage("common.cn")).setAutoWidth(true);

        grid.addColumn("type").setHeader(getMessage("common.type")).setAutoWidth(true);
        grid.addColumn("description").setHeader(getMessage("common.description"));

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("management/" + item.getItem().getType() + "s/" + item.getItem().getCn() + "/details"));
        });

        grid.setItems(query -> ldapService.search(
                query.getPage(),
                query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query).toString(),
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
        MenuHelper.createIconItem(menuBar, "/icons/trash.svg", getMessage("common.delete"), event -> confirmBulkDelete());
        MenuHelper.createIconItem(menuBar, "/icons/options.svg", getMessage("bulk.move_selected"), event -> confirmBulkMove());

        MenuItem menuAdd = menuBar.addItem(getMessage("common.new"));

        SubMenu subMenu = menuAdd.getSubMenu();
        subMenu.addItem(getMessage("containers_view.user"), menuItemClickEvent -> {
            addUserDialog(this::refreshGrid).open();
        });
        subMenu.addItem(getMessage("containers_view.computer"), menuItemClickEvent -> {
            addComputerDialog(this::refreshGrid).open();
        });
        subMenu.addItem(getMessage("containers_view.group"), menuItemClickEvent -> {
            addGroupDialog(this::refreshGrid).open();
        });
        subMenu.addItem(getMessage("containers_view.contact"), menuItemClickEvent -> {
            addContactDialog(this::refreshGrid).open();
        });

        return menuBar;
    }

    private void confirmBulkDelete() {
        if (grid == null || grid.getSelectedItems().isEmpty()) {
            Notification.show(getMessage("bulk.no_selection"));
            return;
        }
        int selectedCount = grid.getSelectedItems().size();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getMessage("common.delete"));
        dialog.add(new Paragraph(getMessage("bulk.delete_confirm_text", selectedCount)));
        Button cancel = new Button(getMessage("common.cancel"), event -> dialog.close());
        Button confirm = new Button(getMessage("common.execute"), event -> {
            Map<String, List<String>> byType = new HashMap<>();
            int unsupported = 0;
            for (Entry item : grid.getSelectedItems()) {
                String type = item.getType() == null ? "" : item.getType().toLowerCase();
                if (item.getDistinguishedName() == null || item.getDistinguishedName().isBlank()
                        || !List.of("user", "computer", "group", "contact", "printer").contains(type)) {
                    unsupported++;
                } else {
                    byType.computeIfAbsent(type, key -> new ArrayList<>()).add(item.getDistinguishedName());
                }
            }
            int updated = 0;
            int failed = unsupported;
            try {
                for (Map.Entry<String, List<String>> values : byType.entrySet()) {
                    BulkOperationResult result = switch (values.getKey()) {
                        case "user" -> usersService.bulkDeleteDistinguishedNames(values.getValue());
                        case "computer" -> computersService.bulkDeleteDistinguishedNames(values.getValue());
                        case "group" -> groupsService.bulkDeleteDistinguishedNames(values.getValue());
                        case "contact" -> contactsService.bulkDeleteDistinguishedNames(values.getValue());
                        case "printer" -> printersService.bulkDeleteDistinguishedNames(values.getValue());
                        default -> null;
                    };
                    updated += result == null ? 0 : result.getUpdated();
                    failed += result == null || result.getFailures() == null
                            ? values.getValue().size() : result.getFailures().size();
                }
            } catch (Exception exception) {
                failed += selectedCount - updated - failed;
            }
            grid.deselectAll();
            refreshGrid();
            Notification notification = Notification.show(getMessage(failed == 0 ? "bulk.success" : "bulk.error", updated));
            notification.addThemeVariants(failed == 0 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
            dialog.close();
        });
        confirm.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    private void confirmBulkMove() {
        if (grid == null || grid.getSelectedItems().isEmpty()) {
            Notification.show(getMessage("bulk.no_selection"));
            return;
        }
        ContainerField target = new ContainerField(ldapService, messageSource, localeService);
        target.setWidthFull();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getMessage("bulk.move_selected"));
        dialog.add(new Paragraph(getMessage("bulk.move_confirm_text", grid.getSelectedItems().size())));
        dialog.add(new Paragraph(getMessage("bulk.preview", grid.getSelectedItems().stream()
                .map(Entry::getCn).limit(20).collect(java.util.stream.Collectors.joining(", ")))));
        dialog.add(target);
        Button cancel = new Button(getMessage("common.cancel"), event -> dialog.close());
        Button confirm = new Button(getMessage("common.execute"), event -> {
            if (target.getValue() == null || target.getValue().isBlank()) {
                Notification.show(getMessage("bulk.move_required"));
                return;
            }
            List<String> dns = grid.getSelectedItems().stream().map(Entry::getDistinguishedName)
                    .filter(name -> name != null && !name.isBlank()).toList();
            BulkOperationResult result = ldapService.bulkMove(dns, target.getValue());
            int updated = result == null ? 0 : result.getUpdated();
            int failed = result == null || result.getFailures() == null ? dns.size() : result.getFailures().size();
            Notification notification = Notification.show(getMessage(failed == 0 ? "bulk.success" : "bulk.error", updated));
            notification.addThemeVariants(failed == 0 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
            grid.deselectAll();
            refreshGrid();
            dialog.close();
        });
        confirm.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    private Dialog addUserDialog(Runnable onSearch) {
        return new AddUserDialog(usersService, messageSource, localeService, authenticatedUser, settingsService, onSearch);
    }

    private Dialog addComputerDialog(Runnable onSearch) {
        return new AddComputerDialog(computersService, messageSource, localeService, onSearch);
    }

    private Dialog addGroupDialog(Runnable onSearch) {
        return new AddGroupDialog(groupsService, messageSource, localeService, onSearch);
    }

    private Dialog addContactDialog(Runnable onSearch) {
        return new AddContactDialog(contactsService, messageSource, localeService, onSearch);
    }

    public String getPageTitle() {
        return getMessage("containers_view.title");
    }

}
