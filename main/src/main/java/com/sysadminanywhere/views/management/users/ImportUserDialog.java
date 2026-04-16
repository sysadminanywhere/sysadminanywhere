package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.control.ContainerField;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.UsersService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("text/csv", ".csv");

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            try (InputStream inputStream = buffer.getInputStream(fileName);
                 InputStreamReader reader = new InputStreamReader(inputStream);
                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                csvRecords = new ArrayList<>();
                for (CSVRecord record : parser) {
                    csvRecords.add(record);
                }
                headerMap = parser.getHeaderMap();

                if (!csvRecords.isEmpty()) {
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
        add(formLayout);

        saveButton.addClickListener(e -> {
            if (csvRecords == null || csvRecords.isEmpty()) {
                Notification notification = Notification.show(getMessage("import_user_dialog.no_data_to_import"));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            for (CSVRecord record : csvRecords) {
                UserEntry user = new UserEntry();
                user.setCn(record.get("displayName"));
                user.setDisplayName(record.get("displayName"));
                user.setFirstName(record.get("firstName"));
                user.setLastName(record.get("lastName"));
                user.setSamAccountName(record.get("accountName"));
                try {
                    UserEntry newUser = usersService.add(
                            containerField.getValue(),
                            user,
                            record.get("password"),
                            false,
                            false,
                            false,
                            true);

                    if (headerMap.containsKey("company") && !record.get("company").isEmpty())
                        newUser.setCompany(record.get("company"));

                    if (headerMap.containsKey("department") && !record.get("department").isEmpty())
                        newUser.setDepartment(record.get("department"));

                    if (headerMap.containsKey("title") && !record.get("title").isEmpty())
                        newUser.setTitle(record.get("title"));

                    if (headerMap.containsKey("description") && !record.get("description").isEmpty())
                        newUser.setDescription(record.get("description"));

                    usersService.update(newUser);

                    Notification notification = Notification.show(getMessage("import_user_dialog.user_added", user.getDisplayName()));
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception ex) {
                    Notification notification = Notification.show(ex.getMessage());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }

            onSearch.run();
            close();
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

        getFooter().add(templateButton);
        getFooter().add(cancelButton);
        getFooter().add(saveButton);

    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.getCurrentLocale());
    }

}
