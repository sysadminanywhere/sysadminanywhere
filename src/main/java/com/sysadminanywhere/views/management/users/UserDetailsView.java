package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.service.UsersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.dialog.Dialog;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@PageTitle("User details")
@Route(value = "management/users/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Upload.class)
@Uses(Icon.class)
@Uses(ListBox.class)
public class UserDetailsView extends Div implements BeforeEnterObserver {

    private String id;
    private final UsersService usersService;
    UserEntry user;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    Avatar avatar = new Avatar();
    ListBox<String> listMemberOf = new ListBox<>();

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

    private void updateView() {
        if (id != null) {
            user = usersService.getByCN(id);

            if (user != null) {
                binder.readBean(user);

                lblName.setText(user.getCn());
                lblDescription.setText(user.getDescription());

                avatar.setName(user.getName());
                if (user.getJpegPhoto() != null) {
                    StreamResource resource = new StreamResource("profile-pic",
                            () -> new ByteArrayInputStream(user.getJpegPhoto()));
                    avatar.setImageResource(resource);
                }

                listMemberOf.clear();
                if (user.getMemberOf() != null) {
                    List<String> items = new ArrayList<>();
                    if (user.getPrimaryGroupId() != 0)
                        items.add(ADHelper.getPrimaryGroup(user.getPrimaryGroupId()));
                    for (String item : user.getMemberOf()) {
                        items.add(ADHelper.ExtractCN(item));
                    }
                    listMemberOf.setItems(items);
                }
            }
        }
    }

    public UserDetailsView(UsersService usersService) {
        this.usersService = usersService;

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText("Name");
        lblName.setWidth("100%");

        lblDescription.setText("Description");
        lblDescription.setWidth("100%");

        avatar.setThemeName("xlarge");

        add(verticalLayout);

        MenuBar menuBar = new MenuBar();
        MenuHelper.createIconItem(menuBar, VaadinIcon.EDIT, "Update", event -> {
            updateDialog().open();
        });
        MenuHelper.createIconItem(menuBar, VaadinIcon.USER, "Photo", event -> {
            updatePhotoDialog().open();
        });
        MenuHelper.createIconItem(menuBar, VaadinIcon.OPTIONS, "Options", event -> {
            optionsForm().open();
        });
        MenuHelper.createIconItem(menuBar, VaadinIcon.PASSWORD, "Reset password", event -> {
            resetPasswordForm().open();
        });
        MenuHelper.createIconItem(menuBar, VaadinIcon.TRASH, "Delete", event -> {
            deleteDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout horizontalLayout2 = new HorizontalLayout(menuBar);
        horizontalLayout2.setWidthFull();
        horizontalLayout2.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        horizontalLayout.add(avatar, verticalLayout2, horizontalLayout2);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtDisplayName = new TextField("Display name");
        txtDisplayName.setReadOnly(true);
        binder.bind(txtDisplayName, UserEntry::getDisplayName, null);

        TextField txtCompany = new TextField("Company");
        txtCompany.setReadOnly(true);
        binder.bind(txtCompany, UserEntry::getCompany, null);

        TextField txtTitle = new TextField("Title");
        txtTitle.setReadOnly(true);
        binder.bind(txtTitle, UserEntry::getTitle, null);

        TextField txtEmail = new TextField("Email");
        txtEmail.setReadOnly(true);
        binder.bind(txtEmail, UserEntry::getEmailAddress, null);

        TextField txtMobilePhone = new TextField("Mobile phone");
        txtMobilePhone.setReadOnly(true);
        binder.bind(txtMobilePhone, UserEntry::getMobilePhone, null);

        TextField txtOfficePhone = new TextField("Office phone");
        txtOfficePhone.setReadOnly(true);
        binder.bind(txtOfficePhone, UserEntry::getOfficePhone, null);

        TextField txtHomePhone = new TextField("Home phone");
        txtHomePhone.setReadOnly(true);
        binder.bind(txtHomePhone, UserEntry::getOfficePhone, null);

        formLayout.add(txtDisplayName, txtCompany, txtTitle, txtEmail, txtMobilePhone, txtOfficePhone, txtHomePhone);

        verticalLayout.add(formLayout);

        verticalLayout.add(new Hr(), new H5("Member of"), listMemberOf);
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure you want to permanently delete this user?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            usersService.delete(user.getDistinguishedName());
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

    private Dialog updateDialog() {
        return new UpdateUserDialog(usersService, user, updateRunnable());
    }

    private Dialog updatePhotoDialog() {
        return new UpdateUserPhotoDialog(usersService, user, updateRunnable());
    }

    private Dialog resetPasswordForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reset password");
        dialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        PasswordField txtPassword = new PasswordField("New password");
        formLayout.setColspan(txtPassword, 2);

        formLayout.add(txtPassword);
        dialog.add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
            UserEntry entry = user;

            try {
                usersService.resetPassword(entry, txtPassword.getValue());
                updateView();

                Notification notification = Notification.show("Password reset");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            dialog.close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

    private Dialog optionsForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("User Options");
        dialog.setWidth("600px");

        FormLayout formLayout = new FormLayout();

        VerticalLayout checkboxGroup = new VerticalLayout();
        formLayout.setColspan(checkboxGroup, 2);
        Checkbox chkUserMustChangePassword = new Checkbox("User must change password at next logon");
        chkUserMustChangePassword.setValue(user.isUserMustChangePassword());

        Checkbox chkUserCannotChangePassword = new Checkbox("User cannot change password");
        chkUserCannotChangePassword.setValue(user.isUserCannotChangePassword());

        Checkbox chkPasswordNeverExpires = new Checkbox("Password never expires");
        chkPasswordNeverExpires.setValue(user.isNeverExpires());

        Checkbox chkAccountDisabled = new Checkbox("Account disabled");
        chkAccountDisabled.setValue(user.isDisabled());

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

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
            UserEntry entry = user;

            try {
                usersService.ChangeUserAccountControl(
                        entry,
                        chkUserCannotChangePassword.getValue(),
                        chkPasswordNeverExpires.getValue(),
                        chkAccountDisabled.getValue());

                updateView();

                Notification notification = Notification.show("Options changed");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            dialog.close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

}