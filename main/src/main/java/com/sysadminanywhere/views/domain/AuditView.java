package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.model.AuditItem;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@PageTitle("Audit")
@Route(value = "domain/audit")
@PermitAll
public class AuditView extends Div {

    private String id;

    private Grid<AuditItem> grid;

    private AuditView.Filters filters;
    private final LdapService ldapService;

    public AuditView(LdapService ldapService) {
        this.ldapService = ldapService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new AuditView.Filters(() -> refreshGrid(), ldapService);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
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

        private final LdapService ldapService;

        private final TextField name = new TextField("Name");
        private final ComboBox<String> action = new ComboBox<>("Action");
        private final DatePicker startDate = new DatePicker("Date");
        private final DatePicker endDate = new DatePicker();

        public Filters(Runnable onSearch, LdapService ldapService) {
            this.ldapService = ldapService;

            action.setItems("All", "Changed", "Created");
            action.setValue("All");

            startDate.setValue(LocalDate.now());

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();

                action.clear();
                action.setValue("All");

                startDate.clear();
                startDate.setValue(LocalDate.now());

                endDate.clear();

                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(createDateRangeFilter(), actions);
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");
            endDate.setPlaceholder("To");

            startDate.setAriaLabel("From date");
            endDate.setAriaLabel("To date");

            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        public Map<String, Object> getFilters() {
            Map<String, Object> filters = new HashMap<>();
            filters.put("name", name.getValue());
            filters.put("action", action.getValue());
            filters.put("startDate", startDate.getValue());
            filters.put("endDate", endDate.getValue());
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(AuditItem.class, false);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("distinguishedName").setAutoWidth(true);
        grid.addColumn("action").setAutoWidth(true);
        grid.addColumn("whenChanged").setAutoWidth(true);

        try {
            grid.setItems(query -> ldapService.getAudit(
                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                    filters.getFilters()).stream());
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}