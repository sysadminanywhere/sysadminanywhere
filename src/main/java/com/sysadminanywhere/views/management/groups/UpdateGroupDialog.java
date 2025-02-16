package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.service.GroupsService;
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

    public UpdateGroupDialog(GroupsService groupsService, GroupEntry groupEntry, Runnable updateView) {
        this.groupsService = groupsService;
        this.group = groupEntry;

        setHeaderTitle("Updating user");
        setWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(group.getDescription());
        formLayout.setColspan(txtDescription, 2);

        formLayout.add(txtDescription);
        add(formLayout);

        Button saveButton = new com.vaadin.flow.component.button.Button("Save", e -> {
            GroupEntry entry = group;
            entry.setDescription(txtDescription.getValue());

            try {
                groupsService.update(entry);
                updateView.run();

                Notification notification = Notification.show("Group updated");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        com.vaadin.flow.component.button.Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

}