package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Computers")
@Route(value = "management/computers", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ComputersView extends Div {

    private Grid<ComputerEntry> grid;

    private Filters filters;
    private final ComputersService computersService;

    public ComputersView(ComputersService computersService) {
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

    public static class Filters extends Div implements FilterSpecification<ComputerEntry> {

        private final ComputersService computersService;

        private final TextField cn = new TextField("CN");

        public Filters(Runnable onSearch, ComputersService computersService) {
            this.computersService = computersService;

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            //cn.setPlaceholder("First or last name");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                cn.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Button plusButton = new Button("+ Add");
            plusButton.addClickListener(e -> addDialog(onSearch).open());

            Div actions = new Div(plusButton, resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(cn, actions);
        }

        @Override
        public Predicate toPredicate(Root<ComputerEntry> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        @Override
        public String getFilters() {
            String searchFilters = "";

            if (!cn.isEmpty()) {
                searchFilters += "(cn=" + cn.getValue() + "*)";
            }

            return searchFilters;
        }

        private Dialog addDialog(Runnable onSearch) {
            Dialog dialog = new Dialog();

            dialog.setHeaderTitle("New computer");
            dialog.setMaxWidth("800px");

            FormLayout formLayout = new FormLayout();

            TextField txtContainer = new TextField("Container");
            formLayout.setColspan(txtContainer, 2);

            TextField txtName = new TextField("Name");
            formLayout.setColspan(txtName, 2);

            TextField txtDescription = new TextField("Description");
            formLayout.setColspan(txtDescription, 2);

            TextField txtLocation = new TextField("Location");
            formLayout.setColspan(txtLocation, 2);

            VerticalLayout checkboxGroup = new VerticalLayout();
            formLayout.setColspan(checkboxGroup, 2);
            Checkbox chkAccountEnabled = new Checkbox("Account enabled");

            checkboxGroup.add(chkAccountEnabled);

            formLayout.add(txtContainer, txtName, txtDescription, txtLocation, checkboxGroup);
            dialog.add(formLayout);

            Button saveButton = new Button("Save", e -> {
                ComputerEntry computer = new ComputerEntry();
                computer.setCn(txtName.getValue());
                computer.setDescription(txtDescription.getValue());
                computer.setLocation(txtLocation.getValue());
                try {
                    ComputerEntry newComputer = computersService.add(txtContainer.getValue(), computer, chkAccountEnabled.getValue());

                    onSearch.run();

                    Notification notification = Notification.show("Computer added");
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception ex) {
                    Notification notification = Notification.show(ex.getMessage());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                dialog.close();
            });

            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            Button cancelButton = new Button("Cancel", e -> dialog.close());
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(saveButton);

            return dialog;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(ComputerEntry.class, false);
        grid.addColumn("cn").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            Button button = new Button(new Icon(VaadinIcon.EDIT));
            button.addThemeVariants(ButtonVariant.LUMO_ICON);
            button.addClickListener(e ->
                    button.getUI().ifPresent(ui ->
                            ui.navigate("management/computers/update"))
            );
            return button;
        }).setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(item -> {
            Button button = new Button(new Icon(VaadinIcon.TRASH));
            button.addThemeVariants(ButtonVariant.LUMO_ICON);

            button.addClickListener(e -> {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Delete");
                dialog.setText("Are you sure you want to permanently delete this computer?");

                dialog.setCancelable(true);

                dialog.setConfirmText("Delete");
                dialog.setConfirmButtonTheme("error primary");

                dialog.addConfirmListener(event -> {
                    computersService.delete(item.getDistinguishedName());
                    refreshGrid();
                });

                dialog.open();
            });

            return button;
        }).setAutoWidth(true).setFlexGrow(0);

        grid.setItems(query -> computersService.getAll(
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
