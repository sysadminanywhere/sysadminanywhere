package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.control.SpreadsheetImport;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.UsersService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.UI;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportUserDialog extends Dialog {

    private final UsersService usersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Runnable onSearch;

    private List<CSVRecord> csvRecords = null;
    private Map<String, Integer> headerMap = null;
    private final Grid<ImportPreviewRow> previewGrid = new Grid<>();
    private final List<ImportPreviewRow> previewRows = new ArrayList<>();
    private final List<String> importedDistinguishedNames = new ArrayList<>();

    public ImportUserDialog(UsersService usersService, MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.usersService = usersService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        this.onSearch = onSearch;

        setHeaderTitle(getMessage("import_user_dialog.title"));
        setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        ContainerField containerField = new ContainerField(usersService.getLdapService(), messageSource, localeService);
        containerField.setValue(usersService.getDefaultContainer());
        formLayout.setColspan(containerField, 2);

        Button saveButton = new Button(getMessage("import_user_dialog.import"));
        Button rollbackButton = new Button(getMessage("import_user_dialog.rollback"));
        rollbackButton.setEnabled(false);

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("text/csv", ".csv", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            try (InputStream inputStream = buffer.getInputStream(fileName);
                 InputStreamReader reader = new InputStreamReader(fileName.toLowerCase().endsWith(".xlsx")
                         ? new java.io.ByteArrayInputStream(SpreadsheetImport.firstSheetAsCsv(inputStream.readAllBytes()).getBytes(java.nio.charset.StandardCharsets.UTF_8))
                         : inputStream);
                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                csvRecords = new ArrayList<>();
                for (CSVRecord record : parser) {
                    csvRecords.add(record);
                }
                headerMap = parser.getHeaderMap();
                previewRows.clear();
                for (CSVRecord record : csvRecords) {
                    String error = validate(record);
                    previewRows.add(new ImportPreviewRow(record.getRecordNumber(), value(record, "displayName"),
                            value(record, "accountName"), error == null ? "Ready" : "Invalid", error));
                }
                previewGrid.setItems(previewRows);
                previewGrid.setVisible(true);
                if (!csvRecords.isEmpty() && previewRows.stream().anyMatch(row -> row.error() == null || row.error().isBlank())) {
                    saveButton.setEnabled(true);
                } else {
                    Notification notification = Notification.show(getMessage("import_user_dialog.csv_file_empty"));
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (IOException ex) {
                Notification notification = Notification.show(getMessage("import_user_dialog.failed_to_parse_csv") + ": " + ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        formLayout.add(containerField, upload);

        previewGrid.addColumn(ImportPreviewRow::rowNumber).setHeader(getMessage("import_user_dialog.row")).setAutoWidth(true);
        previewGrid.addColumn(ImportPreviewRow::displayName).setHeader(getMessage("import_user_dialog.display_name")).setAutoWidth(true);
        previewGrid.addColumn(ImportPreviewRow::accountName).setHeader(getMessage("import_user_dialog.account_name")).setAutoWidth(true);
        previewGrid.addColumn(ImportPreviewRow::status).setHeader(getMessage("import_user_dialog.status")).setAutoWidth(true);
        previewGrid.addColumn(ImportPreviewRow::error).setHeader(getMessage("import_user_dialog.error")).setFlexGrow(1);
        previewGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        previewGrid.setHeight("240px");
        previewGrid.setVisible(false);
        add(new VerticalLayout(formLayout, previewGrid));

        saveButton.addClickListener(e -> {
            if (csvRecords == null || csvRecords.isEmpty()) {
                Notification notification = Notification.show(getMessage("import_user_dialog.no_data_to_import"));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            int imported = 0;
            int failed = 0;
            for (CSVRecord record : csvRecords) {
                ImportPreviewRow preview = findPreview(record.getRecordNumber());
                if (preview != null && preview.error() != null && !preview.error().isBlank()) {
                    failed++;
                    continue;
                }
                UserEntry user = new UserEntry();
                user.setCn(value(record, "displayName"));
                user.setDisplayName(value(record, "displayName"));
                user.setFirstName(value(record, "firstName"));
                user.setLastName(value(record, "lastName"));
                user.setSamAccountName(value(record, "accountName"));
                try {
                    UserEntry newUser = usersService.add(
                            containerField.getValue(),
                            user,
                            record.get("password"),
                            false,
                            false,
                            false,
                            true);

                    if (hasValue(record, "company"))
                        newUser.setCompany(value(record, "company"));

                    if (hasValue(record, "department"))
                        newUser.setDepartment(value(record, "department"));

                    if (hasValue(record, "title"))
                        newUser.setTitle(value(record, "title"));

                    if (hasValue(record, "description"))
                        newUser.setDescription(value(record, "description"));

                    usersService.update(newUser);
                    if (newUser.getDistinguishedName() != null) importedDistinguishedNames.add(newUser.getDistinguishedName());
                    imported++;
                    if (preview != null) preview.status("Imported");
                } catch (Exception ex) {
                    failed++;
                    if (preview != null) {
                        preview.status("Error");
                        preview.error(ex.getMessage() == null ? "Import failed" : ex.getMessage());
                    }
                }
            }

            previewGrid.getDataProvider().refreshAll();
            Notification notification = Notification.show(getMessage("import_user_dialog.result", imported, failed));
            notification.addThemeVariants(failed == 0 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_CONTRAST);
            onSearch.run();
            rollbackButton.setEnabled(!importedDistinguishedNames.isEmpty());
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);

        Button templateButton = new Button(getMessage("import_user_dialog.help"), e -> {
            UI.getCurrent().getPage().open("https://github.com/sysadminanywhere/sysadminanywhere/wiki/Import-users-from-csv-file", "_blank");
        });
        templateButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button cancelButton = new Button(getMessage("common.cancel"), e -> {
            csvRecords = null;
            headerMap = null;
            close();
        });

        rollbackButton.addClickListener(e -> {
            ConfirmDialog confirm = new ConfirmDialog();
            confirm.setHeader(getMessage("import_user_dialog.rollback"));
            confirm.setText(getMessage("import_user_dialog.rollback_confirm", importedDistinguishedNames.size()));
            confirm.setCancelable(true);
            confirm.setConfirmText(getMessage("import_user_dialog.rollback"));
            confirm.addConfirmListener(event -> {
                importedDistinguishedNames.reversed().forEach(usersService::delete);
                importedDistinguishedNames.clear();
                rollbackButton.setEnabled(false);
                Notification.show(getMessage("import_user_dialog.rollback_done"));
                onSearch.run();
            });
            confirm.open();
        });

        getFooter().add(templateButton);
        getFooter().add(cancelButton);
        getFooter().add(rollbackButton);
        getFooter().add(saveButton);

    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.getCurrentLocale());
    }

    private String validate(CSVRecord record) {
        String[] required = {"displayName", "firstName", "lastName", "accountName", "password"};
        for (String column : required) {
            if (!hasValue(record, column)) return "Missing or empty column: " + column;
        }
        return null;
    }

    private boolean hasValue(CSVRecord record, String column) {
        return headerMap != null && headerMap.containsKey(column) && record.isSet(column)
                && record.get(column) != null && !record.get(column).trim().isEmpty();
    }

    private String value(CSVRecord record, String column) {
        return hasValue(record, column) ? record.get(column).trim() : "";
    }

    private ImportPreviewRow findPreview(long rowNumber) {
        return previewRows.stream().filter(row -> row.rowNumber() == rowNumber).findFirst().orElse(null);
    }

    private static final class ImportPreviewRow {
        private final long rowNumber;
        private final String displayName;
        private final String accountName;
        private String status;
        private String error;

        private ImportPreviewRow(long rowNumber, String displayName, String accountName, String status, String error) {
            this.rowNumber = rowNumber;
            this.displayName = displayName;
            this.accountName = accountName;
            this.status = status;
            this.error = error;
        }

        long rowNumber() { return rowNumber; }
        String displayName() { return displayName; }
        String accountName() { return accountName; }
        String status() { return status; }
        String error() { return error == null ? "" : error; }
        void status(String value) { this.status = value; }
        void error(String value) { this.error = value; }
    }

}
