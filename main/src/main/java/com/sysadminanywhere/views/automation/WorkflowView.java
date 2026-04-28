package com.sysadminanywhere.views.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.control.HasMenu;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.workflow.WorkflowData;
import com.sysadminanywhere.model.workflow.Execution;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.Utils;
import com.sysadminanywhere.service.WorkflowsService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.List;

@RolesAllowed("ADMIN")
@Route(value = "automation/workflows/:id?/details")
public class WorkflowView extends Div implements BeforeEnterObserver, HasMenu, HasDynamicTitle {

    private String id;

    private final WorkflowsService workflowsService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    Grid<Execution> executionGrid = new Grid<>(Execution.class, false);
    Div n8Container;

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        id = beforeEnterEvent.getRouteParameters().get("id").
                orElse(null);

        loadFlow(id);
        getExecutions(id);
    }

    public WorkflowView(WorkflowsService workflowsService, MessageSource messageSource, LocaleService localeService) {
        this.workflowsService = workflowsService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        UI.getCurrent().getPage().addJavaScript("https://cdn.jsdelivr.net/npm/@webcomponents/webcomponentsjs@2.0.0/webcomponents-loader.js");
        UI.getCurrent().getPage().addJavaScript("https://www.unpkg.com/lit@2.0.0-rc.2/polyfill-support.js");
        UI.getCurrent().getPage().addJavaScript("https://cdn.jsdelivr.net/npm/@n8n_io/n8n-demo-component/n8n-demo.bundled.js");

        UI.getCurrent().getPage().executeJs("""
                    const script = document.createElement('script');
                    script.type = 'module';
                    script.src = 'https://cdn.jsdelivr.net/npm/@n8n_io/n8n-demo-component/n8n-demo.bundled.js';
                    document.head.appendChild(script);
                """);

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText(getMessage("common.description"));
        lblName.setWidth("100%");

        lblDescription.setText(getMessage("common.description"));
        lblDescription.setWidth("100%");

        add(verticalLayout);

        n8Container = new Div();
        n8Container.setWidth("100%");

        executionGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        executionGrid.addColumn(Execution::getId).setHeader(getMessage("common.id")).setAutoWidth(true);

        executionGrid.addColumn(workflow ->
                        Utils.formatInstant(workflow.getStartedAt()))
                .setHeader(getMessage("common.started")).setAutoWidth(true);

        executionGrid.addColumn(workflow ->
                        Utils.formatDuration(Duration.between(
                                workflow.getStartedAt(),
                                workflow.getStoppedAt()), false))
                .setHeader(getMessage("common.run_time")).setAutoWidth(true);

        executionGrid.addColumn(Execution::getStatus).setHeader(getMessage("common.status"));
        executionGrid.addColumn(Execution::getErrorMessage).setHeader(getMessage("common.error_message"));

        executionGrid.setAllRowsVisible(true);

        verticalLayout.add(lblName, lblDescription, n8Container, executionGrid);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private void loadFlow(String id) {
        WorkflowData workflow = workflowsService.getWorkflow(id);

        lblName.setText(workflow.getName());
        lblDescription.setText(workflow.getDescription());

        n8Container.removeAll();
        n8Container.add(workflowPreview(workflow));
    }

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);

        MenuHelper.createIconItem(menuBar, "/icons/pencil.svg", getMessage("common.edit"), event -> {
            if (!id.isEmpty()) {
                event.getSource().getUI().ifPresent(ui ->
                        ui.getPage().open("http://localhost:5678/workflow/" + id, "_blank"));
            }
        });

        MenuHelper.createIconItem(menuBar, "/icons/play.svg", getMessage("common.execute"), event -> {
            executeWorkflow();
        });

        MenuHelper.createIconItem(menuBar, "/icons/trash.svg", getMessage("common.delete"), event -> {
            deleteDialog().open();
        });

        return menuBar;
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getMessage("common.delete"));
        dialog.setText(getMessage("common.delete_workflow_confirmation"));

        dialog.setCancelable(true);

        dialog.setConfirmText(getMessage("common.delete"));
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            workflowsService.delete(id);
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

    private void executeWorkflow() {
        try {
            String response = workflowsService.executeWorkflow(id);
            
            if (response != null && !response.isEmpty()) {
                Notification notification = Notification.show(response, 5000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                Notification notification = Notification.show(getMessage("workflow_view.execution_success"), 3000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        } catch (Exception e) {
            Notification notification = Notification.show(getMessage("common.error") + ": " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


    @SneakyThrows
    public Html workflowPreview(WorkflowData workflowData) {

        ObjectMapper mapper = new ObjectMapper();
        String workflow = mapper.writeValueAsString(workflowData);

        String n8nHtml = "<n8n-demo workflow='" + workflow + "'></n8n-demo>";

        return new Html(n8nHtml);
    }

    private void getExecutions(String id) {
        List<Execution> executions = workflowsService.getExecutions(id, 10);
        executionGrid.setItems(executions);
    }

    public String getPageTitle() {
        return getMessage("workflow_view.title");
    }

}
