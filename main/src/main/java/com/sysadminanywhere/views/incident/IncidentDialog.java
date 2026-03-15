package com.sysadminanywhere.views.incident;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.service.IncidentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

public class IncidentDialog extends Dialog {

    private final IncidentService incidentService;
    private final IncidentItem incident;

    public IncidentDialog(IncidentService incidentService, IncidentItem incident, Runnable onSearch) {
        this.incidentService = incidentService;
        this.incident = incident;

        setHeaderTitle("Incident");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setValue(incident.getName());
        txtName.setReadOnly(true);
        formLayout.setColspan(txtName, 2);

        TextField txtMachineName = new TextField("Machine name");
        txtMachineName.setValue(incident.getMachineName());
        txtName.setReadOnly(true);
        formLayout.setColspan(txtMachineName, 2);

        ComboBox<String> comboSeverity = new ComboBox<>("Severity");
        comboSeverity.setItems(List.of("Low", "Medium", "High", "Critical"));
        comboSeverity.setValue(incident.getSeverity().name());

        ComboBox<String> comboStatus = new ComboBox<>("Status");
        comboStatus.setItems(List.of("Open", "In Progress", "Resolved", "False Positive", "Closed"));
        comboStatus.setValue(incident.getStatus().name().replace("_", " "));

        TextField txtRecommendation = new TextField("Recommendation");
        txtRecommendation.setValue(incident.getRecommendation());
        txtRecommendation.setReadOnly(true);
        formLayout.setColspan(txtRecommendation, 2);

        formLayout.add(txtName, txtMachineName, comboSeverity, comboStatus, txtRecommendation);

        add(formLayout);

        Button saveButton = new Button("Save", e -> {
            try {
                incidentService.updateIncident(incident.getId(),
                        Severity.valueOf(comboSeverity.getValue().toUpperCase()),
                        IncidentStatus.valueOf(comboStatus.getValue().toUpperCase().replace(" ", "_")));

                onSearch.run();

                Notification notification = Notification.show("Incident updated");
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
