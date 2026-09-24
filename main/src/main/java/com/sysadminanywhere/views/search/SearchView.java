package com.sysadminanywhere.views.search;

import com.sysadminanywhere.common.directory.model.*;
import com.sysadminanywhere.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Route("search")
@RolesAllowed({"ADMIN", "READER"})
public class SearchView extends VerticalLayout implements HasDynamicTitle {

    private final UsersService usersService;
    private final ComputersService computersService;
    private final GroupsService groupsService;
    private final ContactsService contactsService;
    private final PrintersService printersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final TextField query = new TextField();
    private final Button searchButton = new Button();
    private final VerticalLayout results = new VerticalLayout();

    public SearchView(UsersService usersService, ComputersService computersService,
                      GroupsService groupsService, ContactsService contactsService,
                      PrintersService printersService, MessageSource messageSource,
                      LocaleService localeService) {
        this.usersService = usersService;
        this.computersService = computersService;
        this.groupsService = groupsService;
        this.contactsService = contactsService;
        this.printersService = printersService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames("global-search-view", LumoUtility.BoxSizing.BORDER);

        query.setPlaceholder(message("search.placeholder"));
        query.setClearButtonVisible(true);
        query.setWidthFull();
        searchButton.setText(message("search.button"));
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(event -> search());
        query.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, event -> search());

        HorizontalLayout toolbar = new HorizontalLayout(query, searchButton);
        toolbar.addClassName("global-search-toolbar");
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        toolbar.setFlexGrow(1, query);
        results.setPadding(false);
        results.setSpacing(false);
        results.setWidthFull();
        results.addClassName("global-search-results");
        add(toolbar, results);
        showEmptyState();
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private void search() {
        String value = query.getValue() == null ? "" : query.getValue().trim();
        if (value.length() < 2) {
            Notification.show(message("search.min_length"));
            return;
        }
        String escaped = escapeFilter(value);
        results.removeAll();
        addUsers(escaped);
        addComputers(escaped);
        addGroups(escaped);
        addContacts(escaped);
        addPrinters(escaped);
        if (results.getComponentCount() == 0) {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        Span empty = new Span(message("search.empty"));
        empty.addClassName("global-search-empty");
        results.add(empty);
    }

    private void addUsers(String value) {
        List<UserEntry> entries = safe(usersService.getAll(
                "(|(cn=*" + value + "*)(displayName=*" + value + "*)(sAMAccountName=*" + value + "*)(mail=*" + value + "*))",
                "cn", "displayName", "mail", "sAMAccountName"));
        if (entries.isEmpty()) return;
        Grid<UserEntry> grid = new Grid<>();
        grid.addColumn(UserEntry::getCn).setHeader(message("search.name")).setAutoWidth(true);
        grid.addColumn(UserEntry::getDisplayName).setHeader(message("search.display_name")).setAutoWidth(true);
        grid.addColumn(UserEntry::getEmailAddress).setHeader(message("search.email")).setAutoWidth(true);
        grid.setItems(entries);
        grid.addItemClickListener(event -> navigate("management/users", event.getItem().getCn()));
        addResult(message("search.users"), grid);
    }

    private void addComputers(String value) {
        List<ComputerEntry> entries = safe(computersService.getAll(
                "(|(cn=*" + value + "*)(dNSHostName=*" + value + "*)(operatingSystem=*" + value + "*))",
                "cn", "dNSHostName", "operatingSystem"));
        if (entries.isEmpty()) return;
        Grid<ComputerEntry> grid = new Grid<>();
        grid.addColumn(ComputerEntry::getCn).setHeader(message("search.name")).setAutoWidth(true);
        grid.addColumn(ComputerEntry::getOperatingSystem).setHeader(message("search.operating_system")).setAutoWidth(true);
        grid.addColumn(ComputerEntry::getDnsHostName).setHeader(message("search.host")).setAutoWidth(true);
        grid.setItems(entries);
        grid.addItemClickListener(event -> navigate("management/computers", event.getItem().getCn()));
        addResult(message("search.computers"), grid);
    }

    private void addGroups(String value) {
        List<GroupEntry> entries = safe(groupsService.getAll(
                "(|(cn=*" + value + "*)(description=*" + value + "*))", "cn", "description"));
        if (entries.isEmpty()) return;
        Grid<GroupEntry> grid = new Grid<>();
        grid.addColumn(GroupEntry::getCn).setHeader(message("search.name")).setAutoWidth(true);
        grid.addColumn(GroupEntry::getDescription).setHeader(message("search.description")).setAutoWidth(true);
        grid.setItems(entries);
        grid.addItemClickListener(event -> navigate("management/groups", event.getItem().getCn()));
        addResult(message("search.groups"), grid);
    }

    private void addContacts(String value) {
        List<ContactEntry> entries = safe(contactsService.getAll(
                "(|(cn=*" + value + "*)(displayName=*" + value + "*)(mail=*" + value + "*)(company=*" + value + "*))",
                "cn", "displayName", "mail", "company"));
        if (entries.isEmpty()) return;
        Grid<ContactEntry> grid = new Grid<>();
        grid.addColumn(ContactEntry::getCn).setHeader(message("search.name")).setAutoWidth(true);
        grid.addColumn(ContactEntry::getDisplayName).setHeader(message("search.display_name")).setAutoWidth(true);
        grid.addColumn(ContactEntry::getEmailAddress).setHeader(message("search.email")).setAutoWidth(true);
        grid.setItems(entries);
        grid.addItemClickListener(event -> navigate("management/contacts", event.getItem().getCn()));
        addResult(message("search.contacts"), grid);
    }

    private void addPrinters(String value) {
        List<PrinterEntry> entries = safe(printersService.getAll(
                "(|(cn=*" + value + "*)(printerName=*" + value + "*)(serverName=*" + value + "*))",
                "cn", "printerName", "serverName"));
        if (entries.isEmpty()) return;
        Grid<PrinterEntry> grid = new Grid<>();
        grid.addColumn(PrinterEntry::getCn).setHeader(message("search.name")).setAutoWidth(true);
        grid.addColumn(PrinterEntry::getPrinterName).setHeader(message("search.printer")).setAutoWidth(true);
        grid.addColumn(PrinterEntry::getServerName).setHeader(message("search.server")).setAutoWidth(true);
        grid.setItems(entries);
        grid.addItemClickListener(event -> navigate("management/printers", event.getItem().getCn()));
        addResult(message("search.printers"), grid);
    }

    private void addResult(String heading, Grid<?> grid) {
        results.add(new H4(heading), grid);
        grid.setWidthFull();
        grid.setMaxHeight("260px");
    }

    private void navigate(String base, String cn) {
        if (cn != null && !cn.isBlank()) {
            UI.getCurrent().navigate(base + "/" + URLEncoder.encode(cn, StandardCharsets.UTF_8).replace("+", "%20") + "/details");
        }
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static String escapeFilter(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (char character : value.toCharArray()) {
            switch (character) {
                case '\\' -> result.append("\\5c");
                case '*' -> result.append("\\2a");
                case '(' -> result.append("\\28");
                case ')' -> result.append("\\29");
                case '\0' -> result.append("\\00");
                default -> result.append(character);
            }
        }
        return result.toString();
    }

    @Override
    public String getPageTitle() {
        return message("search.title");
    }
}
