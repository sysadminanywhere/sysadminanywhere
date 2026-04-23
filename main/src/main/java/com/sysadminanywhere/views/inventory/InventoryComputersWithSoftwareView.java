package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
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
@Route(value = "inventory/software/:id?/computer")
@Uses(Icon.class)
public class InventoryComputersWithSoftwareView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private Long id = 0L;

    private Grid<ComputerItem> grid;

    private final Filters filters;
    private final InventoryService inventoryService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").
                orElse(null);

        if(idParam != null && !idParam.isEmpty())
            id = Long.valueOf(idParam);
    }

    public InventoryComputersWithSoftwareView(InventoryService inventoryService, MessageSource messageSource, LocaleService localeService) {
        this.inventoryService = inventoryService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), messageSource, localeService);
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

        private final TextField name;
        private final MessageSource messageSource;
        private final LocaleService localeService;

        public Filters(Runnable onSearch, MessageSource messageSource, LocaleService localeService) {
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.name = new TextField("Name");

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button(getMessage("common.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();
                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(name, actions);
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        public Map<String, String> getFilters() {
            Map<String, String> filters = new HashMap<>();
            filters.put("name", name.getValue());
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(ComputerItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn("name").setAutoWidth(true);

        grid.setItems(query -> inventoryService.getComputersWithSoftware(id,
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    public String getPageTitle() {
        return getMessage("inventory_computers_with_software_view.title");
    }

}
