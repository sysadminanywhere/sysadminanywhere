package com.sysadminanywhere.views;

import com.sysadminanywhere.control.MenuButton;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.views.about.AboutView;
import com.sysadminanywhere.views.account.MeView;
import com.sysadminanywhere.views.automation.AutomationsView;
import com.sysadminanywhere.views.domain.AuditView;
import com.sysadminanywhere.views.domain.DashboardView;
import com.sysadminanywhere.views.domain.DomainView;
import com.sysadminanywhere.views.incident.IncidentsView;
import com.sysadminanywhere.views.inventory.InventoryHardwareView;
import com.sysadminanywhere.views.inventory.InventorySoftwareView;
import com.sysadminanywhere.views.management.computers.ComputersView;
import com.sysadminanywhere.views.management.contacts.ContactsView;
import com.sysadminanywhere.views.management.container.ContainersView;
import com.sysadminanywhere.views.management.groups.GroupsView;
import com.sysadminanywhere.views.management.printers.PrintersView;
import com.sysadminanywhere.views.management.users.UsersView;
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
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.MessageSource;

import java.util.Locale;


@Layout
@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {

    private H1 viewTitle;
    private HorizontalLayout menuLayout;

    HorizontalLayout drawerContent = new HorizontalLayout();
    FlexLayout buttons = new FlexLayout();
    FlexLayout subNav = new FlexLayout();

    VerticalLayout topMenu;
    VerticalLayout bottomMenu;

    SideNav dashboardSubNavs;
    SideNav managementSubNavs;
    SideNav settingsSubNavs;
    SideNav inventorySubNavs;
    SideNav incidentsSubNavs;
    SideNav reportsSubNavs;
    SideNav accountSubNavs;
    SideNav automationsSubNavs;

    String currentTitle = "main_layout.dashboard";

    private final MessageSource messageSource;
    private final LocaleService localeService;

    private Locale locale;

    public MainLayout(MessageSource messageSource, LocaleService localeService) {

        this.messageSource = messageSource;
        this.localeService = localeService;

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

        drawerContent.add(buttons, subNav);

        drawerContent.setHeightFull();
        scroller.setHeightFull();

        scroller.getStyle().setMargin("0px");
        scroller.getStyle().setPadding("0px");

        addToDrawer(scroller);
        addHeaderContent();

        locale = localeService.getCurrentLocale();

        addNavigation();
    }

    private void addNavigation() {
        dashboardSubNavs = new SideNav();
        managementSubNavs = new SideNav();
        settingsSubNavs = new SideNav();
        inventorySubNavs = new SideNav();
        incidentsSubNavs = new SideNav();
        reportsSubNavs = new SideNav();
        accountSubNavs = new SideNav();
        automationsSubNavs = new SideNav();

        topMenu = new VerticalLayout(createSelectedMainButtonItem("main_layout.dashboard", getMessage("main_layout.dashboard"), DashboardView.class, "icons/dashboard.svg"),
                createMainButtonItem("main_layout.management", getMessage("main_layout.management"), ContainersView.class, "icons/management.svg"),
                createMainButtonItem("main_layout.incidents", getMessage("main_layout.incidents"), IncidentsView.class, "icons/incident.svg"),
                createMainButtonItem("main_layout.automation", getMessage("main_layout.automation"), AutomationsView.class, "icons/automation.svg"),
                createMainButtonItem("main_layout.inventory", getMessage("main_layout.inventory"), InventorySoftwareView.class, "icons/inventory.svg"),
                createMainButtonItem("main_layout.reports", getMessage("main_layout.reports"), UserReportsView.class, "icons/reports.svg"));
        topMenu.setMargin(false);

        bottomMenu = new VerticalLayout();

        bottomMenu.add(createMainButtonItem("main_layout.account", getMessage("main_layout.account"), MeView.class, "icons/user.svg"));

        bottomMenu.add(createMainButtonItem("main_layout.settings", getMessage("main_layout.settings"), SettingsView.class, "icons/settings.svg"));

        bottomMenu.setHeightFull();
        bottomMenu.setMargin(false);
        bottomMenu.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        int count = Math.toIntExact(buttons.getChildren().count());

        if (count == 3) {
            buttons.remove(buttons.getComponentAt(count - 1));
            buttons.remove(buttons.getComponentAt(count - 2));
        }

        buttons.add(topMenu, bottomMenu);

        dashboardSubNavs.addItem(createSideNavItem(getMessage("main_layout.dashboard"), DashboardView.class),
                createSideNavItem(getMessage("main_layout.domain"), DomainView.class),
                createSideNavItem(getMessage("main_layout.audit"), AuditView.class));

        managementSubNavs.addItem(
                createSideNavItem(getMessage("main_layout.containers"), ContainersView.class),
                createSideNavItem(getMessage("main_layout.users"), UsersView.class),
                createSideNavItem(getMessage("main_layout.computers"), ComputersView.class),
                createSideNavItem(getMessage("main_layout.groups"), GroupsView.class),
                createSideNavItem(getMessage("main_layout.printers"), PrintersView.class),
                createSideNavItem(getMessage("main_layout.contacts"), ContactsView.class));

        settingsSubNavs.addItem(createSideNavItem(getMessage("main_layout.settings"), SettingsView.class),
                createSideNavItem(getMessage("main_layout.about"), AboutView.class));

        inventorySubNavs.addItem(createSideNavItem(getMessage("main_layout.software_inventory"), InventorySoftwareView.class),
                createSideNavItem(getMessage("main_layout.hardware_inventory"), InventoryHardwareView.class));

        incidentsSubNavs.addItem(createSideNavItem(getMessage("main_layout.incidents"), IncidentsView.class));

        automationsSubNavs.addItem(createSideNavItem(getMessage("main_layout.workflows"), AutomationsView.class));

        reportsSubNavs.addItem(createSideNavItem(getMessage("main_layout.users_reports"), UserReportsView.class),
                createSideNavItem(getMessage("main_layout.computer_reports"), ComputerReportsView.class),
                createSideNavItem(getMessage("main_layout.group_reports"), GroupReportsView.class));

        accountSubNavs.addItem(createSideNavItem(getMessage("main_layout.me"), MeView.class));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!locale.equals(localeService.getCurrentLocale())) {
            locale = localeService.getCurrentLocale();
            addNavigation();
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

            H4 title = new H4(getMessage(currentTitle));
            title.getStyle().setMarginTop("10px");
            title.getStyle().setMarginBottom("10px");

            Hr hr = new Hr();
            hr.getStyle().setMarginBottom("10px");

            subNav.add(title, hr);

            if (currentRoute.startsWith("settings")) {
                subNav.add(settingsSubNavs);
            } else if (currentRoute.startsWith("account")) {
                subNav.add(accountSubNavs);
            } else if (currentRoute.startsWith("management")) {
                subNav.add(managementSubNavs);
            } else if (currentRoute.startsWith("incidents")) {
                subNav.add(incidentsSubNavs);
            } else if (currentRoute.startsWith("automation")) {
                subNav.add(automationsSubNavs);
            } else if (currentRoute.startsWith("inventory")) {
                subNav.add(inventorySubNavs);
            } else if (currentRoute.startsWith("reports")) {
                subNav.add(reportsSubNavs);
            } else if (currentRoute.startsWith("dashboard") || currentRoute.startsWith("domain") || currentRoute.isEmpty()) {
                subNav.add(dashboardSubNavs);
            }
        }

    }

    private MenuButton createMainButtonItem(String key, String label, Class<? extends Component> navigationTarget, String imgPath) {
        return createMainButtonItem(key, label, navigationTarget, imgPath, false);
    }

    private MenuButton createSelectedMainButtonItem(String key, String label, Class<? extends Component> navigationTarget, String imgPath) {
        return createMainButtonItem(key, label, navigationTarget, imgPath, true);
    }

    private MenuButton createMainButtonItem(String key, String label, Class<? extends Component> navigationTarget, String imgPath, boolean isSelected) {
        MenuButton button = new MenuButton(label, imgPath);

        if (isSelected)
            button.selected(true);

        button.addClickListener(e -> {
            currentTitle = key;
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
            if (component instanceof VerticalLayout layout) {
                for (Component item : layout.getChildren().toList()) {
                    if (item instanceof MenuButton button) {
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