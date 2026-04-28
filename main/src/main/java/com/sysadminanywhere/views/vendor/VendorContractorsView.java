package com.sysadminanywhere.views.vendor;

import com.sysadminanywhere.common.vendor.model.VendorContractor;
import com.sysadminanywhere.control.HasMenu;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.Utils;
import com.sysadminanywhere.service.VendorContractorService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route("vendor-contractors")
public class VendorContractorsView extends Div implements HasMenu, HasDynamicTitle {

    private Grid<VendorContractor> grid;

    private VendorContractorsView.Filters filters;
    private final VendorContractorService contractorService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public VendorContractorsView(VendorContractorService contractorService, MessageSource messageSource, LocaleService localeService) {
        this.contractorService = contractorService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new VendorContractorsView.Filters(() -> refreshGrid(), messageSource, localeService);

        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        add(layout);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private HorizontalLayout createMobileFilters() {
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span(getMessage("common.filters"));
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();

        MenuHelper.createIconItem(menuBar, "/icons/plus.svg", getMessage("common.new"), event -> {
            VendorContractor newContractor = VendorContractor.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(6))
                    .accessLevel(VendorContractor.AccessLevel.STANDARD)
                    .status(VendorContractor.ContractorStatus.PENDING)
                    .build();
            contractorDialog(contractorService, newContractor, this::refreshGrid).open();
        });

        return menuBar;
    }

    public static class Filters extends Div {

        private final ComboBox<String> status;
        private final ComboBox<String> accessLevel;
        private final ComboBox<String> company;
        private final MessageSource messageSource;
        private final LocaleService localeService;

        public Filters(Runnable onSearch, MessageSource messageSource, LocaleService localeService) {
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.status = new ComboBox<>(getMessage("vendor_contractors_view.status"));
            this.accessLevel = new ComboBox<>(getMessage("vendor_contractors_view.access_level"));
            this.company = new ComboBox<>(getMessage("vendor_contractors_view.company"));

            status.setItems(getMessage("vendor_contractors_view.all"), 
                    getMessage("vendor_contractors_view.active"), 
                    getMessage("vendor_contractors_view.expired"), 
                    getMessage("vendor_contractors_view.revoked"), 
                    getMessage("vendor_contractors_view.pending"));
            status.setValue(getMessage("vendor_contractors_view.all"));

            accessLevel.setItems(getMessage("vendor_contractors_view.all"),
                    getMessage("vendor_contractors_view.read_only"),
                    getMessage("vendor_contractors_view.standard"),
                    getMessage("vendor_contractors_view.elevated"),
                    getMessage("vendor_contractors_view.admin"));
            accessLevel.setValue(getMessage("vendor_contractors_view.all"));

            company.setItems(getMessage("vendor_contractors_view.all"));
            company.setValue(getMessage("vendor_contractors_view.all"));

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            Button resetBtn = new Button(getMessage("common.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                status.clear();
                status.setValue(getMessage("vendor_contractors_view.all"));

                accessLevel.clear();
                accessLevel.setValue(getMessage("vendor_contractors_view.all"));

                company.clear();
                company.setValue(getMessage("vendor_contractors_view.all"));

                onSearch.run();
            });

            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(status, accessLevel, company, actions);
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        private Map<String, String> getStatusReverseMapping() {
            Map<String, String> mapping = new HashMap<>();
            mapping.put(getMessage("vendor_contractors_view.all"), "ALL");
            mapping.put(getMessage("vendor_contractors_view.active"), "ACTIVE");
            mapping.put(getMessage("vendor_contractors_view.expired"), "EXPIRED");
            mapping.put(getMessage("vendor_contractors_view.revoked"), "REVOKED");
            mapping.put(getMessage("vendor_contractors_view.pending"), "PENDING");
            return mapping;
        }

        private Map<String, String> getAccessLevelReverseMapping() {
            Map<String, String> mapping = new HashMap<>();
            mapping.put(getMessage("vendor_contractors_view.all"), "ALL");
            mapping.put(getMessage("vendor_contractors_view.read_only"), "READ_ONLY");
            mapping.put(getMessage("vendor_contractors_view.standard"), "STANDARD");
            mapping.put(getMessage("vendor_contractors_view.elevated"), "ELEVATED");
            mapping.put(getMessage("vendor_contractors_view.admin"), "ADMIN");
            return mapping;
        }

        public Map<String, Object> getFilters() {
            Map<String, Object> filters = new HashMap<>();
            String statusValue = status.getValue();
            String accessLevelValue = accessLevel.getValue();
            String companyValue = company.getValue();

            filters.put("status", getStatusReverseMapping().getOrDefault(statusValue, statusValue.toUpperCase()));
            filters.put("accessLevel", getAccessLevelReverseMapping().getOrDefault(accessLevelValue, accessLevelValue.toUpperCase()));
            filters.put("company", companyValue.equals(getMessage("vendor_contractors_view.all")) ? null : companyValue);
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(VendorContractor.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(VendorContractor::getName).setHeader(getMessage("vendor_contractors_view.name")).setAutoWidth(true);
        grid.addColumn(VendorContractor::getCompany).setHeader(getMessage("vendor_contractors_view.company")).setAutoWidth(true);
        grid.addColumn(VendorContractor::getEmail).setHeader(getMessage("vendor_contractors_view.email")).setAutoWidth(true);
        grid.addColumn(VendorContractor::getPhone).setHeader(getMessage("vendor_contractors_view.phone")).setAutoWidth(true);
        
        grid.addColumn(contractor -> 
                        Utils.formatLocalDate(contractor.getStartDate()))
                .setHeader(getMessage("vendor_contractors_view.start_date")).setAutoWidth(true);

        grid.addColumn(contractor -> 
                        Utils.formatLocalDate(contractor.getEndDate()))
                .setHeader(getMessage("vendor_contractors_view.end_date")).setAutoWidth(true);

        grid.addColumn("accessLevel").setHeader(getMessage("vendor_contractors_view.access_level")).setAutoWidth(true);
        grid.addColumn("status").setHeader(getMessage("vendor_contractors_view.status")).setAutoWidth(true);

        grid.addItemClickListener(item -> {
                contractorDialog(contractorService, item.getItem(), this::refreshGrid).open();
        });

        grid.setItems(query -> contractorService.getAllContractors(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Dialog contractorDialog(VendorContractorService contractorService, VendorContractor contractor, Runnable onSearch) {
        return new VendorContractorDialog(contractorService, contractor, messageSource, localeService, onSearch);
    }

    public String getPageTitle() {
        return getMessage("vendor_contractors_view.title");
    }

}
