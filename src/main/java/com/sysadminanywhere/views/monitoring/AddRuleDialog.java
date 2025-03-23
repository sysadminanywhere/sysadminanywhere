package com.sysadminanywhere.views.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.monitoring.Rule;
import com.sysadminanywhere.service.MonitoringService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.Map;

public class AddRuleDialog extends Dialog {

    private final MonitoringService monitoringService;

    private VerticalLayout parametersLayout;

    public AddRuleDialog(MonitoringService monitoringService, Runnable onSearch) {
        this.monitoringService = monitoringService;

        setHeaderTitle("New rule");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ComboBox<Rule> cmbRules = getRuleImplementations(monitoringService.getRuleImplementations());
        formLayout.setColspan(cmbRules, 2);

        TextField txtName = new TextField("Name");
        txtName.setRequired(true);

        TextField txtDescription = new TextField("Description");

        Checkbox chkActive = new Checkbox("Active");
        chkActive.setValue(true);

        TextField txtCron = new TextField("Cron");
        txtCron.setValue("0 * * * * *");

        parametersLayout = new VerticalLayout();
        parametersLayout.setWidthFull();
        formLayout.setColspan(parametersLayout, 2);

        formLayout.add(cmbRules, txtName, txtDescription, parametersLayout, txtCron, chkActive);
        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                RuleEntity rule = new RuleEntity();
                rule.setName(txtName.getValue());
                rule.setDescription(txtDescription.getValue());
                rule.setType(cmbRules.getValue().getName());
                rule.setParameters("{}");
                rule.setActive(chkActive.getValue());
                rule.setCronExpression(txtCron.getValue());
                monitoringService.addRule(rule);

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

    private void setParameters(Map<String, String> parameters) {
        parametersLayout.removeAll();

        parametersLayout.add(new H4("Parameters"));

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            TextField txt = new TextField(key);
            txt.setWidthFull();
            parametersLayout.add(txt);
        }
    }

    private ComboBox<Rule> getRuleImplementations(List<Rule> rules) {
        ComboBox<Rule> comboBox = new ComboBox<>();

        comboBox.setItems(rules);
        comboBox.setItemLabelGenerator(Rule::getName);

        comboBox.addValueChangeListener(event -> {
            Rule selectedValue = event.getValue();
            setParameters(selectedValue.getParameters());
        });

        return comboBox;
    }

}