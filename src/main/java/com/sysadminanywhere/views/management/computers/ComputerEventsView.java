package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.wmi.EventEntity;
import com.sysadminanywhere.service.ComputersService;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@PageTitle("Events")
@Route(value = "management/computers/:id?/events")
@PermitAll
@Uses(Icon.class)
@Uses(DatePicker.class)
@Uses(TextArea.class)
public class ComputerEventsView extends Div implements BeforeEnterObserver {

    private String id;

    private Grid<EventEntity> grid;

    private Filters filters;
    private final ComputersService computersService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerEventsView(ComputersService computersService) {
        this.computersService = computersService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), computersService);
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

        private final ComputersService computersService;

        private final TextField sourceName = new TextField("Source name");
        private final ComboBox<String> eventType = new ComboBox<>("Event type");
        private final DatePicker datePicker = new DatePicker("Date");

        public Filters(Runnable onSearch, ComputersService computersService) {
            this.computersService = computersService;

            eventType.setItems("Error", "Warning", "Information", "Audit Success", "Audit Failure", "All");
            eventType.setValue("Error");

            datePicker.setValue(LocalDate.now());

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                sourceName.clear();

                eventType.clear();
                eventType.setValue("Errors");

                datePicker.clear();
                datePicker.setValue(LocalDate.now());

                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(sourceName, eventType, datePicker, actions);
        }

        public Map<String, Object> getFilters() {
            Map<String, Object> filters = new HashMap<>();
            filters.put("sourceName", sourceName.getValue());
            filters.put("eventType", eventType.getValue());
            filters.put("date", datePicker.getValue());
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(EventEntity.class, false);
        grid.addColumn("sourceName").setAutoWidth(true);
        grid.addColumn("type").setAutoWidth(true);
        grid.addColumn("timeGenerated").setAutoWidth(true);
        grid.addColumn("logfile").setAutoWidth(true);

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

        dialog.setHeaderTitle("Event");
        dialog.setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtSourceName = new TextField("Source name");
        txtSourceName.setReadOnly(true);
        txtSourceName.setValue(event.getSourceName());

        TextField txtEventType = new TextField("Event type");
        txtEventType.setReadOnly(true);
        txtEventType.setValue(event.getType());

        TextField txtTimeGenerated = new TextField("Time generated");
        txtTimeGenerated.setReadOnly(true);
        txtTimeGenerated.setValue(event.getTimeGenerated());

        TextField txtLogfile = new TextField("Log file");
        txtLogfile.setReadOnly(true);
        txtLogfile.setValue(event.getLogfile());

        TextArea txtMessage = new TextArea("Message");
        txtMessage.setReadOnly(true);
        txtMessage.setValue(event.getMessage());
        txtMessage.setMinHeight("100px");
        formLayout.setColspan(txtMessage, 2);

        formLayout.add(txtSourceName, txtEventType, txtTimeGenerated, txtLogfile, txtMessage);

        dialog.add(formLayout);

        Button cancelButton = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(cancelButton);

        return dialog;
    }

}