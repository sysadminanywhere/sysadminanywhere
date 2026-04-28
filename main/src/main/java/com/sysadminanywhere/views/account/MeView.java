package com.sysadminanywhere.views.account;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.control.HasMenu;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.security.AuthenticatedUser;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.UsersService;
import com.sysadminanywhere.views.management.users.UpdateUserDialog;
import com.sysadminanywhere.views.management.users.UpdateUserPhotoDialog;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.MessageSource;

import java.util.Base64;

@Route(value = "account/me")
@PermitAll
public class MeView extends VerticalLayout implements BeforeEnterObserver, HasMenu, HasDynamicTitle {

    private final UsersService usersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    UserEntry user;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    Avatar avatar = new Avatar();

    Binder<UserEntry> binder = new Binder<>(UserEntry.class);

    private final AuthenticatedUser authenticatedUser;

    public MeView(UsersService usersService, AuthenticatedUser authenticatedUser, MessageSource messageSource, LocaleService localeService) {
        this.usersService = usersService;
        this.authenticatedUser = authenticatedUser;
        this.messageSource = messageSource;
        this.localeService = localeService;

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText(getMessage("common.description"));
        lblName.setWidth("100%");

        lblDescription.setText(getMessage("common.description"));
        lblDescription.setWidth("100%");

        avatar.setThemeName("xlarge");

        add(verticalLayout);


        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        horizontalLayout.add(avatar, verticalLayout2);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtDisplayName = new TextField(getMessage("me_view.display_name"));
        txtDisplayName.setReadOnly(true);
        binder.bind(txtDisplayName, UserEntry::getDisplayName, null);

        TextField txtCompany = new TextField(getMessage("me_view.company"));
        txtCompany.setReadOnly(true);
        binder.bind(txtCompany, UserEntry::getCompany, null);

        TextField txtTitle = new TextField(getMessage("me_view.title_field"));
        txtTitle.setReadOnly(true);
        binder.bind(txtTitle, UserEntry::getTitle, null);

        TextField txtEmail = new TextField(getMessage("me_view.email"));
        txtEmail.setReadOnly(true);
        binder.bind(txtEmail, UserEntry::getEmailAddress, null);

        TextField txtMobilePhone = new TextField(getMessage("me_view.mobile_phone"));
        txtMobilePhone.setReadOnly(true);
        binder.bind(txtMobilePhone, UserEntry::getMobilePhone, null);

        TextField txtOfficePhone = new TextField(getMessage("me_view.office_phone"));
        txtOfficePhone.setReadOnly(true);
        binder.bind(txtOfficePhone, UserEntry::getOfficePhone, null);

        TextField txtHomePhone = new TextField(getMessage("me_view.home_phone"));
        txtHomePhone.setReadOnly(true);
        binder.bind(txtHomePhone, UserEntry::getHomePhone, null);

        formLayout.add(txtDisplayName, txtCompany, txtTitle, txtEmail, txtMobilePhone, txtOfficePhone, txtHomePhone);

        verticalLayout.add(formLayout);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        updateView();
    }

    private void updateView() {
        user = usersService.getByCN(authenticatedUser.get().get().getUsername());

        if (user != null) {
            binder.readBean(user);

            lblName.setText(user.getCn());
            lblDescription.setText(user.getDescription());

            avatar.setName(user.getName());
            avatar.setImage(null);
            if (user.getJpegPhoto() != null) {
                String base64 = Base64.getEncoder().encodeToString(user.getJpegPhoto());
                String dataUrl = "data:image/jpeg;base64," + base64;
                avatar.setImage(dataUrl);
            }
        }

    }

    private Runnable updateRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        };
    }

    private Dialog updateDialog() {
        return new UpdateUserDialog(usersService, user, messageSource, localeService, updateRunnable());
    }

    private Dialog updatePhotoDialog() {
        return new UpdateUserPhotoDialog(usersService, user, messageSource, localeService, updateRunnable());
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();
        MenuHelper.createIconItem(menuBar, "/icons/pencil.svg", getMessage("common.edit"), event -> {
            updateDialog().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/portrait.svg", getMessage("me_view.photo"), event -> {
            updatePhotoDialog().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/sign-out.svg", getMessage("me_view.sign_out"), event -> {
            authenticatedUser.logout();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        return menuBar;
    }

    public String getPageTitle() {
        return getMessage("me_view.title");
    }

}
