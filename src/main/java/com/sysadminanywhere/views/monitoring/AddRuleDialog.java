package com.sysadminanywhere.views.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.RuleEntity;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.HashMap;

public class AddRuleDialog extends Dialog {

    private final MonitoringService monitoringService;

    private VerticalLayout parametersLayout;

    public AddRuleDialog(MonitoringService monitoringService, Rule rule, Runnable onSearch) {
        this.monitoringService = monitoringService;

        setHeaderTitle("New rule");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setValue(rule.getName());
        txtName.setRequired(true);

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(rule.getDescription());

        TextField txtCron = new TextField("Cron");
        txtCron.setValue("0 * * * * *");

        Checkbox chkActive = new Checkbox("Active");
        chkActive.setValue(true);

        formLayout.add(txtName, txtDescription, txtCron, chkActive);

        for(Component item : rule.getControls(new HashMap<>())) {
            formLayout.add(item);
        }

        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                RuleEntity ruleEntity = new RuleEntity();
                ruleEntity.setName(txtName.getValue());
                ruleEntity.setDescription(txtDescription.getValue());
                ruleEntity.setType(rule.getType());

                ruleEntity.setParameters(objectMapper.writeValueAsString(rule.getParameters()));

                ruleEntity.setActive(chkActive.getValue());
                ruleEntity.setCronExpression(txtCron.getValue());
                monitoringService.addRule(ruleEntity);

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