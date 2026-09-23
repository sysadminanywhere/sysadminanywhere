package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.common.directory.dto.ChangeJournalDto;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route(value = "domain/change-history")
public class ChangeHistoryView extends Div implements HasDynamicTitle {

    private final LdapService ldapService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Filters filters;
    private Grid<ChangeJournalDto> grid;

    public ChangeHistoryView(LdapService ldapService, MessageSource messageSource, LocaleService localeService) {
        this.ldapService = ldapService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassName("gridwith-filters-view");
        filters = new Filters(this::refreshGrid);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        add(layout);
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private HorizontalLayout createMobileFilters() {
        HorizontalLayout mobile = new HorizontalLayout();
        mobile.setWidthFull();
        mobile.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER, "mobile-filters");
        Icon icon = new Icon("lumo", "plus");
        Span heading = new Span(message("common.filters"));
        mobile.add(icon, heading);
        mobile.setFlexGrow(1, heading);
        mobile.addClickListener(event -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                icon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                icon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobile;
    }

    private class Filters extends Div {
        private final TextField objectName = new TextField(message("change_history_view.object_name"));
        private final TextField distinguishedName = new TextField(message("change_history_view.distinguished_name"));
        private final TextField actor = new TextField(message("change_history_view.actor"));
        private final ComboBox<String> action = new ComboBox<>(message("change_history_view.action"));
        private final DatePicker startDate = new DatePicker(message("change_history_view.date"));
        private final DatePicker endDate = new DatePicker();
        private final Runnable onSearch;

        private Filters(Runnable onSearch) {
            this.onSearch = onSearch;
            action.setItems(message("common.all"), message("change_history_view.created"),
                    message("change_history_view.changed"), message("change_history_view.deleted"));
            action.setValue(message("common.all"));
            setWidthFull();
            addClassNames("filter-layout", LumoUtility.Padding.Horizontal.LARGE,
                    LumoUtility.Padding.Vertical.MEDIUM, LumoUtility.BoxSizing.BORDER);

            Button reset = new Button(message("common.reset"), event -> reset());
            reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            Button search = new Button(message("common.search"), event -> onSearch.run());
            search.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            Div actions = new Div(reset, search);
            actions.addClassNames(LumoUtility.Gap.SMALL, "actions");
            add(objectName, distinguishedName, actor, action, createDateRange(), actions);
        }

        private Component createDateRange() {
            startDate.setPlaceholder(message("common.from"));
            endDate.setPlaceholder(message("common.to"));
            startDate.setAriaLabel(message("common.from_date"));
            endDate.setAriaLabel(message("common.to"));
            FlexLayout dates = new FlexLayout(startDate, new Text(" – "), endDate);
            dates.setAlignItems(FlexComponent.Alignment.BASELINE);
            dates.addClassName(LumoUtility.Gap.XSMALL);
            return dates;
        }

        private void reset() {
            objectName.clear();
            distinguishedName.clear();
            actor.clear();
            action.setValue(message("common.all"));
            startDate.clear();
            endDate.clear();
            onSearch.run();
        }

        private Map<String, String> values() {
            Map<String, String> values = new HashMap<>();
            values.put("objectName", objectName.getValue());
            values.put("distinguishedName", distinguishedName.getValue());
            values.put("actor", actor.getValue());
            String selected = action.getValue();
            if (selected != null && !selected.equals(message("common.all"))) {
                if (selected.equals(message("change_history_view.created"))) selected = "Created";
                else if (selected.equals(message("change_history_view.changed"))) selected = "Changed";
                else if (selected.equals(message("change_history_view.deleted"))) selected = "Deleted";
            } else {
                selected = "";
            }
            values.put("action", selected);
            if (startDate.getValue() != null) values.put("startDate", startDate.getValue().toString());
            if (endDate.getValue() != null) values.put("endDate", endDate.getValue().toString());
            return values;
        }
    }

    private Component createGrid() {
        grid = new Grid<>(ChangeJournalDto.class, false);
        grid.addColumn("objectName").setHeader(message("change_history_view.object_name")).setAutoWidth(true);
        grid.addColumn("distinguishedName").setHeader(message("change_history_view.distinguished_name")).setAutoWidth(true);
        grid.addColumn("objectClass").setHeader(message("audit_view.object_class")).setAutoWidth(true);
        grid.addColumn("action").setHeader(message("change_history_view.action")).setAutoWidth(true);
        grid.addColumn("actor").setHeader(message("change_history_view.actor")).setAutoWidth(true);
        grid.addColumn("changedAt").setHeader(message("change_history_view.changed")).setAutoWidth(true);
        grid.addItemClickListener(event -> showDetails(event.getItem()));
        try {
            grid.setItems(ldapService.getChangeHistory(filters.values()));
        } catch (Exception exception) {
            Notification.show(exception.getMessage());
        }
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        return grid;
    }

    private void refreshGrid() {
        grid.setItems(ldapService.getChangeHistory(filters.values()));
    }

    private void showDetails(ChangeJournalDto item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(message("change_history_view.details"));
        VerticalLayout content = new VerticalLayout(
                new H3(value(item.getObjectName())),
                new Paragraph(message("change_history_view.distinguished_name") + ": " + value(item.getDistinguishedName())),
                new Paragraph(message("change_history_view.action") + ": " + value(item.getAction())),
                new Paragraph(message("change_history_view.actor") + ": " + value(item.getActor())),
                new Paragraph(message("change_history_view.changed") + ": " + value(item.getChangedAt())));
        content.setPadding(false);
        content.add(snapshot(message("change_history_view.before"), item.getBefore()),
                snapshot(message("change_history_view.after"), item.getAfter()));
        dialog.add(content);
        dialog.getFooter().add(new Button(message("common.close"), event -> dialog.close()));
        dialog.open();
    }

    private TextArea snapshot(String label, Map<String, String> values) {
        TextArea area = new TextArea(label);
        area.setWidthFull();
        area.setReadOnly(true);
        area.setValue(format(values));
        area.setMinHeight("140px");
        return area;
    }

    private String format(Map<String, String> values) {
        if (values == null || values.isEmpty()) return message("change_history_view.no_value");
        return values.entrySet().stream().map(entry -> entry.getKey() + " = " + entry.getValue())
                .reduce((left, right) -> left + "\n" + right).orElse("");
    }

    private String value(Object value) {
        return value == null ? "—" : value.toString();
    }

    @Override
    public String getPageTitle() {
        return message("change_history_view.title");
    }
}
