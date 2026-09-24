package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.control.SpreadsheetImport;
import com.sysadminanywhere.service.GroupsService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportGroupDialog extends Dialog {
    private final GroupsService groupsService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Runnable onSearch;
    private final Grid<PreviewRow> previewGrid = new Grid<>();
    private final List<PreviewRow> previewRows = new ArrayList<>();
    private List<CSVRecord> records = List.of();
    private Map<String, Integer> headers;
    private final List<String> importedDistinguishedNames = new ArrayList<>();

    public ImportGroupDialog(GroupsService groupsService, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.groupsService = groupsService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        this.onSearch = onSearch;
        setHeaderTitle(message("import_group_dialog.title"));
        setMaxWidth("800px");

        FormLayout form = new FormLayout();
        ContainerField container = new ContainerField(groupsService.getLdapService(), messageSource, localeService);
        container.setValue(groupsService.getDefaultContainer());
        form.setColspan(container, 2);

        Button importButton = new Button(message("import_group_dialog.import"));
        Button rollbackButton = new Button(message("import_group_dialog.rollback"));
        rollbackButton.setEnabled(false);
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setEnabled(false);
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("text/csv", ".csv", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        upload.addSucceededListener(event -> parse(buffer, event.getFileName(), importButton));
        form.add(container, upload);

        previewGrid.addColumn(PreviewRow::row).setHeader(message("import_group_dialog.row")).setAutoWidth(true);
        previewGrid.addColumn(PreviewRow::name).setHeader(message("import_group_dialog.name")).setAutoWidth(true);
        previewGrid.addColumn(PreviewRow::status).setHeader(message("import_group_dialog.status")).setAutoWidth(true);
        previewGrid.addColumn(PreviewRow::error).setHeader(message("import_group_dialog.error")).setFlexGrow(1);
        previewGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        previewGrid.setHeight("240px");
        previewGrid.setVisible(false);
        add(new VerticalLayout(form, previewGrid));

        importButton.addClickListener(event -> importRows(container, rollbackButton));
        rollbackButton.addClickListener(event -> {
            ConfirmDialog confirm = new ConfirmDialog();
            confirm.setHeader(message("import_group_dialog.rollback"));
            confirm.setText(message("import_group_dialog.rollback_confirm", importedDistinguishedNames.size()));
            confirm.setCancelable(true);
            confirm.setConfirmText(message("import_group_dialog.rollback"));
            confirm.addConfirmListener(ignored -> {
                importedDistinguishedNames.reversed().forEach(groupsService::delete);
                importedDistinguishedNames.clear();
                rollbackButton.setEnabled(false);
                Notification.show(message("import_group_dialog.rollback_done"));
                onSearch.run();
            });
            confirm.open();
        });
        getFooter().add(new Button(message("common.cancel"), event -> close()), rollbackButton, importButton);
    }

    private void parse(MultiFileMemoryBuffer buffer, String fileName, Button importButton) {
        try (InputStream input = buffer.getInputStream(fileName);
             InputStreamReader reader = new InputStreamReader(fileName.toLowerCase().endsWith(".xlsx")
                     ? new java.io.ByteArrayInputStream(SpreadsheetImport.firstSheetAsCsv(input.readAllBytes()).getBytes(java.nio.charset.StandardCharsets.UTF_8))
                     : input);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            records = parser.getRecords();
            headers = parser.getHeaderMap();
            previewRows.clear();
            for (CSVRecord record : records) {
                String error = validate(record);
                previewRows.add(new PreviewRow(record.getRecordNumber(), value(record, "name"),
                        error == null ? "Ready" : "Invalid", error));
            }
            previewGrid.setItems(previewRows);
            previewGrid.setVisible(true);
            importButton.setEnabled(previewRows.stream().anyMatch(row -> row.error() == null || row.error().isBlank()));
        } catch (IOException exception) {
            notifyError(message("import_group_dialog.failed_to_parse_csv") + ": " + exception.getMessage());
        }
    }

    private String validate(CSVRecord record) {
        for (String column : List.of("name", "scope", "type")) {
            if (!hasValue(record, column)) return "Missing or empty column: " + column;
        }
        try {
            GroupScope.valueOf(value(record, "scope"));
        } catch (IllegalArgumentException exception) {
            return "Scope must be Local, Global or Universal";
        }
        if (!value(record, "type").equalsIgnoreCase("security") && !value(record, "type").equalsIgnoreCase("distribution")) {
            return "Type must be Security or Distribution";
        }
        return null;
    }

    private void importRows(ContainerField container, Button rollbackButton) {
        int imported = 0, failed = 0;
        for (CSVRecord record : records) {
            PreviewRow row = find(record.getRecordNumber());
            if (row != null && row.error() != null && !row.error().isBlank()) { failed++; continue; }
            try {
                GroupEntry group = new GroupEntry();
                group.setCn(value(record, "name"));
                group.setDescription(value(record, "description"));
                groupsService.add(container.getValue(), group, GroupScope.valueOf(value(record, "scope")),
                        value(record, "type").equalsIgnoreCase("security"));
                GroupEntry created = groupsService.getByCN(value(record, "name"));
                if (created != null && created.getDistinguishedName() != null) importedDistinguishedNames.add(created.getDistinguishedName());
                if (row != null) row.status("Imported");
                imported++;
            } catch (Exception exception) {
                failed++;
                if (row != null) { row.status("Error"); row.error(exception.getMessage() == null ? "Import failed" : exception.getMessage()); }
            }
        }
        previewGrid.getDataProvider().refreshAll();
        Notification notification = Notification.show(message("import_group_dialog.result", imported, failed));
        notification.addThemeVariants(failed == 0 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_CONTRAST);
        onSearch.run();
        rollbackButton.setEnabled(!importedDistinguishedNames.isEmpty());
    }

    private boolean hasValue(CSVRecord record, String key) {
        return headers != null && headers.containsKey(key) && record.isSet(key) && record.get(key) != null && !record.get(key).trim().isEmpty();
    }
    private String value(CSVRecord record, String key) { return hasValue(record, key) ? record.get(key).trim() : ""; }
    private PreviewRow find(long row) { return previewRows.stream().filter(item -> item.row() == row).findFirst().orElse(null); }
    private void notifyError(String text) { Notification notification = Notification.show(text); notification.addThemeVariants(NotificationVariant.LUMO_ERROR); }
    private String message(String key) { return messageSource.getMessage(key, null, localeService.getCurrentLocale()); }
    private String message(String key, Object... args) { return messageSource.getMessage(key, args, localeService.getCurrentLocale()); }

    private static final class PreviewRow {
        private final long row;
        private final String name;
        private String status;
        private String error;
        private PreviewRow(long row, String name, String status, String error) { this.row = row; this.name = name; this.status = status; this.error = error; }
        long row() { return row; }
        String name() { return name; }
        String status() { return status; }
        String error() { return error == null ? "" : error; }
        void status(String value) { status = value; }
        void error(String value) { error = value; }
    }
}
