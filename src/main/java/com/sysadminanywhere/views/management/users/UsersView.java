package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LoginService;
import com.sysadminanywhere.service.SettingsService;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

@PageTitle("Users")
@Route(value = "management/users")
@PermitAll
@Uses(Icon.class)
@Uses(Upload.class)
public class UsersView extends Div implements MenuControl {

    private Grid<UserEntry> grid;

    private Filters filters;
    private final UsersService usersService;

    private AuthenticatedUser authenticatedUser;
    private final LoginService loginService;
    private final SettingsService settingsService;

    private Optional<LoginEntity> loginEntity;
    private Settings settings;

    public UsersView(UsersService usersService, AuthenticatedUser authenticatedUser, LoginService loginService, SettingsService settingsService) {
        this.usersService = usersService;
        this.loginService = loginService;
        this.settingsService = settingsService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), usersService, authenticatedUser, loginService, settingsService);
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

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();

        MenuHelper.createIconItem(menuBar, "/icons/plus.svg", "New", event -> {
            addDialog(this::refreshGrid).open();
        });

        MenuHelper.createIconItem(menuBar, "/icons/import.svg", "Import", event -> {
            importDialog(this::refreshGrid).open();
        });

        return menuBar;
    }

    private Dialog addDialog(Runnable onSearch) {
        return new AddUserDialog(usersService, loginService, authenticatedUser, settingsService, onSearch);
    }

    private Dialog importDialog(Runnable onSearch) {
        return new ImportUserDialog(usersService, onSearch);
    }

    public static class Filters extends Div {

        private final UsersService usersService;
        private AuthenticatedUser authenticatedUser;
        private final LoginService loginService;
        private final SettingsService settingsService;

        private Optional<LoginEntity> loginEntity;
        private Settings settings;

        private final TextField cn = new TextField("CN");
        private final ComboBox<String> availability = new ComboBox<>("Filters");

        public Filters(Runnable onSearch, UsersService usersService, AuthenticatedUser authenticatedUser, LoginService loginService, SettingsService settingsService) {
            this.usersService = usersService;
            this.loginService = loginService;
            this.settingsService = settingsService;
            this.authenticatedUser = authenticatedUser;

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

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

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            availability.setItems("All", "Disabled", "Locked", "Expired", "Never expires");
            availability.setValue("All");

            add(cn, availability, actions);
        }

        public String getFilters() {
            String searchFilters = "";

            if (!cn.isEmpty()) {
                searchFilters += "(cn=" + cn.getValue() + "*)";
            }
            if (!availability.isEmpty()) {
                if (availability.getValue().equalsIgnoreCase("Disabled"))
                    searchFilters += "(userAccountControl:1.2.840.113556.1.4.803:=2)";
                if (availability.getValue().equalsIgnoreCase("Locked"))
                    searchFilters += "(lockoutTime>=1)";
                if (availability.getValue().equalsIgnoreCase("Expired"))
                    searchFilters += "(accountExpires<=127818648000000000)";
                if (availability.getValue().equalsIgnoreCase("Never expires"))
                    searchFilters += "(userAccountControl:1.2.840.113556.1.4.803:=65536)";
            }

            return searchFilters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(UserEntry.class, false);
        grid.addColumn("cn").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("management/users/" + item.getItem().getCn() + "/details"));
        });

        grid.setItems(query -> usersService.getAll(
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