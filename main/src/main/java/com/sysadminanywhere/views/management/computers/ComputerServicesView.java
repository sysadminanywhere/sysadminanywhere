package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.wmi.ServiceEntity;
import com.sysadminanywhere.service.ComputersService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
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
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;

@PageTitle("Services")
@Route(value = "management/computers/:id?/services")
@PermitAll
@Uses(Icon.class)
@Uses(TextArea.class)
public class ComputerServicesView extends Div implements BeforeEnterObserver, MenuControl {

    private String id;

    private Grid<ServiceEntity> grid;

    private Filters filters;
    private final ComputersService computersService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerServicesView(ComputersService computersService) {
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
        grid = new Grid<>(ServiceEntity.class, false);
        grid.addColumn("caption").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);

        grid.addItemClickListener(item -> {
            showDialog(item.getItem()).open();
        });

        try {
            grid.setItems(query -> computersService.getServices(
                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                    filters.getFilters(), id).stream());
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

    private Dialog showDialog(ServiceEntity service) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Service");
        dialog.setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setReadOnly(true);
        txtName.setValue(service.getName());

        TextArea txtDescription = new TextArea("Description");
        txtDescription.setReadOnly(true);
        txtDescription.setValue(service.getDescription());
        txtDescription.setMinHeight("100px");
        formLayout.setColspan(txtDescription, 2);

        TextField txtDisplayName = new TextField("Display name");
        txtDisplayName.setReadOnly(true);
        txtDisplayName.setValue(service.getDisplayName());

        TextField txtPathName = new TextField("Path name");
        txtPathName.setReadOnly(true);
        formLayout.setColspan(txtPathName, 2);
        if(service.getPathName() != null)
            txtPathName.setValue(service.getPathName());

        TextField txtState = new TextField("State");
        txtState.setReadOnly(true);
        txtState.setValue(service.getState());

        TextField txtStartMode = new TextField("Start mode");
        txtStartMode.setReadOnly(true);
        txtStartMode.setValue(service.getStartMode());

        formLayout.add(txtName, txtDisplayName, txtDescription, txtPathName, txtState, txtStartMode);

        dialog.add(formLayout);

        Button startButton = new Button("Start", e -> {
            computersService.startService(id, service);
            dialog.close();
        });
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        dialog.getFooter().add(startButton);

        Button stopButton = new Button("Stop", e -> {
            computersService.stopService(id, service);
            dialog.close();
        });
        stopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(stopButton);


        Button cancelButton = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(cancelButton);

        return dialog;
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();

        MenuHelper.createIconItem(menuBar,"/icons/refresh.svg", menuItemClickEvent -> {
            computersService.clearServices(filters.getFilters(), id);
            refreshGrid();
        });

        return menuBar;
    }

}