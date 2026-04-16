package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.wmi.EventEntity;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route(value = "management/computers/:id?/events")
@Uses(Icon.class)
@Uses(DatePicker.class)
@Uses(TextArea.class)
public class ComputerEventsView extends Div implements BeforeEnterObserver, MenuControl, HasDynamicTitle {

    private String id;

    private Grid<EventEntity> grid;

    private final Filters filters;
    private final ComputersService computersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerEventsView(ComputersService computersService, MessageSource messageSource, LocaleService localeService) {
        this.computersService = computersService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), computersService, messageSource, localeService);
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
        Span filtersHeading = new Span(getMessage("computer_events_view.filters"));
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

        private final ComputersService computersService;
        private final MessageSource messageSource;
        private final LocaleService localeService;
        private final java.util.Map<String, String> translationToEnglishMap;

        private final TextField sourceName;
        private final ComboBox<String> eventType;
        private final DatePicker datePicker;

        public Filters(Runnable onSearch, ComputersService computersService, MessageSource messageSource, LocaleService localeService) {
            this.computersService = computersService;
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.translationToEnglishMap = new java.util.HashMap<>();
            translationToEnglishMap.put(getMessage("computer_events_view.error"), "Error");
            translationToEnglishMap.put(getMessage("computer_events_view.warning"), "Warning");
            translationToEnglishMap.put(getMessage("computer_events_view.information"), "Information");
            translationToEnglishMap.put(getMessage("computer_events_view.audit_success"), "Audit Success");
            translationToEnglishMap.put(getMessage("computer_events_view.audit_failure"), "Audit Failure");
            translationToEnglishMap.put(getMessage("computer_events_view.all"), "All");

            this.sourceName = new TextField(getMessage("computer_events_view.source_name"));
            this.eventType = new ComboBox<>(getMessage("computer_events_view.event_type"));
            this.datePicker = new DatePicker(getMessage("computer_events_view.date"));

            eventType.setItems(getMessage("computer_events_view.error"), getMessage("computer_events_view.warning"), getMessage("computer_events_view.information"), getMessage("computer_events_view.audit_success"), getMessage("computer_events_view.audit_failure"), getMessage("computer_events_view.all"));
            eventType.setValue(getMessage("computer_events_view.error"));

            datePicker.setValue(LocalDate.now());

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button(getMessage("computer_events_view.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                sourceName.clear();

                eventType.clear();
                eventType.setValue(getMessage("computer_events_view.error"));

                datePicker.clear();
                datePicker.setValue(LocalDate.now());

                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("computer_events_view.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(sourceName, eventType, datePicker, actions);
        }

        public Map<String, String> getFilters() {
            Map<String, String> filters = new HashMap<>();
            filters.put("sourceName", sourceName.getValue());
            String selectedEventType = eventType.getValue();
            String englishEventType = translationToEnglishMap.getOrDefault(selectedEventType, selectedEventType);
            filters.put("eventType", englishEventType);
            filters.put("date", datePicker.getValue().toString());
            return filters;
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

    }

    private Component createGrid() {
        grid = new Grid<>(EventEntity.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn("sourceName").setHeader(getMessage("computer_events_view.source_name")).setAutoWidth(true);
        grid.addColumn("type").setHeader(getMessage("computer_events_view.event_type")).setAutoWidth(true);
        grid.addColumn("timeGenerated").setHeader(getMessage("computer_events_view.time_generated")).setAutoWidth(true);
        grid.addColumn("logfile").setHeader(getMessage("computer_events_view.log_file")).setAutoWidth(true);

        grid.addItemClickListener(item -> {
            showDialog(item.getItem()).open();
        });

        grid.setItems(query -> computersService.getEvents(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters(), id).stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Dialog showDialog(EventEntity event) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle(getMessage("computer_events_view.dialog_title"));
        dialog.setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtSourceName = new TextField(getMessage("computer_events_view.source_name"));
        txtSourceName.setReadOnly(true);
        txtSourceName.setValue(event.getSourceName());

        TextField txtEventType = new TextField(getMessage("computer_events_view.event_type"));
        txtEventType.setReadOnly(true);
        txtEventType.setValue(event.getType());

        TextField txtTimeGenerated = new TextField(getMessage("computer_events_view.time_generated"));
        txtTimeGenerated.setReadOnly(true);
        txtTimeGenerated.setValue(event.getTimeGenerated());

        TextField txtLogfile = new TextField(getMessage("computer_events_view.log_file"));
        txtLogfile.setReadOnly(true);
        txtLogfile.setValue(event.getLogfile());

        TextArea txtMessage = new TextArea(getMessage("computer_events_view.message"));
        txtMessage.setReadOnly(true);
        txtMessage.setValue(event.getMessage());
        txtMessage.setMinHeight("100px");
        formLayout.setColspan(txtMessage, 2);

        formLayout.add(txtSourceName, txtEventType, txtTimeGenerated, txtLogfile, txtMessage);

        dialog.add(formLayout);

        Button cancelButton = new Button(getMessage("computer_events_view.close"), e -> dialog.close());
        dialog.getFooter().add(cancelButton);

        return dialog;
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();

        MenuHelper.createIconItem(menuBar,"/icons/refresh.svg", getMessage("computer_events_view.refresh"), menuItemClickEvent -> {
            computersService.clearEvents(filters.getFilters(), id);
            refreshGrid();
        });

        return menuBar;
    }

    public String getPageTitle() {
        return getMessage("computer_events_view.title");
    }

}
