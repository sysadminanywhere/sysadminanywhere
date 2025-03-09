package com.sysadminanywhere.views;

import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LoginService;
import com.sysadminanywhere.views.about.AboutView;
import com.sysadminanywhere.views.dashboard.DashboardView;
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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    String currentTitle = "Dashboard";

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    private final LoginService loginService;

    public RootLayout(AuthenticatedUser authenticatedUser,
                      AccessAnnotationChecker accessChecker,
                      LoginService loginService) {

        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.loginService = loginService;

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

        VerticalLayout topMenu = new VerticalLayout(createSelectedMainButtonItem("Dashboard", DashboardView.class, "icons/dashboard.png"),
                createMainButtonItem("Management", UsersView.class, "icons/management.png"),
                createMainButtonItem("Inventory", InventorySoftwareView.class, "icons/inventory.png"),
                createMainButtonItem("Monitoring", MonitorsView.class, "icons/monitoring.png"),
                createMainButtonItem("Reports", UserReportsView.class, "icons/reports.png"));
        topMenu.setMargin(false);

        VerticalLayout bottomMenu = new VerticalLayout(createMainButtonItem("Settings", SettingsView.class, "icons/settings.png"));
        bottomMenu.setHeightFull();
        bottomMenu.setMargin(false);
        bottomMenu.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        buttons.add(topMenu, bottomMenu);

        dashboardSubNavs.addItem(createSideNavItem("Dashboard", DashboardView.class));

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
            } else if (currentRoute.startsWith("dashboard") || currentRoute.isEmpty()) {
                subNav.add(dashboardSubNavs);
            }
        }

    }

    private Button createMainButtonItem(String label, Class<? extends Component> navigationTarget, String imgPath) {
        return createMainButtonItem(label, navigationTarget, imgPath, false);
    }

    private Button createSelectedMainButtonItem(String label, Class<? extends Component> navigationTarget, String imgPath) {
        return createMainButtonItem(label, navigationTarget, imgPath, true);
    }

    private Button createMainButtonItem(String label, Class<? extends Component> navigationTarget, String imgPath, boolean isSelected) {
        Image img = new Image(imgPath, "");
        Button button = new Button(img);

        button.setWidth("48px");
        button.setHeight("48px");

        button.getStyle().setMargin("0px");
        button.getStyle().setBorder("none");
        button.setClassName("teams-nav-button");
        changeIconColor(button, false);
        button.getStyle().setBorderRadius("10px");
        button.getStyle().setBackground("transparent");

        if (isSelected)
            selectButton(button);

        button.addClickListener(e -> {
            currentTitle = label;
            unselectButtons();
            selectButton(button);
            UI.getCurrent().navigate(navigationTarget);
        });

        return button;
    }

    private SideNavItem createSideNavItem(String label, Class<? extends Component> navigationTarget) {
        return new SideNavItem(label, navigationTarget);
    }

    private void unselectButtons() {
        for (Component component : buttons.getChildren().toList()) {
            if (component instanceof VerticalLayout) {
                VerticalLayout layout = (VerticalLayout) component;
                for (Component item : layout.getChildren().toList()) {
                    if (item instanceof Button) {
                        Button button = (Button) item;
                        button.getStyle().setBorder("none");
                        changeIconColor(button, false);
                        button.getStyle().setBackground("transparent");
                        button.getElement().removeAttribute("active");
                    }
                }
            }
        }
    }

    private void selectButton(Button button) {
        button.getStyle().setBorder("1px");
        button.getStyle().setBackground("#F6F8F9");
        changeIconColor(button, true);
        button.getElement().setAttribute("active", true);
    }

    private void changeIconColor(Button button, boolean isSelected) {
        Image image = (Image) button.getChildren()
                .filter(component -> {
                    return component instanceof Image;
                })
                .findFirst()
                .orElse(null);

        if(image != null) {
//            Image img = new Image("", "");
//            button.setIcon(img);
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