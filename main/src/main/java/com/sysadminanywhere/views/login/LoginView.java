package com.sysadminanywhere.views.login;

import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private final LdapService ldapService;

    public LoginView(AuthenticatedUser authenticatedUser, LdapService ldapService) {
        this.authenticatedUser = authenticatedUser;
        this.ldapService = ldapService;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Sysadmin Anywhere");
        i18n.getHeader().setDescription("Login using Active Directory account");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (ldapService.getConnection().isConnected()) {
            if (authenticatedUser.get().isPresent()) {
                // Already logged in
                setOpened(false);
                event.forwardTo("");
            }
        } else {
            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Error");
            dialog.setText("Active Directory server is unreachable or configuration is invalid.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Documentation");
            dialog.addConfirmListener(e -> {
                UI.getCurrent().getPage().executeJs(
                        "window.open($0, '_blank')",
                        "https://github.com/sysadminanywhere/sysadminanywhere/wiki/Errors");
            });

            dialog.open();
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }

}