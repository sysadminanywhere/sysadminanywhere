package com.sysadminanywhere.views.servicedesk;

import com.sysadminanywhere.common.incident.model.Category;
import com.sysadminanywhere.common.incident.model.Priority;
import com.sysadminanywhere.common.incident.model.TicketItem;
import com.sysadminanywhere.common.incident.model.TicketStatus;
import com.sysadminanywhere.control.HasMenu;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.TicketService;
import com.sysadminanywhere.service.Utils;
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

import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route("tickets")
public class TicketsView extends Div implements HasMenu, HasDynamicTitle {

    private Grid<TicketItem> grid;

    private TicketsView.Filters filters;
    private final TicketService ticketService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public TicketsView(TicketService ticketService, MessageSource messageSource, LocaleService localeService) {
        this.ticketService = ticketService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        if (!ticketService.ping()) {
            Notification notification = Notification.show(getMessage("common.error") + ": " + getMessage("tickets_view.service_unavailable"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            filters = new TicketsView.Filters(() -> refreshGrid(), messageSource, localeService);
            

            VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
            layout.setSizeFull();
            add(layout);
        }
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
            TicketItem newTicket = TicketItem.builder()
                    .status(TicketStatus.OPEN)
                    .priority(Priority.MEDIUM)
                    .category(Category.OTHER)
                    .build();
            ticketDialog(ticketService, newTicket, this::refreshGrid).open();
        });

        return menuBar;
    }

    public static class Filters extends Div {

        private final ComboBox<String> status;
        private final ComboBox<String> priority;
        private final ComboBox<String> category;
        private final ComboBox<String> assignee;
        private final MessageSource messageSource;
        private final LocaleService localeService;

        public Filters(Runnable onSearch, MessageSource messageSource, LocaleService localeService) {
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.status = new ComboBox<>(getMessage("tickets_view.status"));
            this.priority = new ComboBox<>(getMessage("tickets_view.priority"));
            this.category = new ComboBox<>(getMessage("tickets_view.category"));
            this.assignee = new ComboBox<>(getMessage("tickets_view.assignee"));

            status.setItems(getMessage("tickets_view.all"), getMessage("tickets_view.open"), getMessage("tickets_view.in_progress"), 
                    getMessage("tickets_view.resolved"), getMessage("tickets_view.on_hold"), getMessage("tickets_view.closed"));
            status.setValue(getMessage("tickets_view.all"));

            priority.setItems(getMessage("tickets_view.all"), getMessage("tickets_view.low"), getMessage("tickets_view.medium"), 
                    getMessage("tickets_view.high"), getMessage("tickets_view.critical"));
            priority.setValue(getMessage("tickets_view.all"));

            category.setItems(getMessage("tickets_view.all"), getMessage("tickets_view.hardware"), getMessage("tickets_view.software"), 
                    getMessage("tickets_view.network"), getMessage("tickets_view.access"), getMessage("tickets_view.other"));
            category.setValue(getMessage("tickets_view.all"));

            assignee.setItems(getMessage("tickets_view.all"));
            assignee.setValue(getMessage("tickets_view.all"));

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            Button resetBtn = new Button(getMessage("common.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                status.clear();
                status.setValue(getMessage("tickets_view.all"));

                priority.clear();
                priority.setValue(getMessage("tickets_view.all"));

                category.clear();
                category.setValue(getMessage("tickets_view.all"));

                assignee.clear();
                assignee.setValue(getMessage("tickets_view.all"));

                onSearch.run();
            });

            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(status, priority, category, assignee, actions);
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        private Map<String, String> getStatusReverseMapping() {
            Map<String, String> mapping = new HashMap<>();
            mapping.put(getMessage("tickets_view.all"), "ALL");
            mapping.put(getMessage("tickets_view.open"), "OPEN");
            mapping.put(getMessage("tickets_view.in_progress"), "IN_PROGRESS");
            mapping.put(getMessage("tickets_view.resolved"), "RESOLVED");
            mapping.put(getMessage("tickets_view.on_hold"), "ON_HOLD");
            mapping.put(getMessage("tickets_view.closed"), "CLOSED");
            return mapping;
        }

        private Map<String, String> getPriorityReverseMapping() {
            Map<String, String> mapping = new HashMap<>();
            mapping.put(getMessage("tickets_view.all"), "ALL");
            mapping.put(getMessage("tickets_view.low"), "LOW");
            mapping.put(getMessage("tickets_view.medium"), "MEDIUM");
            mapping.put(getMessage("tickets_view.high"), "HIGH");
            mapping.put(getMessage("tickets_view.critical"), "CRITICAL");
            return mapping;
        }

        private Map<String, String> getCategoryReverseMapping() {
            Map<String, String> mapping = new HashMap<>();
            mapping.put(getMessage("tickets_view.all"), "ALL");
            mapping.put(getMessage("tickets_view.hardware"), "HARDWARE");
            mapping.put(getMessage("tickets_view.software"), "SOFTWARE");
            mapping.put(getMessage("tickets_view.network"), "NETWORK");
            mapping.put(getMessage("tickets_view.access"), "ACCESS");
            mapping.put(getMessage("tickets_view.other"), "OTHER");
            return mapping;
        }

        public Map<String, Object> getFilters() {
            Map<String, Object> filters = new HashMap<>();
            String statusValue = status.getValue();
            String priorityValue = priority.getValue();
            String categoryValue = category.getValue();
            String assigneeValue = assignee.getValue();

            filters.put("status", getStatusReverseMapping().getOrDefault(statusValue, statusValue.toUpperCase().replace(" ", "_")));
            filters.put("priority", getPriorityReverseMapping().getOrDefault(priorityValue, priorityValue.toUpperCase()));
            filters.put("category", getCategoryReverseMapping().getOrDefault(categoryValue, categoryValue.toUpperCase()));
            filters.put("assignee", assigneeValue.equals(getMessage("tickets_view.all")) ? null : assigneeValue);
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(TicketItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(TicketItem::getTicketNumber).setHeader(getMessage("tickets_view.ticket_number")).setAutoWidth(true);
        
        grid.addColumn(ticketItem ->
                        Utils.formatLocalDateTime(ticketItem.getCreatedAt()))
                .setHeader(getMessage("common.created_at")).setAutoWidth(true);

        grid.addColumn("title").setHeader(getMessage("tickets_view.title")).setAutoWidth(true);
        grid.addColumn("status").setHeader(getMessage("tickets_view.status")).setAutoWidth(true);
        grid.addColumn("priority").setHeader(getMessage("tickets_view.priority")).setAutoWidth(true);
        grid.addColumn("category").setHeader(getMessage("tickets_view.category")).setAutoWidth(true);
        grid.addColumn("assignee").setHeader(getMessage("tickets_view.assignee")).setAutoWidth(true);

        grid.addItemClickListener(item -> {
                ticketDialog(ticketService, item.getItem(), this::refreshGrid).open();
        });

        grid.setItems(query -> ticketService.getTickets(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Dialog ticketDialog(TicketService ticketService, TicketItem ticketItem, Runnable onSearch) {
        return new TicketDialog(ticketService, ticketItem, messageSource, localeService, onSearch);
    }

    public String getPageTitle() {
        return getMessage("tickets_view.title");
    }

}
