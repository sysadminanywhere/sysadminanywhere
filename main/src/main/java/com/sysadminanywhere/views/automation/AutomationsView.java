package com.sysadminanywhere.views.automation;

import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.workflow.Workflow;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.WorkflowsService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

@RolesAllowed("ADMIN")
@PageTitle("automations_view.title")
@Route(value = "automation/workflows")
@Uses(Icon.class)
public class AutomationsView extends Div implements MenuControl {

    private Grid<Workflow> grid;

    private Filters filters;
    private final WorkflowsService workflowsService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public AutomationsView(WorkflowsService workflowsService, MessageSource messageSource, LocaleService localeService) {
        this.workflowsService = workflowsService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        if (!workflowsService.ping()) {
            Notification notification = Notification.show(getMessage("common.error") + ": N8n service is unavailable!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            filters = new Filters(() -> refreshGrid(), workflowsService, messageSource, localeService);
            VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
            layout.setSizeFull();
            layout.setPadding(false);
            layout.setSpacing(false);

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

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();

        MenuHelper.createIconItem(menuBar, "/icons/refresh.svg", menuItemClickEvent -> {
            refreshGrid();
        });

        MenuHelper.createIconItem(menuBar, "/icons/plus.svg", getMessage("common.new"), event -> {
            grid.getUI().ifPresent(ui ->
                    ui.getPage().open("http://localhost:5678/workflow/new", "_blank"));
        });

        return menuBar;
    }

    public static class Filters extends Div {

        private final WorkflowsService workflowsService;
        private final MessageSource messageSource;
        private final LocaleService localeService;

        private final TextField cn;
        private final ComboBox<String> availability;

        public Filters(Runnable onSearch, WorkflowsService workflowsService, MessageSource messageSource, LocaleService localeService) {
            this.workflowsService = workflowsService;
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.cn = new TextField("Name");
            this.availability = new ComboBox<>(getMessage("common.filters"));

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button(getMessage("common.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                cn.clear();
                availability.setValue(getMessage("common.all"));
                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            availability.setItems("All", "Active", "Inactive");
            availability.setValue("All");

            add(cn, availability, actions);
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        public String getFilters() {
            String searchFilters = "";
            return searchFilters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(Workflow.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn("id").setAutoWidth(true);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("automation/workflows/" + item.getItem().getId() + "/details"));
        });

        grid.setItems(query -> workflowsService.getAll(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
