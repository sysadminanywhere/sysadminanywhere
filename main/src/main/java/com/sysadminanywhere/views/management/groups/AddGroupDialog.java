package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
import com.sysadminanywhere.service.GroupsService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;

public class AddGroupDialog extends Dialog {

    private final GroupsService groupsService;

    public AddGroupDialog(GroupsService groupsService, Runnable onSearch) {
        this.groupsService = groupsService;

        setHeaderTitle("New group");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(groupsService.getLdapService());
        containerField.setValue(groupsService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtName = new TextField("Name");
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField("Description");
        formLayout.setColspan(txtDescription, 2);

        RadioButtonGroup<String> radioGroupScope = new RadioButtonGroup<>();
        radioGroupScope.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupScope.setLabel("Group scope");
        radioGroupScope.setItems("Global", "Local", "Universal");
        radioGroupScope.setValue("Global");

        RadioButtonGroup<String> radioGroupType = new RadioButtonGroup<>();
        radioGroupType.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupType.setLabel("Group type");
        radioGroupType.setItems("Security", "Distribution");
        radioGroupType.setValue("Security");

        formLayout.add(containerField, txtName, txtDescription, radioGroupScope, radioGroupType);
        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            GroupEntry group = new GroupEntry();
            group.setCn(txtName.getValue());
            group.setDescription(txtDescription.getValue());

            GroupScope scope = GroupScope.Global;

            switch (radioGroupScope.getValue()) {
                case "Global":
                    scope = GroupScope.Global;
                    break;
                case "Local":
                    scope = GroupScope.Local;
                    break;
                case "Universal":
                    scope = GroupScope.Universal;
                    break;
            }

            try {
                GroupEntry newGroup = groupsService.add(containerField.getValue(), group, scope, radioGroupType.getValue().equals("Security") ? true : false);

                onSearch.run();

                Notification notification = Notification.show("Computer added");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

}