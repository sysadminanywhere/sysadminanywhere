package com.sysadminanywhere.views.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.service.MonitoringService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;

public class UpdateRuleDialog extends Dialog {

    private final MonitoringService monitoringService;

    public UpdateRuleDialog(MonitoringService monitoringService, RuleEntity rule, Runnable onSearch) {
        this.monitoringService = monitoringService;

        setHeaderTitle("Update rule");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField("Description");
        formLayout.setColspan(txtDescription, 2);

        TextField txtCron = new TextField("Cron");
        txtCron.setValue("0 * * * * *");
        formLayout.setColspan(txtDescription, 2);

        formLayout.add(txtName, txtDescription, txtCron);
        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                rule.setName(txtName.getValue());
                rule.setDescription(txtDescription.getValue());

                rule.setParameters("{}");

                rule.setActive(true);
                rule.setCronExpression(txtCron.getValue());
                monitoringService.addRule(rule);

                onSearch.run();

                Notification notification = Notification.show("Rule updated");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> close());

        Button deleteButton = new Button("Delete", e -> {
            try {
                monitoringService.deleteRule(rule.getId());

                onSearch.run();

                Notification notification = Notification.show("Rule deleted");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        getFooter().add(deleteButton);
        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

}