package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.control.MemberOf;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.UsersService;
import com.vaadin.flow.router.*;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.dialog.Dialog;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;

@RolesAllowed("ADMIN")
@Route(value = "management/users/:id?/details")
@Uses(Upload.class)
@Uses(Icon.class)
@Uses(ListBox.class)
public class UserDetailsView extends Div implements BeforeEnterObserver, MenuControl, HasDynamicTitle {

    private String id;
    private final UsersService usersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    UserEntry user;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    Avatar avatar = new Avatar();
    MemberOf memberOf;

    Binder<UserEntry> binder = new Binder<>(UserEntry.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        updateView();
    }

    private Runnable updateRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        };
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private void updateView() {
        if (id != null) {
            user = usersService.getByCN(id);

            if (user != null) {
                binder.readBean(user);

                lblName.setText(user.getCn());
                lblDescription.setText(user.getDescription());

                avatar.setName(user.getName());
                avatar.setImageResource(null);
                if (user.getJpegPhoto() != null) {
                    StreamResource resource = new StreamResource("profile-pic",
                            () -> new ByteArrayInputStream(user.getJpegPhoto()));
                    avatar.setImageResource(resource);
                }

                memberOf.update(usersService.getLdapService(), id);
            }
        }
    }

    public UserDetailsView(UsersService usersService, MessageSource messageSource, LocaleService localeService) {
        this.usersService = usersService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        memberOf = new MemberOf(messageSource, localeService);

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText(getMessage("common.name"));
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

        TextField txtDisplayName = new TextField(getMessage("update_user_dialog.display_name"));
        txtDisplayName.setReadOnly(true);
        binder.bind(txtDisplayName, UserEntry::getDisplayName, null);

        TextField txtCompany = new TextField(getMessage("update_user_dialog.company"));
        txtCompany.setReadOnly(true);
        binder.bind(txtCompany, UserEntry::getCompany, null);

        TextField txtTitle = new TextField(getMessage("update_user_dialog.job_title"));
        txtTitle.setReadOnly(true);
        binder.bind(txtTitle, UserEntry::getTitle, null);

        TextField txtEmail = new TextField(getMessage("update_user_dialog.email"));
        txtEmail.setReadOnly(true);
        binder.bind(txtEmail, UserEntry::getEmailAddress, null);

        TextField txtMobilePhone = new TextField(getMessage("update_user_dialog.mobile_phone"));
        txtMobilePhone.setReadOnly(true);
        binder.bind(txtMobilePhone, UserEntry::getMobilePhone, null);

        TextField txtOfficePhone = new TextField(getMessage("update_user_dialog.office_phone"));
        txtOfficePhone.setReadOnly(true);
        binder.bind(txtOfficePhone, UserEntry::getOfficePhone, null);

        TextField txtHomePhone = new TextField(getMessage("update_user_dialog.home_phone"));
        txtHomePhone.setReadOnly(true);
        binder.bind(txtHomePhone, UserEntry::getHomePhone, null);

        formLayout.add(txtDisplayName, txtCompany, txtTitle, txtEmail, txtMobilePhone, txtOfficePhone, txtHomePhone);

        verticalLayout.add(formLayout);

        Card card = new Card();
        card.add(memberOf);
        verticalLayout.add(card);
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getMessage("common.delete"));
        dialog.setText(getMessage("common.delete_user_confirmation"));

        dialog.setCancelable(true);

        dialog.setConfirmText(getMessage("common.delete"));
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            usersService.delete(user.getDistinguishedName());
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

    private Dialog updateDialog() {
        return new UpdateUserDialog(usersService, user, messageSource, localeService, updateRunnable());
    }

    private Dialog updatePhotoDialog() {
        return new UpdateUserPhotoDialog(usersService, user, messageSource, localeService, updateRunnable());
    }

    private Dialog resetPasswordForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getMessage("user_details_view.reset_password"));
        dialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        PasswordField txtPassword = new PasswordField(getMessage("user_details_view.new_password"));
        formLayout.setColspan(txtPassword, 2);

        formLayout.add(txtPassword);
        dialog.add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button(getMessage("common.save"), e -> {
            UserEntry entry = user;

            try {
                usersService.resetPassword(entry, txtPassword.getValue());
                updateView();

                Notification notification = Notification.show(getMessage("user_details_view.password_reset"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            dialog.close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button(getMessage("common.cancel"), e -> dialog.close());

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

    private Dialog optionsForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getMessage("user_details_view.user_options"));
        dialog.setWidth("600px");

        FormLayout formLayout = new FormLayout();

        VerticalLayout checkboxGroup = new VerticalLayout();
        formLayout.setColspan(checkboxGroup, 2);
        Checkbox chkUserMustChangePassword = new Checkbox(getMessage("user_details_view.user_must_change_password"));
        //chkUserMustChangePassword.setValue(user.isUserMustChangePassword());

        Checkbox chkUserCannotChangePassword = new Checkbox(getMessage("user_details_view.user_cannot_change_password"));
        //chkUserCannotChangePassword.setValue(user.isUserCannotChangePassword());

        Checkbox chkPasswordNeverExpires = new Checkbox(getMessage("user_details_view.password_never_expires"));
        //chkPasswordNeverExpires.setValue(user.isNeverExpires());

        Checkbox chkAccountDisabled = new Checkbox(getMessage("user_details_view.account_disabled"));
        //chkAccountDisabled.setValue(user.isDisabled());

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

        formLayout.add(checkboxGroup);
        dialog.add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button(getMessage("common.save"), e -> {
            UserEntry entry = user;

            try {
                usersService.changeUserAccountControl(
                        entry,
                        chkUserCannotChangePassword.getValue(),
                        chkPasswordNeverExpires.getValue(),
                        chkAccountDisabled.getValue(),
                        chkUserMustChangePassword.getValue());

                updateView();

                Notification notification = Notification.show(getMessage("user_details_view.options_changed"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            dialog.close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button(getMessage("common.cancel"), e -> dialog.close());

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();
        MenuHelper.createIconItem(menuBar, "/icons/pencil.svg", getMessage("user_details_view.update"), event -> {
            updateDialog().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/portrait.svg", getMessage("user_details_view.photo"), event -> {
            updatePhotoDialog().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/options.svg", getMessage("user_details_view.options"), event -> {
            optionsForm().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/password.svg", getMessage("user_details_view.reset_password_menu"), event -> {
            resetPasswordForm().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/trash.svg", getMessage("user_details_view.delete"), event -> {
            deleteDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        return menuBar;
    }

    public String getPageTitle() {
        return getMessage("user_details_view.title");
    }
}
