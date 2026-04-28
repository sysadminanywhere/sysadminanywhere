package com.sysadminanywhere.views.vendor;

import com.sysadminanywhere.common.vendor.model.VendorContractor;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.VendorContractorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.context.MessageSource;

import java.time.LocalDate;

public class VendorContractorDialog extends Dialog {

    private final VendorContractorService contractorService;
    private final VendorContractor contractor;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Runnable onSearch;

    private TextField txtName;
    private TextField txtCompany;
    private TextField txtEmail;
    private TextField txtPhone;
    private TextField txtAdUsername;
    private TextField txtAdDomain;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> comboAccessLevel;
    private ComboBox<String> comboStatus;
    private TextArea txtNotes;

    public VendorContractorDialog(VendorContractorService contractorService, VendorContractor contractor, 
                                  MessageSource messageSource, LocaleService localeService, Runnable onSearch) {
        this.contractorService = contractorService;
        this.contractor = contractor;
        this.messageSource = messageSource;
        this.localeService = localeService;
        this.onSearch = onSearch;

        setHeaderTitle(contractor.getId() == null ? getMessage("vendor_contractor_dialog.create_title") : getMessage("vendor_contractor_dialog.title"));
        setWidth("600px");

        FormLayout formLayout = new FormLayout();

        txtName = new TextField(getMessage("vendor_contractor_dialog.name"));
        txtName.setValue(contractor.getName() != null ? contractor.getName() : "");
        txtName.setRequired(true);

        txtCompany = new TextField(getMessage("vendor_contractor_dialog.company"));
        txtCompany.setValue(contractor.getCompany() != null ? contractor.getCompany() : "");
        txtCompany.setRequired(true);

        txtEmail = new TextField(getMessage("vendor_contractor_dialog.email"));
        txtEmail.setValue(contractor.getEmail() != null ? contractor.getEmail() : "");

        txtPhone = new TextField(getMessage("vendor_contractor_dialog.phone"));
        txtPhone.setValue(contractor.getPhone() != null ? contractor.getPhone() : "");

        txtAdUsername = new TextField(getMessage("vendor_contractor_dialog.ad_username"));
        txtAdUsername.setValue(contractor.getAdUsername() != null ? contractor.getAdUsername() : "");
        txtAdUsername.setPlaceholder(getMessage("vendor_contractor_dialog.ad_username_placeholder"));

        txtAdDomain = new TextField(getMessage("vendor_contractor_dialog.ad_domain"));
        txtAdDomain.setValue(contractor.getAdDomain() != null ? contractor.getAdDomain() : "");
        txtAdDomain.setPlaceholder(getMessage("vendor_contractor_dialog.ad_domain_placeholder"));

        startDatePicker = new DatePicker(getMessage("vendor_contractor_dialog.start_date"));
        startDatePicker.setValue(contractor.getStartDate() != null ? contractor.getStartDate() : LocalDate.now());
        startDatePicker.setRequired(true);

        endDatePicker = new DatePicker(getMessage("vendor_contractor_dialog.end_date"));
        endDatePicker.setValue(contractor.getEndDate() != null ? contractor.getEndDate() : LocalDate.now().plusMonths(6));
        endDatePicker.setRequired(true);

        comboAccessLevel = new ComboBox<>(getMessage("vendor_contractor_dialog.access_level"));
        comboAccessLevel.setItems(getMessage("vendor_contractor_dialog.read_only"), 
                getMessage("vendor_contractor_dialog.standard"),
                getMessage("vendor_contractor_dialog.elevated"),
                getMessage("vendor_contractor_dialog.admin"));
        comboAccessLevel.setValue(contractor.getAccessLevel() != null ? 
                getAccessLevelLabel(contractor.getAccessLevel()) : getMessage("vendor_contractor_dialog.standard"));

        comboStatus = new ComboBox<>(getMessage("vendor_contractor_dialog.status"));
        comboStatus.setItems(getMessage("vendor_contractor_dialog.active"),
                getMessage("vendor_contractor_dialog.expired"),
                getMessage("vendor_contractor_dialog.revoked"),
                getMessage("vendor_contractor_dialog.pending"));
        comboStatus.setValue(contractor.getStatus() != null ? 
                getStatusLabel(contractor.getStatus()) : getMessage("vendor_contractor_dialog.pending"));

        txtNotes = new TextArea(getMessage("vendor_contractor_dialog.notes"));
        txtNotes.setValue(contractor.getNotes() != null ? contractor.getNotes() : "");
        txtNotes.setMinHeight("80px");

        formLayout.add(txtName, txtCompany, txtEmail, txtPhone, txtAdUsername, txtAdDomain, 
                       startDatePicker, endDatePicker, comboAccessLevel, comboStatus, txtNotes);

        add(formLayout);

        Button saveButton = new Button(getMessage("common.save"), e -> {
            try {
                if (contractor.getId() == null) {
                    VendorContractor newContractor = VendorContractor.builder()
                            .name(txtName.getValue())
                            .company(txtCompany.getValue())
                            .email(txtEmail.getValue())
                            .phone(txtPhone.getValue())
                            .adUsername(txtAdUsername.getValue())
                            .adDomain(txtAdDomain.getValue())
                            .startDate(startDatePicker.getValue())
                            .endDate(endDatePicker.getValue())
                            .accessLevel(getAccessLevelFromLabel(comboAccessLevel.getValue()))
                            .status(getStatusFromLabel(comboStatus.getValue()))
                            .notes(txtNotes.getValue())
                            .build();
                    
                    contractorService.createContractor(newContractor);
                    
                    onSearch.run();
                    
                    Notification notification = Notification.show(getMessage("vendor_contractor_dialog.contractor_created"));
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    contractor.setName(txtName.getValue());
                    contractor.setCompany(txtCompany.getValue());
                    contractor.setEmail(txtEmail.getValue());
                    contractor.setPhone(txtPhone.getValue());
                    contractor.setAdUsername(txtAdUsername.getValue());
                    contractor.setAdDomain(txtAdDomain.getValue());
                    contractor.setStartDate(startDatePicker.getValue());
                    contractor.setEndDate(endDatePicker.getValue());
                    contractor.setAccessLevel(getAccessLevelFromLabel(comboAccessLevel.getValue()));
                    contractor.setStatus(getStatusFromLabel(comboStatus.getValue()));
                    contractor.setNotes(txtNotes.getValue());
                    
                    contractorService.updateContractor(contractor.getId(), contractor);
                    
                    onSearch.run();
                    
                    Notification notification = Notification.show(getMessage("vendor_contractor_dialog.contractor_updated"));
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            } catch (Exception ex) {
                Notification notification = Notification.show(ex.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            close();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button(getMessage("common.cancel"), e -> close());
        getFooter().add(cancelButton);
        getFooter().add(saveButton);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private String getAccessLevelLabel(VendorContractor.AccessLevel level) {
        switch (level) {
            case READ_ONLY: return getMessage("vendor_contractor_dialog.read_only");
            case STANDARD: return getMessage("vendor_contractor_dialog.standard");
            case ELEVATED: return getMessage("vendor_contractor_dialog.elevated");
            case ADMIN: return getMessage("vendor_contractor_dialog.admin");
            default: return getMessage("vendor_contractor_dialog.standard");
        }
    }

    private VendorContractor.AccessLevel getAccessLevelFromLabel(String label) {
        if (label.equals(getMessage("vendor_contractor_dialog.read_only"))) return VendorContractor.AccessLevel.READ_ONLY;
        if (label.equals(getMessage("vendor_contractor_dialog.standard"))) return VendorContractor.AccessLevel.STANDARD;
        if (label.equals(getMessage("vendor_contractor_dialog.elevated"))) return VendorContractor.AccessLevel.ELEVATED;
        if (label.equals(getMessage("vendor_contractor_dialog.admin"))) return VendorContractor.AccessLevel.ADMIN;
        return VendorContractor.AccessLevel.STANDARD;
    }

    private String getStatusLabel(VendorContractor.ContractorStatus status) {
        switch (status) {
            case ACTIVE: return getMessage("vendor_contractor_dialog.active");
            case EXPIRED: return getMessage("vendor_contractor_dialog.expired");
            case REVOKED: return getMessage("vendor_contractor_dialog.revoked");
            case PENDING: return getMessage("vendor_contractor_dialog.pending");
            default: return getMessage("vendor_contractor_dialog.pending");
        }
    }

    private VendorContractor.ContractorStatus getStatusFromLabel(String label) {
        if (label.equals(getMessage("vendor_contractor_dialog.active"))) return VendorContractor.ContractorStatus.ACTIVE;
        if (label.equals(getMessage("vendor_contractor_dialog.expired"))) return VendorContractor.ContractorStatus.EXPIRED;
        if (label.equals(getMessage("vendor_contractor_dialog.revoked"))) return VendorContractor.ContractorStatus.REVOKED;
        if (label.equals(getMessage("vendor_contractor_dialog.pending"))) return VendorContractor.ContractorStatus.PENDING;
        return VendorContractor.ContractorStatus.PENDING;
    }

}
