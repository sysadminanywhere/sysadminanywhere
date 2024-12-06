package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.domain.FilterSpecification;
import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.model.ContactEntry;
import com.sysadminanywhere.service.ContactsService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Contacts")
@Route(value = "management/contacts", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ContactsView extends Div {

    private Grid<ContactEntry> grid;

    private Filters filters;
    private final ContactsService contactsService;

    public ContactsView(ContactsService contactsService) {
        this.contactsService = contactsService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), contactsService);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
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

    public static class Filters extends Div {

        private final ContactsService contactsService;

        private final TextField cn = new TextField("CN");

        public Filters(Runnable onSearch, ContactsService contactsService) {
            this.contactsService = contactsService;

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            //cn.setPlaceholder("First or last name");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                cn.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Button plusButton = new Button("New");
            plusButton.addClickListener(e -> addDialog(onSearch).open());

            Div actions = new Div(plusButton, resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(cn, actions);
        }

        public String getFilters() {
            String searchFilters = "";

            if (!cn.isEmpty()) {
                searchFilters += "(cn=" + cn.getValue() + "*)";
            }

            return searchFilters;
        }

        private Dialog addDialog(Runnable onSearch) {
            Dialog dialog = new Dialog();

            dialog.setHeaderTitle("New contact");
            dialog.setMaxWidth("800px");

            FormLayout formLayout = new FormLayout();

            TextField txtContainer = new TextField("Container");
            txtContainer.setValue(contactsService.getDefaultContainer());
            formLayout.setColspan(txtContainer, 2);

            TextField txtDisplayName = new TextField("Display name");
            formLayout.setColspan(txtDisplayName, 2);
            txtDisplayName.setRequired(true);

            TextField txtFirstName = new TextField("First name");
            txtFirstName.setRequired(true);
            TextField txtInitials = new TextField("Initials");
            TextField txtLastName = new TextField("Last name");
            txtLastName.setRequired(true);

            formLayout.add(txtContainer,txtDisplayName, txtFirstName, txtInitials, txtLastName);
            dialog.add(formLayout);

            Button saveButton = new Button("Save", e -> {
                ContactEntry contact = new ContactEntry();
                contact.setCn(txtDisplayName.getValue());
                contact.setDisplayName(txtDisplayName.getValue());
                contact.setFirstName(txtFirstName.getValue());
                contact.setInitials(txtInitials.getValue());
                contact.setLastName(txtLastName.getValue());
                try {
                    ContactEntry newContact = contactsService.add(txtContainer.getValue(), contact);

                    onSearch.run();

                    Notification notification = Notification.show("Contact added");
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception ex) {
                    Notification notification = Notification.show(ex.getMessage());
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                dialog.close();
            });

            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Button cancelButton = new Button("Cancel", e -> dialog.close());
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(saveButton);

            return dialog;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(ContactEntry.class, false);
        grid.addColumn("cn").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("management/contacts/" + item.getItem().getCn() + "/details"));
        });

        grid.setItems(query -> contactsService.getAll(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
