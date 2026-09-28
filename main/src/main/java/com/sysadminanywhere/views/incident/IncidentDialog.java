package com.sysadminanywhere.views.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.service.IncidentService;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.Utils;
import com.sysadminanywhere.security.UiAuthorization;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;

import java.time.LocalDateTime;

public class IncidentDialog extends Dialog {

    private final IncidentService incidentService;
    private final IncidentItem incident;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public IncidentDialog(IncidentService incidentService, IncidentItem incident, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.incidentService = incidentService;
        this.incident = incident;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("incident_dialog.title"));
        setWidth("min(800px, calc(100vw - 32px))");
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField(getMessage("incident_dialog.name"));
        txtName.setValue(value(incident.getName()));
        txtName.setReadOnly(true);
        formLayout.setColspan(txtName, 2);

        TextField txtCreatedAt = new TextField(getMessage("incident_dialog.created_at"));
        txtCreatedAt.setValue(formatDate(incident.getCreatedAt()));
        txtCreatedAt.setReadOnly(true);

        TextField txtMachineName = new TextField(getMessage("incident_dialog.machine_name"));
        txtMachineName.setValue(value(incident.getMachineName()));
        txtMachineName.setReadOnly(true);

        ComboBox<Severity> comboSeverity = new ComboBox<>(getMessage("incident_dialog.severity"));
        comboSeverity.setItems(Severity.values());
        comboSeverity.setItemLabelGenerator(item -> getMessage("incidents_view." + item.name().toLowerCase()));
        comboSeverity.setValue(incident.getSeverity());
        comboSeverity.setReadOnly(!UiAuthorization.isAdmin());

        ComboBox<IncidentStatus> comboStatus = new ComboBox<>(getMessage("incident_dialog.status"));
        comboStatus.setItems(IncidentStatus.values());
        comboStatus.setItemLabelGenerator(item -> getMessage("incidents_view." + item.name().toLowerCase()));
        comboStatus.setValue(incident.getStatus());
        comboStatus.setReadOnly(!UiAuthorization.isAdmin());

        TextField txtFirstEventTime = new TextField(getMessage("incident_dialog.first_event_time"));
        txtFirstEventTime.setValue(formatDate(incident.getFirstEventTime()));
        txtFirstEventTime.setReadOnly(true);

        TextField txtLastEventTime = new TextField(getMessage("incident_dialog.last_event_time"));
        txtLastEventTime.setValue(formatDate(incident.getLastEventTime()));
        txtLastEventTime.setReadOnly(true);

        TextField txtRecommendation = new TextField(getMessage("incident_dialog.recommendation"));
        txtRecommendation.setValue(value(incident.getRecommendation()));
        txtRecommendation.setReadOnly(true);
        formLayout.setColspan(txtRecommendation, 2);

        TextField txtAffectedUser = new TextField(getMessage("incident_dialog.affected_user"));
        txtAffectedUser.setValue(value(incident.getAffectedUser()));
        txtAffectedUser.setReadOnly(true);

        TextArea txtContext = new TextArea(getMessage("incident_dialog.context"));
        txtContext.setValue(value(incident.getContext()));
        txtContext.setReadOnly(true);
        txtContext.setMinHeight("120px");
        formLayout.setColspan(txtContext, 2);

        TextField txtEventCount = new TextField(getMessage("incident_dialog.event_count"));
        txtEventCount.setValue(String.valueOf(incident.getEventCount()));
        txtEventCount.setReadOnly(true);

        TextField txtUpdatedAt = new TextField(getMessage("incident_dialog.updated_at"));
        txtUpdatedAt.setValue(formatDate(incident.getUpdatedAt()));
        txtUpdatedAt.setReadOnly(true);

        formLayout.add(txtName, txtRecommendation, txtContext, txtCreatedAt, txtMachineName, txtAffectedUser, txtFirstEventTime, txtLastEventTime, txtEventCount, txtUpdatedAt, comboSeverity, comboStatus);

        add(formLayout);

        com.vaadin.flow.component.html.Span saveError = new com.vaadin.flow.component.html.Span();
        saveError.addClassName("dialog-save-error");
        saveError.setVisible(false);
        add(saveError);

        Button saveButton = new Button(getMessage("common.save"), e -> {
            try {
                Severity severity = comboSeverity.getValue();
                IncidentStatus status = comboStatus.getValue();

                if (status == IncidentStatus.CLOSED) {
                    incidentService.closeIncident(incident.getId());

                    onSearch.run();

                    Notification notification = Notification.show(getMessage("incident_dialog.incident_closed"));
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    incidentService.updateIncident(incident.getId(), severity, status);

                    onSearch.run();

                    Notification notification = Notification.show(getMessage("incident_dialog.incident_updated"));
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            } catch (Exception ex) {
                saveError.setText(ex.getMessage() == null ? getMessage("common.error") : ex.getMessage());
                saveError.setVisible(true);
                return;
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button(getMessage("common.cancel"), e -> close());
        getFooter().add(cancelButton);
        if (UiAuthorization.isAdmin()) getFooter().add(saveButton);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    private static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : Utils.formatLocalDateTime(dateTime);
    }

}
