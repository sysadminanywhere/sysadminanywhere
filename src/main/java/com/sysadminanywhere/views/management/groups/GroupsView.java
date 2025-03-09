package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.service.GroupsService;
import com.sysadminanywhere.views.MainLayout;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

@PageTitle("Groups")
@Route(value = "management/groups")
@PermitAll
@Uses(Icon.class)
public class GroupsView extends Div {

    private Grid<GroupEntry> grid;

    private Filters filters;
    private final GroupsService groupsService;

    public GroupsView(GroupsService groupsService) {
        this.groupsService = groupsService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), groupsService);
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

        private final GroupsService groupsService;

        private final TextField cn = new TextField("CN");
        private final ComboBox<String> availability = new ComboBox<>("Filters");

        public Filters(Runnable onSearch, GroupsService groupsService) {
            this.groupsService = groupsService;

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
                availability.setValue("All");
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Button plusButton = new Button("New");
            plusButton.addClickListener(e -> addDialog(onSearch).open());

            Div actions = new Div(plusButton, resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            availability.setItems("All", "Global security", "Global distribution", "Domain security", "Domain distribution", "Universal security", "Universal distribution", "BuiltIn");
            availability.setValue("All");

            add(cn, availability, actions);
        }

        public String getFilters() {
            String searchFilters = "";

            if (!cn.isEmpty()) {
                searchFilters += "(cn=" + cn.getValue() + "*)";
            }

            if (!availability.isEmpty()) {
                switch (availability.getValue()) {
                    case "Global security":
                        searchFilters += "(groupType=-2147483646)";
                        break;
                    case "Global distribution":
                        searchFilters += "(groupType=2)";
                        break;
                    case "Domain security":
                        searchFilters += "(groupType=-2147483644)";
                        break;
                    case "Domain distribution":
                        searchFilters += "(groupType=4)";
                        break;
                    case "Universal security":
                        searchFilters += "(groupType=-2147483640)";
                        break;
                    case "Universal distribution":
                        searchFilters += "(groupType=2)";
                        break;
                    case "BuiltIn":
                        searchFilters += "(groupType=-2147483643)";
                        break;
                }
            }

            return searchFilters;
        }

        private Dialog addDialog(Runnable onSearch) {
            return new AddGroupDialog(groupsService, onSearch);
        }

    }

    private Component createGrid() {
        grid = new Grid<>(GroupEntry.class, false);
        grid.addColumn("cn").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("management/groups/" + item.getItem().getCn() + "/details"));
        });

        grid.setItems(query -> groupsService.getAll(
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