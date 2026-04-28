package com.sysadminanywhere.views.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.service.IncidentService;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.TicketService;
import com.sysadminanywhere.service.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route("incidents")
public class IncidentsView extends Div implements HasDynamicTitle {

    private Grid<IncidentItem> grid;

    private IncidentsView.Filters filters;
    private final IncidentService incidentService;
    private final TicketService ticketService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public IncidentsView(IncidentService incidentService, TicketService ticketService, MessageSource messageSource, LocaleService localeService) {
        this.incidentService = incidentService;
        this.ticketService = ticketService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        if (!incidentService.ping()) {
            Notification notification = Notification.show(getMessage("common.error") + ": " + getMessage("incidents_view.service_unavailable"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            filters = new IncidentsView.Filters(() -> refreshGrid(), messageSource, localeService);
            VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
            layout.setSizeFull();
            add(layout);
        }
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span(getMessage("common.filters"));
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div {

        private final ComboBox<String> severity;
        private final ComboBox<String> status;
        private final MessageSource messageSource;
        private final LocaleService localeService;

        public Filters(Runnable onSearch, MessageSource messageSource, LocaleService localeService) {
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.severity = new ComboBox<>(getMessage("incidents_view.severity"));
            this.status = new ComboBox<>(getMessage("incidents_view.status"));

            severity.setItems(getMessage("incidents_view.all"), getMessage("incidents_view.low"), getMessage("incidents_view.medium"), getMessage("incidents_view.high"), getMessage("incidents_view.critical"));
            severity.setValue(getMessage("incidents_view.all"));

            status.setItems(getMessage("incidents_view.all"), getMessage("incidents_view.open"), getMessage("incidents_view.in_progress"), getMessage("incidents_view.resolved"), getMessage("incidents_view.false_positive"), getMessage("incidents_view.closed"));
            status.setValue(getMessage("incidents_view.open"));

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button(getMessage("common.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                severity.clear();
                severity.setValue(getMessage("incidents_view.critical"));

                status.clear();
                status.setValue(getMessage("incidents_view.open"));

                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(severity, status, actions);
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        private Map<String, String> getSeverityReverseMapping() {
            Map<String, String> mapping = new HashMap<>();
            mapping.put(getMessage("incidents_view.all"), "ALL");
            mapping.put(getMessage("incidents_view.low"), "LOW");
            mapping.put(getMessage("incidents_view.medium"), "MEDIUM");
            mapping.put(getMessage("incidents_view.high"), "HIGH");
            mapping.put(getMessage("incidents_view.critical"), "CRITICAL");
            return mapping;
        }

        private Map<String, String> getStatusReverseMapping() {
            Map<String, String> mapping = new HashMap<>();
            mapping.put(getMessage("incidents_view.all"), "ALL");
            mapping.put(getMessage("incidents_view.open"), "OPEN");
            mapping.put(getMessage("incidents_view.in_progress"), "IN_PROGRESS");
            mapping.put(getMessage("incidents_view.resolved"), "RESOLVED");
            mapping.put(getMessage("incidents_view.false_positive"), "FALSE_POSITIVE");
            mapping.put(getMessage("incidents_view.closed"), "CLOSED");
            return mapping;
        }

        public Map<String, Object> getFilters() {
            Map<String, Object> filters = new HashMap<>();
            String severityValue = severity.getValue();
            String statusValue = status.getValue();

            filters.put("severity", getSeverityReverseMapping().getOrDefault(severityValue, severityValue.toUpperCase()));
            filters.put("status", getStatusReverseMapping().getOrDefault(statusValue, statusValue.toUpperCase().replace(" ", "_")));
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(IncidentItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(incidentItem ->
                        Utils.formatLocalDateTime(incidentItem.getCreatedAt()))
                .setHeader(getMessage("common.created_at")).setAutoWidth(true);

        grid.addColumn("name").setHeader(getMessage("incidents_view.name")).setAutoWidth(true);
        grid.addColumn("machineName").setHeader(getMessage("incidents_view.machine_name")).setAutoWidth(true);
        grid.addColumn("severity").setHeader(getMessage("incidents_view.severity")).setAutoWidth(true);
        grid.addColumn("status").setHeader(getMessage("incidents_view.status")).setAutoWidth(true);
        grid.addColumn("recommendation").setHeader(getMessage("incidents_view.recommendation")).setAutoWidth(true);

        grid.addItemClickListener(item -> {
                incidentDialog(incidentService, ticketService, item.getItem(), this::refreshGrid).open();
        });

        grid.setItems(query -> incidentService.getIncidents(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Dialog incidentDialog(IncidentService incidentService, TicketService ticketService, IncidentItem incidentItem, Runnable onSearch) {
        return new IncidentDialog(incidentService, ticketService, incidentItem, messageSource, localeService, onSearch);
    }

    public String getPageTitle() {
        return getMessage("incidents_view.title");
    }

}
