package com.sysadminanywhere.views.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

public class IncidentDialog extends Dialog {

    private final IncidentItem incident;

    public IncidentDialog(IncidentItem incident) {
        this.incident = incident;

        setHeaderTitle("Incident");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField("Name");
        txtName.setValue(incident.getName());
        formLayout.setColspan(txtName, 2);

        TextField txtMachineName = new TextField("Machine name");
        txtMachineName.setValue(incident.getMachineName());
        formLayout.setColspan(txtMachineName, 2);

        TextField txtSeverity = new TextField("Severity");
        txtSeverity.setValue(incident.getSeverity().name());
        formLayout.setColspan(txtSeverity, 2);

        TextField txtStatus = new TextField("Status");
        txtStatus.setValue(incident.getStatus().name());
        formLayout.setColspan(txtStatus, 2);

        TextField txtRecommendation = new TextField("Recommendation");
        txtRecommendation.setValue(incident.getRecommendation());
        formLayout.setColspan(txtRecommendation, 2);

        formLayout.add(txtName, txtMachineName, txtSeverity, txtStatus, txtRecommendation);

        add(formLayout);

        Button saveButton = new Button("Save", e -> {

        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }
}
