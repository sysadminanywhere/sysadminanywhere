package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.common.directory.dto.OrganizationEmployee;
import com.sysadminanywhere.domain.OrganizationChart;
import com.sysadminanywhere.domain.OrganizationChart.Filters;
import com.sysadminanywhere.domain.OrganizationChart.Selection;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static com.sysadminanywhere.domain.OrganizationChart.name;
import static com.sysadminanywhere.domain.OrganizationChart.text;

@Route("management/organization")
@RouteAlias("management/users/organization")
@RolesAllowed({"ADMIN", "READER"})
public class OrganizationView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {
    private final UsersService usersService;
    private final MessageSource messages;
    private final LocaleService locales;
    private final TextField search = new TextField();
    private final ComboBox<String> country = new ComboBox<>();
    private final ComboBox<OfficeChoice> office = new ComboBox<>();
    private final ComboBox<String> department = new ComboBox<>();
    private final ComboBox<OrganizationEmployee> manager = new ComboBox<>();
    private final Checkbox includeDisabled = new Checkbox();
    private final TreeGrid<Row> tree = new TreeGrid<>();
    private final Span status = new Span();
    private final Tab reportingTab;
    private final Tabs modes;
    private OrganizationChart chart = new OrganizationChart(List.of());
    private boolean updatingFilters;
    private boolean directoryUnavailable;
    private OrganizationEmployee focus;

    public OrganizationView(UsersService usersService, MessageSource messages, LocaleService locales) {
        this.usersService = usersService;
        this.messages = messages;
        this.locales = locales;
        reportingTab = new Tab(message("reporting"));
        modes = new Tabs(reportingTab, new Tab(message("locations")));
        modes.getElement().setAttribute("aria-label", message("view_mode"));
        modes.addSelectedChangeListener(event -> render());

        search.setLabel(message("search"));
        search.setId("organization-search");
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setClearButtonVisible(true);
        search.addValueChangeListener(event -> render());
        configure(country, "country");
        configure(office, "office");
        configure(department, "department");
        configure(manager, "manager");
        country.setItemLabelGenerator(this::countryName);
        office.setItemLabelGenerator(value -> {
            String officeName = label(value.name(), "unknown_office");
            return country.getValue() == null ? officeName + " · " + countryName(value.country()) : officeName;
        });
        department.setItemLabelGenerator(value -> label(value, "unknown_department"));
        manager.setItemLabelGenerator(this::employeeLabel);
        country.addValueChangeListener(event -> {
            if (!updatingFilters) {
                updatingFilters = true;
                updateOffices();
                updatingFilters = false;
                render();
            }
        });
        includeDisabled.setLabel(message("include_disabled"));
        includeDisabled.addValueChangeListener(event -> render());
        status.getElement().setAttribute("role", "status");

        HorizontalLayout filters = new HorizontalLayout(search, country, office, department, manager, includeDisabled,
                new Button(messages.getMessage("common.refresh", null, locales.getCurrentLocale()), event -> reload()));
        filters.setWidthFull();
        filters.setAlignItems(Alignment.BASELINE);
        filters.getStyle().set("flex-wrap", "wrap");
        configureTree();
        setSizeFull();
        add(modes, filters, status, tree);
        expand(tree);
        reload();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getPath().equals("management/users/organization")) {
            event.forwardTo(OrganizationView.class, event.getLocation().getQueryParameters());
        }
    }

    private void configure(ComboBox<?> field, String key) {
        field.setLabel(message(key));
        field.setClearButtonVisible(true);
        field.setWidth("12rem");
        field.setMaxWidth("100%");
        if (field != country) {
            field.addValueChangeListener(event -> render());
        }
    }

    private void configureTree() {
        tree.setId("organization-tree");
        tree.setSizeFull();
        tree.getElement().setAttribute("aria-label", message("title"));
        tree.addHierarchyColumn(Row::label).setHeader(message("employee")).setFrozen(true).setWidth("19rem");
        tree.addColumn(row -> field(row, OrganizationEmployee::title)).setHeader(message("job_title")).setAutoWidth(true);
        tree.addColumn(row -> field(row, OrganizationEmployee::department)).setHeader(message("department")).setAutoWidth(true);
        tree.addColumn(row -> row.employee() == null ? "" : countryName(row.employee().country()))
                .setHeader(message("country")).setAutoWidth(true);
        tree.addColumn(row -> row.employee() == null ? "" : label(row.employee().office(), "unknown_office"))
                .setHeader(message("office")).setAutoWidth(true);
        tree.addColumn(this::rowStatus).setHeader(message("status")).setAutoWidth(true);
        tree.asSingleSelect().addValueChangeListener(event -> {
            Row row = event.getValue();
            if (event.isFromClient() && row != null && row.employee() != null) {
                tree.deselect(row);
                showDetails(row.employee());
            }
        });
    }

    private void reload() {
        try {
            chart = new OrganizationChart(usersService.getOrganization());
        } catch (RuntimeException exception) {
            directoryUnavailable = true;
            status.setText(messages.getMessage("directory.unavailable", null, locales.getCurrentLocale()));
            return;
        }
        directoryUnavailable = false;
        updatingFilters = true;
        replaceOptions(country, values(chart.employees(), OrganizationEmployee::country));
        updateOffices();
        replaceOptions(department, values(chart.employees(), OrganizationEmployee::department));
        replaceOptions(manager, chart.employees().stream().filter(employee -> !chart.reportsOf(employee).isEmpty()).toList());
        updatingFilters = false;
        render();
    }

    private void updateOffices() {
        Set<OfficeChoice> choices = new TreeSet<>(Comparator
                .comparing(OfficeChoice::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(OfficeChoice::country, String.CASE_INSENSITIVE_ORDER));
        chart.employees().stream()
                .filter(employee -> country.getValue() == null || country.getValue().equalsIgnoreCase(text(employee.country())))
                .map(employee -> new OfficeChoice(text(employee.country()), text(employee.office())))
                .forEach(choices::add);
        replaceOptions(office, choices);
    }

    private static Set<String> values(List<OrganizationEmployee> employees, Function<OrganizationEmployee, String> attribute) {
        Set<String> values = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        employees.forEach(employee -> values.add(text(attribute.apply(employee))));
        return values;
    }

    private static <T> void replaceOptions(ComboBox<T> field, Collection<T> options) {
        T previous = field.getValue();
        field.setItems(options);
        if (previous != null && options.contains(previous)) {
            field.setValue(previous);
        } else {
            field.clear();
        }
    }

    private void render() {
        if (updatingFilters) {
            return;
        }
        boolean filtered = !text(search.getValue()).isEmpty() || country.getValue() != null
                || office.getValue() != null || department.getValue() != null || manager.getValue() != null;
        OfficeChoice selectedOffice = office.getValue();
        String selectedCountry = selectedOffice == null ? country.getValue() : selectedOffice.country();
        Filters filters = new Filters(search.getValue(), selectedCountry,
                selectedOffice == null ? null : selectedOffice.name(), department.getValue(),
                manager.getValue(), includeDisabled.getValue());
        Selection selection = chart.select(filters);
        TreeData<Row> data = new TreeData<>();
        Set<Row> expand = new LinkedHashSet<>();
        Row focused = modes.getSelectedTab() == reportingTab
                ? buildReporting(data, selection, expand, filtered)
                : buildLocations(data, selection.matches(), expand, filtered);
        tree.setDataProvider(new TreeDataProvider<>(data));
        tree.expand(expand);
        if (focused != null) {
            tree.scrollToItem(focused);
        }
        focus = null;
        status.setText(directoryUnavailable
                ? messages.getMessage("directory.unavailable", null, locales.getCurrentLocale())
                : message("count", selection.matches().size()));
    }

    private Row buildReporting(TreeData<Row> data, Selection selection, Set<Row> expand, boolean filtered) {
        Set<OrganizationEmployee> matches = Set.copyOf(selection.matches());
        Map<OrganizationEmployee, Row> rows = new HashMap<>();
        ArrayDeque<OrganizationEmployee> queue = new ArrayDeque<>(chart.roots());
        while (!queue.isEmpty()) {
            OrganizationEmployee employee = queue.removeFirst();
            if (!selection.withManagers().contains(employee)) {
                continue;
            }
            Row parent = rows.get(chart.managerOf(employee));
            Row row = new Row(name(employee), employee, !matches.contains(employee), null);
            rows.put(employee, row);
            data.addItem(parent, row);
            if (filtered && parent != null) {
                expand.add(parent);
            }
            queue.addAll(chart.reportsOf(employee));
        }
        ArrayDeque<Row> focusPath = new ArrayDeque<>();
        for (OrganizationEmployee ancestor = chart.managerOf(focus); rows.containsKey(ancestor);
             ancestor = chart.managerOf(ancestor)) {
            focusPath.addFirst(rows.get(ancestor));
        }
        expand.addAll(focusPath);
        return rows.get(focus);
    }

    private Row buildLocations(TreeData<Row> data, List<OrganizationEmployee> employees, Set<Row> expand, boolean filtered) {
        Map<String, Row> countries = new LinkedHashMap<>();
        Map<Row, Map<String, Row>> offices = new HashMap<>();
        var ordered = employees.stream()
                .sorted(Comparator.comparing((OrganizationEmployee employee) -> key(employee.country()))
                        .thenComparing(employee -> key(employee.office())))
                .toList();
        for (OrganizationEmployee employee : ordered) {
            String countryKey = key(employee.country());
            Row countryRow = countries.computeIfAbsent(countryKey, ignored -> {
                Row row = new Row(countryName(employee.country()), null, false, countryKey);
                data.addItem(null, row);
                return row;
            });
            String officeKey = key(employee.office());
            Row officeRow = offices.computeIfAbsent(countryRow, ignored -> new HashMap<>())
                    .computeIfAbsent(officeKey, ignored -> {
                        Row row = new Row(label(employee.office(), "unknown_office"), null, false, List.of(countryKey, officeKey));
                        data.addItem(countryRow, row);
                        return row;
                    });
            data.addItem(officeRow, new Row(name(employee), employee, false, null));
            if (filtered) {
                expand.add(countryRow);
                expand.add(officeRow);
            }
        }
        return null;
    }

    private void showDetails(OrganizationEmployee employee) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(name(employee));
        dialog.setWidth("min(36rem, 95vw)");
        OrganizationEmployee supervisor = chart.managerOf(employee);
        VerticalLayout details = new VerticalLayout(
                detail("login", employee.login()), detail("job_title", employee.title()),
                detail("department", label(employee.department(), "unknown_department")),
                detail("country", countryName(employee.country())),
                detail("office", label(employee.office(), "unknown_office")),
                detail("manager", supervisor == null ? managerStatus(employee) : employeeLabel(supervisor)),
                detail("direct_reports", String.valueOf(chart.reportsOf(employee).size())));
        chart.reportsOf(employee).forEach(report -> details.add(new Span(employeeLabel(report))));
        dialog.add(details);
        if (modes.getSelectedTab() != reportingTab) {
            dialog.getFooter().add(new Button(message("show_reporting"), event -> {
                dialog.close();
                focus = employee;
                modes.setSelectedTab(reportingTab);
            }));
        }
        if (canOpenUser(employee)) {
            RouterLink link = new RouterLink(message("open_user"), UserDetailsView.class,
                    new RouteParameters("id", employee.cn()));
            dialog.getFooter().add(link);
        }
        dialog.getFooter().add(new Button(messages.getMessage("common.close", null, locales.getCurrentLocale()),
                event -> dialog.close()));
        add(dialog);
        dialog.addClosedListener(event -> remove(dialog));
        dialog.open();
    }

    private boolean canOpenUser(OrganizationEmployee employee) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
                && !text(employee.cn()).isEmpty()
                && chart.employees().stream().filter(other -> text(employee.cn()).equalsIgnoreCase(text(other.cn())))
                .limit(2).count() == 1;
    }

    private Div detail(String key, String value) {
        Span label = new Span(message(key));
        label.getStyle().set("font-weight", "600");
        return new Div(label, new Div(text(value).isEmpty() ? "—" : value));
    }

    private String rowStatus(Row row) {
        if (row.employee() == null) {
            return "";
        }
        String issue = chart.issueOf(row.employee()) == null ? "" : managerStatus(row.employee());
        String disabled = row.employee().disabled() ? message("disabled") : "";
        String context = row.context() ? message("context") : "";
        return String.join(" · ", List.of(issue, disabled, context).stream().filter(value -> !value.isEmpty()).toList());
    }

    private String managerStatus(OrganizationEmployee employee) {
        var issue = chart.issueOf(employee);
        if (issue == null) {
            return message("no_manager");
        }
        return message(issue == OrganizationChart.ReportingIssue.CYCLE ? "cycle" : "missing_manager");
    }

    private String employeeLabel(OrganizationEmployee employee) {
        return text(employee.login()).isEmpty() ? name(employee) : name(employee) + " (" + employee.login() + ")";
    }

    private String countryName(String value) {
        String code = text(value).toUpperCase(Locale.ROOT);
        if (code.matches("[A-Z]{2}")) {
            return new Locale.Builder().setRegion(code).build().getDisplayCountry(locales.getCurrentLocale());
        }
        return label(value, "unknown_country");
    }

    private String label(String value, String fallback) {
        return text(value).isEmpty() ? message(fallback) : text(value);
    }

    private static String field(Row row, Function<OrganizationEmployee, String> attribute) {
        return row.employee() == null ? "" : text(attribute.apply(row.employee()));
    }

    private static String key(String value) {
        return text(value).toLowerCase(Locale.ROOT);
    }

    private String message(String key, Object... arguments) {
        return messages.getMessage("organization." + key, arguments, locales.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return message("title");
    }

    private record Row(String label, OrganizationEmployee employee, boolean context, Object locationKey) {
    }

    private record OfficeChoice(String country, String name) {
    }
}
