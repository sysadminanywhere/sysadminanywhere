package com.sysadminanywhere.views;

import com.sysadminanywhere.control.MenuButton;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.LoginService;
import com.sysadminanywhere.views.about.AboutView;
import com.sysadminanywhere.views.domain.AuditView;
import com.sysadminanywhere.views.domain.DashboardView;
import com.sysadminanywhere.views.domain.DomainView;
import com.sysadminanywhere.views.inventory.InventorySoftwareView;
import com.sysadminanywhere.views.login.LoginView;
import com.sysadminanywhere.views.management.computers.ComputersView;
import com.sysadminanywhere.views.management.contacts.ContactsView;
import com.sysadminanywhere.views.management.groups.GroupsView;
import com.sysadminanywhere.views.management.printers.PrintersView;
import com.sysadminanywhere.views.management.users.UsersView;
import com.sysadminanywhere.views.monitoring.MonitorsView;
import com.sysadminanywhere.views.reports.ComputerReportsView;
import com.sysadminanywhere.views.reports.GroupReportsView;
import com.sysadminanywhere.views.reports.UserReportsView;
import com.sysadminanywhere.views.settings.SettingsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

@Layout
@PermitAll
public class RootLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {

    private H1 viewTitle;
    private HorizontalLayout menuLayout;

    HorizontalLayout drawerContent = new HorizontalLayout();
    FlexLayout buttons = new FlexLayout();
    FlexLayout subNav = new FlexLayout();

    SideNav dashboardSubNavs = new SideNav();
    SideNav managementSubNavs = new SideNav();
    SideNav adminSubNavs = new SideNav();
    SideNav inventorySubNavs = new SideNav();
    SideNav monitoringSubNavs = new SideNav();
    SideNav reportsSubNavs = new SideNav();

    String currentTitle = "Domain";

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    private final LoginService loginService;
    private final LdapService ldapService;

    public RootLayout(AuthenticatedUser authenticatedUser,
                      AccessAnnotationChecker accessChecker,
                      LoginService loginService, LdapService ldapService) {

        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.loginService = loginService;
        this.ldapService = ldapService;

        currentTitle = ldapService.getDomainName();

        setPrimarySection(Section.DRAWER);
        getElement().setAttribute("theme", "teams-nav");

        buttons.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        subNav.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

        buttons.getStyle().setBackground("#D6DBE0");

        buttons.setWidth("90px");
        buttons.setHeightFull();
        buttons.setAlignContent(FlexLayout.ContentAlignment.CENTER);

        subNav.setWidthFull();
        subNav.getStyle().setMargin("5px");
        subNav.getStyle().setMarginRight("10px");

        Image logo = new Image("images/sa-logo.png", "Sysadmin Anywhere");
        logo.setWidth("48px");
        logo.setHeight("48px");
        logo.getStyle().setBorderRadius("10px");
        logo.getStyle().setMargin("10px");
        buttons.add(logo);

        drawerContent.getStyle().setMargin("0px");
        drawerContent.getStyle().setPadding("0px");

        Scroller scroller = new Scroller(drawerContent);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        VerticalLayout topMenu = new VerticalLayout(createSelectedMainButtonItem(currentTitle, DashboardView.class, "/icons/dashboard.svg"),
                createMainButtonItem("Management", UsersView.class, "/icons/management.svg"),
                createMainButtonItem("Inventory", InventorySoftwareView.class, "/icons/inventory.svg"),
                createMainButtonItem("Monitoring", MonitorsView.class, "/icons/monitoring.svg"),
                createMainButtonItem("Reports", UserReportsView.class, "/icons/reports.svg"));
        topMenu.setMargin(false);

        VerticalLayout bottomMenu = new VerticalLayout(createMainButtonItem("Account", SettingsView.class, "/icons/user.svg"),
                createMainButtonItem("Settings", SettingsView.class, "/icons/settings.svg"));

        bottomMenu.setHeightFull();
        bottomMenu.setMargin(false);
        bottomMenu.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        buttons.add(topMenu, bottomMenu);

        dashboardSubNavs.addItem(createSideNavItem("Dashboard", DashboardView.class),
                createSideNavItem("Domain", DomainView.class),
                createSideNavItem("Audit", AuditView.class));

        managementSubNavs.addItem(createSideNavItem("Users", UsersView.class),
                createSideNavItem("Computers", ComputersView.class),
                createSideNavItem("Groups", GroupsView.class),
                createSideNavItem("Printers", PrintersView.class),
                createSideNavItem("Contacts", ContactsView.class));

        adminSubNavs.addItem(createSideNavItem("Settings", SettingsView.class),
                createSideNavItem("About", AboutView.class));

        inventorySubNavs.addItem(createSideNavItem("Software inventory", InventorySoftwareView.class));

        monitoringSubNavs.addItem(createSideNavItem("Monitors", MonitorsView.class));

        reportsSubNavs.addItem(createSideNavItem("Users reports", UserReportsView.class),
                createSideNavItem("Computer Reports", ComputerReportsView.class),
                createSideNavItem("Group Reports", GroupReportsView.class));

        drawerContent.add(buttons, subNav);

        drawerContent.setHeightFull();
        scroller.setHeightFull();

        scroller.getStyle().setMargin("0px");
        scroller.getStyle().setPadding("0px");

        AboutView view = new AboutView();
        setContent(view);

        addToDrawer(scroller);
        addHeaderContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UserEntry> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            UserEntry user = maybeUser.get();
            if(!loginService.isLoggedIn())
                loginService.Login(user);
        } else {
            UI.getCurrent().navigate(LoginView.class);
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());

        menuLayout.removeAll();

        Component view = getContent();
        if (view instanceof MenuControl) {
            menuLayout.add(((MenuControl) view).getMenu());
        }

        updateSubNavBasedOnRoute();
    }

    private void updateSubNavBasedOnRoute() {
        if (getUI().isPresent()) {

            subNav.removeAll();
            String currentRoute = getUI().get().getInternals().getActiveViewLocation().getPath();

            H4 title = new H4(currentTitle);
            title.getStyle().setMarginTop("10px");
            title.getStyle().setMarginBottom("10px");

            Hr hr = new Hr();
            hr.getStyle().setMarginBottom("10px");

            subNav.add(title, hr);

            if (currentRoute.startsWith("settings")) {
                subNav.add(adminSubNavs);
            } else if (currentRoute.startsWith("management")) {
                subNav.add(managementSubNavs);
            } else if (currentRoute.startsWith("monitoring")) {
                subNav.add(monitoringSubNavs);
            } else if (currentRoute.startsWith("inventory")) {
                subNav.add(inventorySubNavs);
            } else if (currentRoute.startsWith("reports")) {
                subNav.add(reportsSubNavs);
            } else if (currentRoute.startsWith("dashboard") || currentRoute.startsWith("domain") || currentRoute.isEmpty()) {
                subNav.add(dashboardSubNavs);
            }
        }

    }

    private MenuButton createMainButtonItem(String label, Class<? extends Component> navigationTarget, String imgPath) {
        return createMainButtonItem(label, navigationTarget, imgPath, false);
    }

    private MenuButton createSelectedMainButtonItem(String label, Class<? extends Component> navigationTarget, String imgPath) {
        return createMainButtonItem(label, navigationTarget, imgPath, true);
    }

    private MenuButton createMainButtonItem(String label, Class<? extends Component> navigationTarget, String imgPath, boolean isSelected) {
        MenuButton button = new MenuButton(label, imgPath);

        if (isSelected)
            button.selected(true);

        button.addClickListener(e -> {
            currentTitle = label;
            unselectButtons();
            button.selected(true);
            UI.getCurrent().navigate(navigationTarget);
        });

        return button;
    }

    private SideNavItem createSideNavItem(String label, Class<? extends Component> navigationTarget) {
        SideNavItem sideNavItem = new SideNavItem(label, navigationTarget);
        sideNavItem.setMatchNested(true);
        return sideNavItem;
    }

    private void unselectButtons() {
        for (Component component : buttons.getChildren().toList()) {
            if (component instanceof VerticalLayout) {
                VerticalLayout layout = (VerticalLayout) component;
                for (Component item : layout.getChildren().toList()) {
                    if (item instanceof MenuButton) {
                        MenuButton button = (MenuButton) item;
                        button.selected(false);
                    }
                }
            }
        }
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.setWidthFull();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        menuLayout = new HorizontalLayout();
        menuLayout.setWidthFull();
        menuLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        menuLayout.getStyle().setMarginRight("20px");
        menuLayout.getStyle().setMarginLeft("20px");

        addToNavbar(true, toggle, viewTitle, menuLayout);
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }

}