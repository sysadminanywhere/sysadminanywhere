package com.sysadminanywhere.views.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.entity.RuleEntity;
import com.sysadminanywhere.model.monitoring.Rule;
import com.sysadminanywhere.service.MonitoringService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;

public class SelectRuleDialog extends Dialog {

    private final MonitoringService monitoringService;
    Rule selectedRule;
    private TextArea textArea = new TextArea();

    public SelectRuleDialog(MonitoringService monitoringService, Runnable onSearch) {
        this.monitoringService = monitoringService;

        setHeaderTitle("Select rule");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ListBox<Rule> rules = new ListBox<>();
        rules.setItems(monitoringService.getRuleImplementations());
        rules.setItemLabelGenerator(Rule::getName);

        textArea.setWidthFull();
        textArea.setLabel("Description");
        textArea.setReadOnly(true);

        formLayout.add(rules, textArea);
        add(formLayout);

        Button nextButton = new Button("Next", e -> {
            new AddRuleDialog(monitoringService, selectedRule, onSearch).open();
            close();
        });

        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setEnabled(false);

        rules.addValueChangeListener(event -> {
            selectedRule = event.getValue();
            textArea.setValue(selectedRule.getDescription());
            nextButton.setEnabled(true);
        });

        Button cancelButton = new Button("Cancel", e -> close());

        getFooter().add(cancelButton);
        getFooter().add(nextButton);

    }

}