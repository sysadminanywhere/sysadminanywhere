package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.service.GroupsService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;

public class UpdateGroupDialog extends Dialog {

    private final GroupsService groupsService;
    private final GroupEntry group;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public UpdateGroupDialog(GroupsService groupsService, GroupEntry groupEntry, MessageSource messageSource, LocaleService localeService, Runnable updateView) {
        this.groupsService = groupsService;
        this.group = groupEntry;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("update_group_dialog.title"));
        setWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtDescription = new TextField(getMessage("update_group_dialog.description"));
        txtDescription.setValue(group.getDescription());
        formLayout.setColspan(txtDescription, 2);

        formLayout.add(txtDescription);
        add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button(getMessage("common.save"), e -> {
            GroupEntry entry = group;
            entry.setDescription(txtDescription.getValue());

            try {
                groupsService.update(entry);
                updateView.run();

                Notification notification = Notification.show(getMessage("update_group_dialog.group_updated"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button(getMessage("common.cancel"), e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

}
