package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.LocaleService;
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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route(value = "domain/audit")
public class AuditView extends Div implements HasDynamicTitle {

    private String id;

    private Grid<AuditDto> grid;

    private final AuditView.Filters filters;
    private final LdapService ldapService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public AuditView(LdapService ldapService, MessageSource messageSource, LocaleService localeService) {
        this.ldapService = ldapService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new AuditView.Filters(() -> refreshGrid(), ldapService, messageSource, localeService);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
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

        private final LdapService ldapService;
        private final MessageSource messageSource;
        private final LocaleService localeService;

        private final TextField name;
        private final ComboBox<String> action;
        private final DatePicker startDate;
        private final DatePicker endDate;

        public Filters(Runnable onSearch, LdapService ldapService, MessageSource messageSource, LocaleService localeService) {
            this.ldapService = ldapService;
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.name = new TextField(getMessage("audit_view.name"));
            this.action = new ComboBox<>(getMessage("audit_view.action"));
            this.startDate = new DatePicker(getMessage("audit_view.date"));
            this.endDate = new DatePicker();

            action.setItems(getMessage("common.all"), getMessage("audit_view.changed"), getMessage("audit_view.created"));
            action.setValue(getMessage("common.all"));

            startDate.setValue(LocalDate.now());

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button(getMessage("common.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();

                action.clear();
                action.setValue(getMessage("common.all"));

                startDate.clear();
                startDate.setValue(LocalDate.now());

                endDate.clear();

                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(createDateRangeFilter(), actions);
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder(getMessage("common.from"));
            endDate.setPlaceholder(getMessage("common.to"));

            startDate.setAriaLabel(getMessage("common.from_date"));
            endDate.setAriaLabel(getMessage("common.to"));

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
        grid = new Grid<>(AuditDto.class, false);
        grid.addColumn("name").setHeader(getMessage("audit_view.name")).setAutoWidth(true);
        grid.addColumn("distinguishedName").setHeader(getMessage("audit_view.distinguished_name")).setAutoWidth(true);
        grid.addColumn("action").setHeader(getMessage("audit_view.action")).setAutoWidth(true);
        grid.addColumn("whenChanged").setHeader(getMessage("audit_view.when_changed")).setAutoWidth(true);

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

    public String getPageTitle() {
        return getMessage("audit_view.title");
    }

}
