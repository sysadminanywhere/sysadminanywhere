package com.sysadminanywhere.views;

import com.sysadminanywhere.control.MenuButton;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.control.OnboardingTour;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.views.about.AboutView;
import com.sysadminanywhere.views.about.HelpView;
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
import com.sysadminanywhere.views.reports.ContactReportsView;
import com.sysadminanywhere.views.reports.GroupReportsView;
import com.sysadminanywhere.views.reports.PrinterReportsView;
import com.sysadminanywhere.views.reports.UserReportsView;
import com.sysadminanywhere.views.settings.SettingsView;
import com.sysadminanywhere.views.search.SearchView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.context.MessageSource;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver, BeforeEnterObserver {

    private H3 viewTitle;
    private HorizontalLayout menuLayout;
    private final Map<String, MenuButton> mainButtons = new LinkedHashMap<>();

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
    private boolean tourChecked;

    public MainLayout(MessageSource messageSource, LocaleService localeService) {

        this.messageSource = messageSource;
        this.localeService = localeService;

        setPrimarySection(Section.DRAWER);
        getElement().setAttribute("theme", "teams-nav");
        getElement().setAttribute("no-scroll", true);

        buttons.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        subNav.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

        buttons.addClassName("primary-navigation");
        buttons.getElement().setAttribute("data-tour", "primary-navigation");
        buttons.setWidth("78px");
        buttons.setHeightFull();
        buttons.setAlignContent(FlexLayout.ContentAlignment.CENTER);

        subNav.addClassName("secondary-navigation");
        subNav.getElement().setAttribute("data-tour", "secondary-navigation");
        subNav.setWidth("224px");

        Image logo = new Image("images/sa-logo.png", "Sysadmin Anywhere");
        logo.addClassName("navigation-logo");
        buttons.add(logo);

        drawerContent.addClassName("navigation-shell");

        Scroller scroller = new Scroller(drawerContent);
        scroller.setClassName("navigation-scroller");
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);

        drawerContent.add(buttons, subNav);

        drawerContent.setHeightFull();
        scroller.setHeightFull();

        addToDrawer(scroller);
        addHeaderContent();

        locale = localeService.getCurrentLocale();

        addNavigation();
    }

    private void addNavigation() {
        if (topMenu != null) {
            buttons.remove(topMenu);
        }
        if (bottomMenu != null) {
            buttons.remove(bottomMenu);
        }
        mainButtons.clear();

        dashboardSubNavs = new SideNav();
        managementSubNavs = new SideNav();
        settingsSubNavs = new SideNav();
        inventorySubNavs = new SideNav();
        incidentsSubNavs = new SideNav();
        reportsSubNavs = new SideNav();
        accountSubNavs = new SideNav();
        automationsSubNavs = new SideNav();

        topMenu = new VerticalLayout(createMainButtonItem("main_layout.dashboard", getMessage("main_layout.dashboard"), DashboardView.class, "icons/dashboard.svg"),
                createMainButtonItem("main_layout.management", getMessage("main_layout.management"), ContainersView.class, "icons/management.svg"),
                createMainButtonItem("main_layout.incidents", getMessage("main_layout.incidents"), IncidentsView.class, "icons/incident.svg"),
                createMainButtonItem("main_layout.automation", getMessage("main_layout.automation"), AutomationsView.class, "icons/automation.svg"),
                createMainButtonItem("main_layout.inventory", getMessage("main_layout.inventory"), InventorySoftwareView.class, "icons/inventory.svg"),
                createMainButtonItem("main_layout.reports", getMessage("main_layout.reports"), UserReportsView.class, "icons/reports.svg"));
        topMenu.setMargin(false);
        topMenu.setPadding(false);
        topMenu.setSpacing(false);
        topMenu.addClassName("primary-navigation-group");

        bottomMenu = new VerticalLayout();

        bottomMenu.add(createMainButtonItem("main_layout.account", getMessage("main_layout.account"), MeView.class, "icons/user.svg"));

        bottomMenu.add(createMainButtonItem("main_layout.settings", getMessage("main_layout.settings"), SettingsView.class, "icons/settings.svg"));

        bottomMenu.setHeightFull();
        bottomMenu.setMargin(false);
        bottomMenu.setPadding(false);
        bottomMenu.setSpacing(false);
        bottomMenu.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        bottomMenu.addClassName("primary-navigation-group");

        buttons.add(topMenu, bottomMenu);

        dashboardSubNavs.addItem(createSideNavItem(getMessage("main_layout.dashboard"), DashboardView.class),
                createSideNavItem(getMessage("main_layout.search"), SearchView.class),
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
                createSideNavItem(getMessage("main_layout.help"), HelpView.class),
                createSideNavItem(getMessage("main_layout.about"), AboutView.class));

        inventorySubNavs.addItem(createSideNavItem(getMessage("main_layout.software_inventory"), InventorySoftwareView.class),
                createSideNavItem(getMessage("main_layout.hardware_inventory"), InventoryHardwareView.class));

        incidentsSubNavs.addItem(createSideNavItem(getMessage("main_layout.incidents"), IncidentsView.class));

        automationsSubNavs.addItem(createSideNavItem(getMessage("main_layout.workflows"), AutomationsView.class));

        reportsSubNavs.addItem(createSideNavItem(getMessage("main_layout.users_reports"), UserReportsView.class),
                createSideNavItem(getMessage("main_layout.computer_reports"), ComputerReportsView.class),
                createSideNavItem(getMessage("main_layout.group_reports"), GroupReportsView.class),
                createSideNavItem(getMessage("main_layout.printer_reports"), PrinterReportsView.class),
                createSideNavItem(getMessage("main_layout.contact_reports"), ContactReportsView.class));

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
        viewTitle.setText(getCurrentPageTitle());

        menuLayout.removeAll();

        Component view = getContent();
        if (view instanceof MenuControl) {
            menuLayout.add(((MenuControl) view).getMenu());
        }
        updateSubNavBasedOnRoute();

        if (!tourChecked && !"login".equals(event.getLocation().getPath())) {
            tourChecked = true;
            getUI().ifPresent(ui -> OnboardingTour.openIfNeeded(ui, messageSource, localeService));
        }
    }

    private void updateSubNavBasedOnRoute() {
        if (getUI().isPresent()) {

            subNav.removeAll();
            String currentRoute = getUI().get().getInternals().getActiveViewLocation().getPath();
            String sectionKey = getSectionKey(currentRoute);
            currentTitle = sectionKey;
            mainButtons.forEach((key, button) -> button.selected(key.equals(sectionKey)));

            H4 title = new H4(getMessage(sectionKey));
            title.addClassName("secondary-navigation-title");
            subNav.add(title);

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
            } else if (currentRoute.startsWith("dashboard") || currentRoute.startsWith("domain")
                    || currentRoute.startsWith("search") || currentRoute.isEmpty()) {
                subNav.add(dashboardSubNavs);
            }
        }

    }

    private String getSectionKey(String currentRoute) {
        if (currentRoute.startsWith("settings")) {
            return "main_layout.settings";
        } else if (currentRoute.startsWith("account")) {
            return "main_layout.account";
        } else if (currentRoute.startsWith("management")) {
            return "main_layout.management";
        } else if (currentRoute.startsWith("incidents")) {
            return "main_layout.incidents";
        } else if (currentRoute.startsWith("automation")) {
            return "main_layout.automation";
        } else if (currentRoute.startsWith("inventory")) {
            return "main_layout.inventory";
        } else if (currentRoute.startsWith("reports")) {
            return "main_layout.reports";
        }
        return "main_layout.dashboard";
    }

    private MenuButton createMainButtonItem(String key, String label, Class<? extends Component> navigationTarget, String imgPath) {
        MenuButton button = new MenuButton(label, imgPath);
        mainButtons.put(key, button);

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
        toggle.addClassName("navigation-toggle");

        viewTitle = new H3();
        viewTitle.setWidthFull();
        viewTitle.addClassName("view-title");
        viewTitle.getElement().setAttribute("data-tour", "page-title");

        menuLayout = new HorizontalLayout();
        menuLayout.setWidthFull();
        menuLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        menuLayout.addClassName("view-actions");
        menuLayout.getStyle().setMarginRight("10px");

        addToNavbar(true, toggle, viewTitle, menuLayout);
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }

}
