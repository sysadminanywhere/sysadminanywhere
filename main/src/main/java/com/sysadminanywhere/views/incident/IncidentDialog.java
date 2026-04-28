package com.sysadminanywhere.views.incident;

import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.service.IncidentService;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.TicketService;
import com.sysadminanywhere.service.Utils;
import org.springframework.context.MessageSource;
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
    private final TicketService ticketService;
    private final IncidentItem incident;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public IncidentDialog(IncidentService incidentService, TicketService ticketService, IncidentItem incident, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.incidentService = incidentService;
        this.ticketService = ticketService;
        this.incident = incident;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setHeaderTitle(getMessage("incident_dialog.title"));
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtName = new TextField(getMessage("incident_dialog.name"));
        txtName.setValue(incident.getName());
        txtName.setReadOnly(true);
        formLayout.setColspan(txtName, 2);

        TextField txtCreatedAt = new TextField(getMessage("incident_dialog.created_at"));
        txtCreatedAt.setValue(Utils.formatLocalDateTime(incident.getCreatedAt()));
        txtCreatedAt.setReadOnly(true);

        TextField txtMachineName = new TextField(getMessage("incident_dialog.machine_name"));
        txtMachineName.setValue(incident.getMachineName());
        txtMachineName.setReadOnly(true);

        ComboBox<String> comboSeverity = new ComboBox<>(getMessage("incident_dialog.severity"));
        comboSeverity.setItems(List.of("Low", "Medium", "High", "Critical"));
        comboSeverity.setValue(incident.getSeverity().name());

        ComboBox<String> comboStatus = new ComboBox<>(getMessage("incident_dialog.status"));
        comboStatus.setItems(List.of("Open", "In Progress", "Resolved", "False Positive", "Closed"));
        comboStatus.setValue(incident.getStatus().name().replace("_", " "));

        TextField txtFirstEventTime = new TextField(getMessage("incident_dialog.first_event_time"));
        txtFirstEventTime.setValue(Utils.formatLocalDateTime(incident.getFirstEventTime()));
        txtFirstEventTime.setReadOnly(true);

        TextField txtLastEventTime = new TextField(getMessage("incident_dialog.last_event_time"));
        txtLastEventTime.setValue(Utils.formatLocalDateTime(incident.getLastEventTime()));
        txtLastEventTime.setReadOnly(true);

        TextField txtRecommendation = new TextField(getMessage("incident_dialog.recommendation"));
        txtRecommendation.setValue(incident.getRecommendation());
        txtRecommendation.setReadOnly(true);
        formLayout.setColspan(txtRecommendation, 2);

        TextField txtEventCount = new TextField(getMessage("incident_dialog.event_count"));
        txtEventCount.setValue(String.valueOf(incident.getEventCount()));
        txtEventCount.setReadOnly(true);

        TextField txtUpdatedAt = new TextField(getMessage("incident_dialog.updated_at"));
        txtUpdatedAt.setValue(Utils.formatLocalDateTime(incident.getUpdatedAt()));
        txtUpdatedAt.setReadOnly(true);

        formLayout.add(txtName, txtRecommendation, txtCreatedAt, txtMachineName, txtFirstEventTime, txtLastEventTime, txtEventCount, txtUpdatedAt, comboSeverity, comboStatus);

        add(formLayout);

        Button saveButton = new Button(getMessage("common.save"), e -> {
            try {
                Severity severity = Severity.valueOf(comboSeverity.getValue().toUpperCase());
                IncidentStatus status = IncidentStatus.valueOf(comboStatus.getValue().toUpperCase().replace(" ", "_"));

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
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button createTicketButton = new Button(getMessage("incident_dialog.create_ticket"), e -> {
            try {
                String title = incident.getName();
                String description = incident.getRecommendation() != null ? incident.getRecommendation() : "";
                String requester = "System";
                
                ticketService.createTicketFromIncident(incident.getId(), title, description, requester);
                
                Notification notification = Notification.show(getMessage("incident_dialog.ticket_created"));
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification notification = Notification.show(getMessage("common.error") + ": " + ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        createTicketButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button cancelButton = new Button(getMessage("common.cancel"), e -> close());
        getFooter().add(cancelButton);
        getFooter().add(createTicketButton);
        getFooter().add(saveButton);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

}
