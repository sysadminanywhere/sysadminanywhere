package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.model.EventEntity;
import com.sysadminanywhere.model.ProcessEntity;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Processes")
@Route(value = "management/computers/:id?/processes", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ComputerProcessesView extends Div implements BeforeEnterObserver {

    private String id;

    private Grid<ProcessEntity> grid;

    private Filters filters;
    private final ComputersService computersService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerProcessesView(ComputersService computersService) {
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

        private final TextField caption = new TextField("Caption");

        public Filters(Runnable onSearch, ComputersService computersService) {
            this.computersService = computersService;

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                caption.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(caption, actions);
        }

        public Map<String, String> getFilters() {
            Map<String, String> filters = new HashMap<>();
            filters.put("caption", caption.getValue());
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(ProcessEntity.class, false);
        grid.addColumn("caption").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);

        grid.addItemClickListener(item -> {
            showDialog(item.getItem()).open();
        });

        grid.setItems(query -> computersService.getProcesses(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters(), id).stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Dialog showDialog(ProcessEntity process) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Process");
        dialog.setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtCaption = new TextField("Caption");
        txtCaption.setReadOnly(true);
        txtCaption.setValue(process.getCaption());

        TextField txtDescription = new TextField("Description");
        txtDescription.setReadOnly(true);
        txtDescription.setValue(process.getDescription());

        TextField txtHandle = new TextField("Handle");
        txtHandle.setReadOnly(true);
        txtHandle.setValue(process.getHandle());

        TextField txtExecutablePath = new TextField("Executable path");
        txtExecutablePath.setReadOnly(true);
        formLayout.setColspan(txtExecutablePath, 2);
        if (process.getExecutablePath() != null)
            txtExecutablePath.setValue(process.getExecutablePath());

        TextField txtWorkingSetSize = new TextField("Working set size");
        txtWorkingSetSize.setReadOnly(true);
        txtWorkingSetSize.setValue(process.getWorkingSetSize());

        formLayout.add(txtCaption, txtDescription, txtExecutablePath, txtHandle, txtWorkingSetSize);

        dialog.add(formLayout);

        Button stopButton = new Button("Stop", e -> {
            computersService.stopProcess(id, process);
            dialog.close();
        });
        stopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(stopButton);

        Button cancelButton = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(cancelButton);

        return dialog;
    }

}