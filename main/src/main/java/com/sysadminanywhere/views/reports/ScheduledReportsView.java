package com.sysadminanywhere.views.reports;

import com.sysadminanywhere.model.ScheduledReportConfig;
import com.sysadminanywhere.model.ScheduledReportRun;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.ScheduledReportService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RolesAllowed("ADMIN")
@Route(value = "reports/scheduled")
public class ScheduledReportsView extends VerticalLayout implements HasDynamicTitle {
    private final ScheduledReportService service;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Grid<ScheduledReportConfig> configs = new Grid<>();
    private final Grid<ScheduledReportRun> runs = new Grid<>();
    private ScheduledReportConfig selected;

    public ScheduledReportsView(ScheduledReportService service, MessageSource messageSource, LocaleService localeService) {
        this.service = service;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassName("review-page");
        setPadding(false);
        setSpacing(true);

        Button add = new Button(message("scheduled_reports_view.new"), event -> edit(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button refresh = new Button(message("common.refresh"), event -> refresh());
        HorizontalLayout toolbar = new HorizontalLayout(add, refresh);
        toolbar.addClassName("review-toolbar");
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.CENTER);

        configureConfigsGrid();
        configureRunsGrid();
        H3 runsHeading = new H3(message("scheduled_reports_view.runs"));
        runsHeading.addClassName("review-section-title");
        add(toolbar, configs, runsHeading, runs);
        configs.setHeight("260px");
        runs.setHeight("220px");
        refresh();
    }

    private void configureConfigsGrid() {
        configs.addColumn(ScheduledReportConfig::getEntry).setHeader(message("scheduled_reports_view.entry")).setAutoWidth(true);
        configs.addColumn(ScheduledReportConfig::getReportId).setHeader(message("scheduled_reports_view.report")).setAutoWidth(true);
        configs.addColumn(ScheduledReportConfig::getFrequency).setHeader(message("scheduled_reports_view.frequency")).setAutoWidth(true);
        configs.addColumn(item -> String.format("%02d:%02d", item.getHour(), item.getMinute()))
                .setHeader(message("scheduled_reports_view.time")).setAutoWidth(true);
        configs.addColumn(ScheduledReportConfig::getFormat).setHeader(message("scheduled_reports_view.format")).setAutoWidth(true);
        configs.addColumn(item -> item.isEnabled() ? message("common.yes") : message("common.no"))
                .setHeader(message("scheduled_reports_view.enabled")).setAutoWidth(true);
        configs.addColumn(ScheduledReportConfig::getRecipients).setHeader(message("scheduled_reports_view.recipients"))
                .setFlexGrow(1);
        configs.addItemClickListener(event -> {
            selected = event.getItem();
            edit(selected);
        });
        configs.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        configs.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }

    private void configureRunsGrid() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        runs.addColumn(item -> item.getStartedAt() == null ? "" : item.getStartedAt().format(formatter))
                .setHeader(message("scheduled_reports_view.started")).setAutoWidth(true);
        runs.addColumn(ScheduledReportRun::getStatus).setHeader(message("scheduled_reports_view.status")).setAutoWidth(true);
        runs.addColumn(ScheduledReportRun::getFilePath).setHeader(message("scheduled_reports_view.file")).setFlexGrow(1);
        runs.addColumn(item -> item.getError() == null ? "" : item.getError())
                .setHeader(message("scheduled_reports_view.error")).setFlexGrow(1);
        runs.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        runs.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
    }

    private void edit(ScheduledReportConfig source) {
        ScheduledReportConfig value = source == null ? new ScheduledReportConfig(null, true, "users", "", "DAILY", 8, 0, "CSV", "") :
                new ScheduledReportConfig(source.getId(), source.isEnabled(), source.getEntry(), source.getReportId(), source.getFrequency(),
                        source.getHour(), source.getMinute(), source.getFormat(), source.getRecipients());
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(source == null ? message("scheduled_reports_view.new") : message("scheduled_reports_view.edit"));
        dialog.setWidth("480px");
        dialog.setMaxWidth("calc(100vw - 32px)");

        ComboBox<String> entry = new ComboBox<>(message("scheduled_reports_view.entry"));
        entry.setItems("users", "computers", "groups", "printers", "contacts");
        entry.setValue(value.getEntry());
        TextField report = new TextField(message("scheduled_reports_view.report"));
        report.setValue(value.getReportId() == null ? "" : value.getReportId());
        ComboBox<String> frequency = new ComboBox<>(message("scheduled_reports_view.frequency"));
        frequency.setItems("DAILY", "WEEKLY");
        frequency.setValue(value.getFrequency());
        IntegerField hour = new IntegerField(message("scheduled_reports_view.hour"));
        hour.setMin(0); hour.setMax(23); hour.setValue(value.getHour());
        IntegerField minute = new IntegerField(message("scheduled_reports_view.minute"));
        minute.setMin(0); minute.setMax(59); minute.setValue(value.getMinute());
        ComboBox<String> format = new ComboBox<>(message("scheduled_reports_view.format"));
        format.setItems("PDF", "CSV"); format.setValue(value.getFormat());
        TextField recipients = new TextField(message("scheduled_reports_view.recipients"));
        recipients.setWidthFull(); recipients.setValue(value.getRecipients() == null ? "" : value.getRecipients());
        recipients.setHelperText("email1@example.com, email2@example.com");
        Checkbox enabled = new Checkbox(message("scheduled_reports_view.enabled"), value.isEnabled());

        HorizontalLayout time = new HorizontalLayout(hour, minute);
        time.setWidthFull();
        VerticalLayout form = new VerticalLayout(entry, report, frequency, time, format, recipients, enabled);
        form.setPadding(false);
        form.setWidthFull();
        dialog.add(form);
        Button save = new Button(message("scheduled_reports_view.save"), event -> {
            try {
                value.setEntry(entry.getValue()); value.setReportId(report.getValue()); value.setFrequency(frequency.getValue());
                value.setHour(hour.getValue() == null ? 0 : hour.getValue()); value.setMinute(minute.getValue() == null ? 0 : minute.getValue());
                value.setFormat(format.getValue()); value.setRecipients(recipients.getValue()); value.setEnabled(enabled.getValue());
                service.save(value); dialog.close(); refresh(); notifySuccess(message("scheduled_reports_view.saved"));
            } catch (Exception exception) { notifyError(exception.getMessage()); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(save);
        if (source != null) {
            Button run = new Button(message("scheduled_reports_view.run"), event -> {
                try { service.runNow(source.getId()); dialog.close(); refresh(); notifySuccess(message("scheduled_reports_view.completed")); }
                catch (Exception exception) { notifyError(exception.getMessage()); }
            });
            dialog.getFooter().add(run);
            Button delete = new Button(message("scheduled_reports_view.delete"), event -> confirmDelete(source, dialog));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            dialog.getFooter().add(delete);
        }
        dialog.open();
    }

    private void confirmDelete(ScheduledReportConfig config, Dialog editor) {
        ConfirmDialog confirmation = new ConfirmDialog();
        confirmation.setHeader(message("scheduled_reports_view.delete"));
        confirmation.setText(message("scheduled_reports_view.delete_confirm"));
        confirmation.setCancelable(true);
        confirmation.setConfirmText(message("scheduled_reports_view.delete"));
        confirmation.addConfirmListener(event -> { service.delete(config.getId()); editor.close(); refresh(); });
        confirmation.open();
    }

    private void refresh() { configs.setItems(service.list()); runs.setItems(service.runs()); }
    private void notifySuccess(String text) { Notification.show(text); }
    private void notifyError(String text) { Notification notification = Notification.show(text == null ? message("common.error") : text); notification.addThemeVariants(NotificationVariant.LUMO_ERROR); }
    private String message(String key) { return messageSource.getMessage(key, null, localeService.getCurrentLocale()); }
    @Override public String getPageTitle() { return message("scheduled_reports_view.title"); }
}
