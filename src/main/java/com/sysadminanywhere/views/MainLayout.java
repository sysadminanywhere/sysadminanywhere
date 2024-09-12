package com.sysadminanywhere.views;

import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.views.home.HomeView;
import com.sysadminanywhere.views.management.computers.ComputersView;
import com.sysadminanywhere.views.management.contacts.ContactsView;
import com.sysadminanywhere.views.management.groups.GroupsView;
import com.sysadminanywhere.views.management.printers.PrintersView;
import com.sysadminanywhere.views.management.users.UsersView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Sysadmin Anywhere");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(false);
        verticalLayout.setPadding(false);

        verticalLayout.add(createNavigation());
        verticalLayout.add(createManagementNavigation());

        Scroller scroller = new Scroller(verticalLayout);

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.setWidth("100%");

        if (accessChecker.hasAccess(HomeView.class)) {
            nav.addItem(
                    new SideNavItem("Home", HomeView.class, LineAwesomeIcon.HOME_SOLID.create()));
        }

        return nav;
    }

    private SideNav createManagementNavigation() {
        SideNav nav = new SideNav("Management");
        nav.setCollapsible(true);
        nav.setWidth("100%");

        if (accessChecker.hasAccess(UsersView.class)) {
            nav.addItem(new SideNavItem("Users", UsersView.class,
                    LineAwesomeIcon.USER_SOLID.create()));
        }

        if (accessChecker.hasAccess(ComputersView.class)) {
            nav.addItem(new SideNavItem("Computers", ComputersView.class,
                    LineAwesomeIcon.DESKTOP_SOLID.create()));
        }

        if (accessChecker.hasAccess(GroupsView.class)) {
            nav.addItem(new SideNavItem("Groups", GroupsView.class,
                    LineAwesomeIcon.USERS_SOLID.create()));
        }

        if (accessChecker.hasAccess(PrintersView.class)) {
            nav.addItem(new SideNavItem("Printers", PrintersView.class,
                    LineAwesomeIcon.PRINT_SOLID.create()));
        }

        if (accessChecker.hasAccess(ContactsView.class)) {
            nav.addItem(new SideNavItem("Contacts", ContactsView.class,
                    LineAwesomeIcon.ADDRESS_CARD_SOLID.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<UserEntry> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            UserEntry user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getJpegPhoto()));
            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
