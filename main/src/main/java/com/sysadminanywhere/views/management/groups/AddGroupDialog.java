package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.service.GroupsService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
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
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public AddGroupDialog(GroupsService groupsService, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.groupsService = groupsService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("add_group_dialog.title"));
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(groupsService.getLdapService(), messageSource, localeService);
        containerField.setValue(groupsService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        TextField txtName = new TextField(getMessage("add_group_dialog.name"));
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField(getMessage("add_group_dialog.description"));
        formLayout.setColspan(txtDescription, 2);

        RadioButtonGroup<String> radioGroupScope = new RadioButtonGroup<>();
        radioGroupScope.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupScope.setLabel(getMessage("add_group_dialog.group_scope"));
        radioGroupScope.setItems("Global", "Local", "Universal");
        radioGroupScope.setValue("Global");

        RadioButtonGroup<String> radioGroupType = new RadioButtonGroup<>();
        radioGroupType.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroupType.setLabel(getMessage("add_group_dialog.group_type"));
        radioGroupType.setItems("Security", "Distribution");
        radioGroupType.setValue("Security");

        formLayout.add(containerField, txtName, txtDescription, radioGroupScope, radioGroupType);
        add(formLayout);

        Button saveButton = new Button(getMessage("common.save"), e -> {
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
                GroupEntry newGroup = groupsService.add(containerField.getValue(), group, scope, radioGroupType.getValue().equals("Security"));

                onSearch.run();

                Notification notification = Notification.show(getMessage("add_group_dialog.group_added"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button(getMessage("common.cancel"), e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

}
