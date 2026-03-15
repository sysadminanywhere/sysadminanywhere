package com.sysadminanywhere.views.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.service.IncidentService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@PageTitle("incidents")
@Route("incidents")
public class IncidentsView extends Div {

    private Grid<IncidentItem> grid;

    private IncidentsView.Filters filters;
    private final IncidentService incidentService;

    public IncidentsView(IncidentService incidentService) {
        this.incidentService = incidentService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        if (!incidentService.ping()) {
            Notification notification = Notification.show("Incidents service is unavailable!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            filters = new IncidentsView.Filters(() -> refreshGrid());
            VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
            layout.setSizeFull();
            layout.setPadding(false);
            layout.setSpacing(false);
            add(layout);
        }
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
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

        private final ComboBox<String> severity = new ComboBox<>("Severity");
        private final ComboBox<String> status = new ComboBox<>("Status");

        public Filters(Runnable onSearch) {

            severity.setItems("All", "Low", "Medium", "High", "Critical");
            severity.setValue("All");

            status.setItems("All", "Open", "In Progress", "Resolved", "False Positive", "Closed");
            status.setValue("Open");

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                severity.clear();
                severity.setValue("Critical");

                status.clear();
                status.setValue("Open");

                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(severity, status, actions);
        }

        public Map<String, Object> getFilters() {
            Map<String, Object> filters = new HashMap<>();
            filters.put("severity", severity.getValue().toUpperCase());
            filters.put("status", status.getValue().toUpperCase().replace(" ", "_"));
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(IncidentItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn("createdAt").setAutoWidth(true);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("machineName").setAutoWidth(true);
        grid.addColumn("severity").setAutoWidth(true);
        grid.addColumn("status").setAutoWidth(true);
        grid.addColumn("recommendation").setAutoWidth(true);

        grid.addItemClickListener(item -> {
                incidentDialog(incidentService, item.getItem(), this::refreshGrid).open();
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

    private Dialog incidentDialog(IncidentService incidentService, IncidentItem incidentItem, Runnable onSearch) {
        return new IncidentDialog(incidentService, incidentItem, onSearch);
    }

}