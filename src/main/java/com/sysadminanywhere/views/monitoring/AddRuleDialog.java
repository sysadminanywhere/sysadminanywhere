package com.sysadminanywhere.views.monitoring;

import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.ad.ComputerEntry;
import com.sysadminanywhere.service.MonitoringService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class AddRuleDialog extends Dialog {

    private final MonitoringService monitoringService;

    public AddRuleDialog(MonitoringService monitoringService, Runnable onSearch) {
        this.monitoringService = monitoringService;

        setHeaderTitle("New rule");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField("Description");
        formLayout.setColspan(txtDescription, 2);

        formLayout.add(txtName, txtDescription);
        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            try {
                monitoringService.addRule(new RuleEntity());

                onSearch.run();

                Notification notification = Notification.show("Rule added");
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