package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.entity.LoginEntity;
import com.sysadminanywhere.model.DisplayNamePattern;
import com.sysadminanywhere.model.LoginPattern;
import com.sysadminanywhere.model.Settings;
import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LoginService;
import com.sysadminanywhere.service.SettingsService;
import com.sysadminanywhere.service.UsersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.PasswordField;
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
import java.util.Optional;
import java.util.regex.Pattern;

@PageTitle("Users")
@Route(value = "management/users", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class UsersView extends Div {

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

            Button plusButton = new Button("New");
            plusButton.addClickListener(e -> addDialog(onSearch).open());

            Div actions = new Div(plusButton, resetBtn, searchBtn);
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

        private Dialog addDialog(Runnable onSearch) {

            Pattern userDisplayNameFormat = Pattern.compile("");
            Pattern userLoginPattern = Pattern.compile("");
            String userLoginFormat = "";

            Optional<UserEntry> maybeUser = authenticatedUser.get();
            if (maybeUser.isPresent()) {
                UserEntry user = maybeUser.get();
                loginEntity = loginService.getLogin(user);
                if (loginEntity.isPresent()) {
                    settings = settingsService.getSettings(loginEntity.get());

                    userDisplayNameFormat = Pattern.compile(DisplayNamePattern.valueOf(settings.getDisplayNamePattern()).getPattern());
                    LoginPattern loginPattern = LoginPattern.valueOf(settings.getLoginPattern());
                    userLoginPattern = Pattern.compile(loginPattern.getPattern());
                    userLoginFormat = loginPattern.getFormat();
                }
            }

            Dialog dialog = new Dialog();

            dialog.setHeaderTitle("New user");
            dialog.setMaxWidth("800px");

            FormLayout formLayout = new FormLayout();

            ContainerField containerField = new ContainerField(usersService.getLdapService());
            containerField.setValue(usersService.getDefaultContainer());
            formLayout.setColspan(containerField, 2);

            TextField txtDisplayName = new TextField("Display name");
            txtDisplayName.setRequired(true);
            formLayout.setColspan(txtDisplayName, 2);

            TextField txtFirstName = new TextField("First name");
            txtFirstName.setRequired(true);
            TextField txtInitials = new TextField("Initials");
            TextField txtLastName = new TextField("Last name");
            txtLastName.setRequired(true);

            TextField txtAccountName = new TextField("Account name");
            txtAccountName.setRequired(true);

            PasswordField txtPassword = new PasswordField("Password");
            txtPassword.setRequired(true);
            PasswordField txtConfirmPassword = new PasswordField("Confirm password");
            txtConfirmPassword.setRequired(true);

            VerticalLayout checkboxGroup = new VerticalLayout();
            formLayout.setColspan(checkboxGroup, 2);
            Checkbox chkUserMustChangePassword = new Checkbox("User must change password at next logon");
            chkUserMustChangePassword.setValue(true);
            Checkbox chkUserCannotChangePassword = new Checkbox("User cannot change password");
            Checkbox chkPasswordNeverExpires = new Checkbox("Password never expires");
            chkPasswordNeverExpires.setEnabled(false);
            Checkbox chkAccountDisabled = new Checkbox("Account disabled");

            checkboxGroup.add(chkUserMustChangePassword, chkUserCannotChangePassword, chkPasswordNeverExpires, chkAccountDisabled);

            chkUserMustChangePassword.addValueChangeListener(event -> {
                if (chkUserMustChangePassword.getValue()) {
                    chkPasswordNeverExpires.setEnabled(false);
                    chkPasswordNeverExpires.setValue(false);
                } else {
                    chkPasswordNeverExpires.setEnabled(true);
                }
            });

            chkPasswordNeverExpires.addValueChangeListener(event -> {
                if (chkPasswordNeverExpires.getValue()) {
                    chkUserMustChangePassword.setEnabled(false);
                    chkUserMustChangePassword.setValue(false);
                } else {
                    chkUserMustChangePassword.setEnabled(true);
                }
            });

            Pattern finalUserDisplayNameFormat = userDisplayNameFormat;
            Pattern finalUserLoginPattern = userLoginPattern;
            String finalUserLoginFormat = userLoginFormat;

            txtDisplayName.addValueChangeListener(event -> {
                txtFirstName.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${FirstName}"));
                txtLastName.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${LastName}"));

                if(finalUserDisplayNameFormat.toString().contains("<Middle>"))
                    txtInitials.setValue(finalUserDisplayNameFormat.matcher(txtDisplayName.getValue()).replaceAll("${Middle}"));

                txtAccountName.setValue(finalUserLoginPattern.matcher(txtDisplayName.getValue()).replaceAll(finalUserLoginFormat).toLowerCase());
            });

            if(settings != null && !settings.getDefaultPassword().isEmpty()) {
                txtPassword.setValue(settings.getDefaultPassword());
                txtConfirmPassword.setValue(settings.getDefaultPassword());
            }

            formLayout.add(containerField, txtDisplayName, txtFirstName, txtInitials, txtLastName, txtAccountName, txtPassword, txtConfirmPassword, checkboxGroup);
            dialog.add(formLayout);

            Button saveButton = new Button("Save", e -> {
                UserEntry user = new UserEntry();
                user.setCn(txtDisplayName.getValue());
                user.setDisplayName(txtDisplayName.getValue());
                user.setFirstName(txtFirstName.getValue());
                user.setInitials(txtInitials.getValue());
                user.setLastName(txtLastName.getValue());
                user.setSamAccountName(txtAccountName.getValue());
                try {
                    UserEntry newUser = usersService.add(
                            containerField.getValue(),
                            user,
                            txtPassword.getValue(),
                            chkUserCannotChangePassword.getValue(),
                            chkPasswordNeverExpires.getValue(),
                            chkAccountDisabled.getValue(),
                            chkUserMustChangePassword.getValue());

                    onSearch.run();

                    Notification notification = Notification.show("User added");
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