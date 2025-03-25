package com.sysadminanywhere.views.monitoring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.model.monitoring.Rule;
import com.sysadminanywhere.service.MonitoringService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

public class UpdateRuleDialog extends Dialog {

    private final MonitoringService monitoringService;

    @SneakyThrows
    public UpdateRuleDialog(MonitoringService monitoringService, RuleEntity rule, Runnable onSearch) {
        this.monitoringService = monitoringService;
        ObjectMapper objectMapper = new ObjectMapper();

        setHeaderTitle("Update rule");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setValue(rule.getName());
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(rule.getDescription());
        formLayout.setColspan(txtDescription, 2);

        TextField txtCron = new TextField("Cron");
        txtCron.setValue(rule.getCronExpression());
        formLayout.setColspan(txtDescription, 2);

        Checkbox chkActive = new Checkbox("Active");
        chkActive.setValue(rule.isActive());

        formLayout.add(txtName, txtDescription, txtCron, chkActive);

        Rule ruleInstance = monitoringService.createRuleInstance(rule.getType());

        for (Component item : ruleInstance.getControls(objectMapper.readValue(rule.getParameters(), new TypeReference<Map<String, String>>() {
        }))) {
            formLayout.add(item);
        }

        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            try {
                rule.setName(txtName.getValue());
                rule.setDescription(txtDescription.getValue());

                rule.setParameters(objectMapper.writeValueAsString(ruleInstance.getParameters()));

                rule.setActive(chkActive.getValue());
                rule.setCronExpression(txtCron.getValue());
                monitoringService.updateRule(rule);

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

        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

}