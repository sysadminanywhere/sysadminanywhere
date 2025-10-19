package com.sysadminanywhere.views.monitoring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.control.CronEditor;
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
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import lombok.SneakyThrows;

import java.util.Map;

public class UpdateRuleDialog extends Dialog {

    private final MonitoringService monitoringService;

    @SneakyThrows
    public UpdateRuleDialog(MonitoringService monitoringService, RuleEntity rule, Runnable onSearch) {
        this.monitoringService = monitoringService;
        ObjectMapper objectMapper = new ObjectMapper();

        setHeaderTitle("Update rule");
        setWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setValue(rule.getName());
        txtName.setRequired(true);
        formLayout.setColspan(txtName, 2);

        TextField txtDescription = new TextField("Description");
        txtDescription.setValue(rule.getDescription());
        formLayout.setColspan(txtDescription, 2);

        CronEditor cronEditor = new CronEditor("Cron");
        cronEditor.getStyle().setMarginTop("10px");
        cronEditor.getStyle().setMarginBottom("10px");
        cronEditor.setValue(rule.getCronExpression());
        formLayout.setColspan(cronEditor, 2);

        Checkbox chkActive = new Checkbox("Active");
        chkActive.setValue(rule.isActive());

        formLayout.add(txtName, txtDescription, cronEditor, chkActive);

        Rule ruleInstance = monitoringService.createRuleInstance(rule.getType());

        FormLayout formParameters = new FormLayout();

        for (Component item : ruleInstance.getControls(objectMapper.readValue(rule.getParameters(), new TypeReference<Map<String, String>>() {
        }))) {
            formParameters.add(item);
        }

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Rule", formLayout);
        tabSheet.add("Parameters", formParameters);

        add(tabSheet);

        Button saveButton = new Button("Save", e -> {
            try {
                rule.setName(txtName.getValue());
                rule.setDescription(txtDescription.getValue());

                rule.setParameters(objectMapper.writeValueAsString(ruleInstance.getParameters()));

                rule.setActive(chkActive.getValue());
                rule.setCronExpression(cronEditor.getValue());
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