package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.control.AiSearchField;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.service.AiAssistantService;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

@RolesAllowed("ADMIN")
@Route(value = "management/computers")
@Uses(Icon.class)
public class ComputersView extends Div implements MenuControl, HasDynamicTitle {

    private Grid<ComputerEntry> grid;

    private final Filters filters;
    private final ComputersService computersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final AiAssistantService aiAssistantService;

    public ComputersView(ComputersService computersService, MessageSource messageSource, LocaleService localeService, AiAssistantService aiAssistantService) {
        this.computersService = computersService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        this.aiAssistantService = aiAssistantService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), computersService, messageSource, localeService, aiAssistantService);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();

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

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();

        MenuHelper.createIconItem(menuBar,"/icons/refresh.svg", menuItemClickEvent -> {
            refreshGrid();
        });

        MenuHelper.createIconItem(menuBar, "/icons/plus.svg", getMessage("common.new"), event -> {
            addDialog(this::refreshGrid).open();
        });

        return menuBar;
    }

    private Dialog addDialog(Runnable onSearch) {
        return new AddComputerDialog(computersService, messageSource, localeService, onSearch);
    }

    public static class Filters extends Div {

        private final ComputersService computersService;
        private final MessageSource messageSource;
        private final LocaleService localeService;
        private final AiAssistantService aiAssistantService;

        private final TextField cn;
        private final ComboBox<String> availability;
        private final AiSearchField aiSearchField;

        public Filters(Runnable onSearch, ComputersService computersService, MessageSource messageSource, LocaleService localeService, AiAssistantService aiAssistantService) {
            this.computersService = computersService;
            this.messageSource = messageSource;
            this.localeService = localeService;
            this.aiAssistantService = aiAssistantService;

            this.cn = new TextField(getMessage("common.cn"));
            this.availability = new ComboBox<>(getMessage("common.filters"));
            this.aiSearchField = new AiSearchField(aiAssistantService, translation -> {
                // Apply AI translation to filters
                if (translation.getLdapFilter() != null) {
                    cn.clear();
                    availability.setValue(getMessage("common.all"));
                    // Store the AI filter for use in getFilters()
                    setAiFilter(translation.getLdapFilter());
                    onSearch.run();
                }
            }, "computer");

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
                aiSearchField.clear();
                setAiFilter(null);
                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            availability.setItems(getMessage("common.all"), getMessage("common.disabled"));
            availability.setValue(getMessage("common.all"));

            add(cn, availability, aiSearchField, actions);
        }

        private String aiFilter;

        private void setAiFilter(String filter) {
            this.aiFilter = filter;
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        public String getFilters() {
            String searchFilters = "";

            // Use AI filter if available
            if (aiFilter != null && !aiFilter.isBlank()) {
                return aiFilter;
            }

            if (!cn.isEmpty()) {
                searchFilters += "(cn=" + cn.getValue() + "*)";
            }
            if (!availability.isEmpty()) {
                if (availability.getValue().equalsIgnoreCase(getMessage("common.disabled")))
                    searchFilters += "(userAccountControl:1.2.840.113556.1.4.803:=2)";
            }

            return searchFilters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(ComputerEntry.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(new ComponentRenderer<>(computer -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);

            SvgIcon icon = new SvgIcon("icons/computer.svg");
            icon.setColor("grey");

            Span text = new Span(computer.getCn());

            layout.add(icon, text);
            layout.setSpacing(true);
            return layout;
        })).setHeader(getMessage("common.cn")).setAutoWidth(true);

        grid.addColumn("description").setHeader(getMessage("common.description")).setAutoWidth(true);

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("management/computers/" + item.getItem().getCn() + "/details"));
        });

        grid.setItems(query -> computersService.getAll(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters(), "cn", "description").stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    public String getPageTitle() {
        return getMessage("computers_view.title");
    }

}
