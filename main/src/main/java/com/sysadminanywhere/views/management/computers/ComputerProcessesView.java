package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.wmi.ProcessEntity;
import com.sysadminanywhere.control.HasMenu;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
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
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@Route(value = "management/computers/:id?/processes")
@Uses(Icon.class)
public class ComputerProcessesView extends Div implements BeforeEnterObserver, HasMenu, HasDynamicTitle {

    private String id;

    private Grid<ProcessEntity> grid;

    private final Filters filters;
    private final ComputersService computersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerProcessesView(ComputersService computersService, MessageSource messageSource, LocaleService localeService) {
        this.computersService = computersService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        filters = new Filters(() -> refreshGrid(), computersService, messageSource, localeService);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        add(layout);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span(getMessage("computer_processes_view.filters"));
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

        private final ComputersService computersService;
        private final MessageSource messageSource;
        private final LocaleService localeService;

        private final TextField caption;

        public Filters(Runnable onSearch, ComputersService computersService, MessageSource messageSource, LocaleService localeService) {
            this.computersService = computersService;
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.caption = new TextField(getMessage("computer_processes_view.caption"));

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button(getMessage("computer_processes_view.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                caption.clear();
                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("computer_processes_view.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(caption, actions);
        }

        public Map<String, String> getFilters() {
            Map<String, String> filters = new HashMap<>();
            filters.put("caption", caption.getValue());
            return filters;
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

    }

    private Component createGrid() {
        grid = new Grid<>(ProcessEntity.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn("caption").setHeader(getMessage("computer_processes_view.caption")).setAutoWidth(true);
        grid.addColumn("description").setHeader(getMessage("computer_processes_view.description")).setAutoWidth(true);

        grid.addItemClickListener(item -> {
            showDialog(item.getItem()).open();
        });

        grid.setItems(query -> computersService.getProcesses(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters(), id).stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private Dialog showDialog(ProcessEntity process) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle(getMessage("computer_processes_view.dialog_title"));
        dialog.setMaxWidth("800px");

        FormLayout formLayout = new FormLayout();

        TextField txtCaption = new TextField(getMessage("computer_processes_view.caption"));
        txtCaption.setReadOnly(true);
        txtCaption.setValue(process.getCaption());

        TextField txtDescription = new TextField(getMessage("computer_processes_view.description"));
        txtDescription.setReadOnly(true);
        txtDescription.setValue(process.getDescription());

        TextField txtHandle = new TextField(getMessage("computer_processes_view.handle"));
        txtHandle.setReadOnly(true);
        txtHandle.setValue(process.getHandle());

        TextField txtExecutablePath = new TextField(getMessage("computer_processes_view.executable_path"));
        txtExecutablePath.setReadOnly(true);
        formLayout.setColspan(txtExecutablePath, 2);
        if (process.getExecutablePath() != null)
            txtExecutablePath.setValue(process.getExecutablePath());

        TextField txtWorkingSetSize = new TextField(getMessage("computer_processes_view.working_set_size"));
        txtWorkingSetSize.setReadOnly(true);
        txtWorkingSetSize.setValue(process.getWorkingSetSize());

        formLayout.add(txtCaption, txtDescription, txtExecutablePath, txtHandle, txtWorkingSetSize);

        dialog.add(formLayout);

        Button stopButton = new Button(getMessage("computer_processes_view.stop"), e -> {
            computersService.stopProcess(id, process);
            dialog.close();
        });
        stopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(stopButton);

        Button cancelButton = new Button(getMessage("computer_processes_view.close"), e -> dialog.close());
        dialog.getFooter().add(cancelButton);

        return dialog;
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();

        MenuHelper.createIconItem(menuBar,"/icons/refresh.svg", getMessage("computer_processes_view.refresh"), menuItemClickEvent -> {
            computersService.clearProcesses(filters.getFilters(), id);
            refreshGrid();
        });

        return menuBar;
    }

    public String getPageTitle() {
        return getMessage("computer_processes_view.title");
    }

}
