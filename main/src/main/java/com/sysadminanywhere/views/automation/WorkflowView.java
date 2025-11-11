package com.sysadminanywhere.views.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.workflow.Workflow;
import com.sysadminanywhere.model.workflow.WorkflowData;
import com.sysadminanywhere.service.WorkflowsService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.ui.LoadMode;
import jakarta.annotation.security.PermitAll;
import lombok.SneakyThrows;

@PageTitle("Workflow")
@Route(value = "automation/workflows/:id?/details")
@PermitAll
public class WorkflowView extends Div implements BeforeEnterObserver, MenuControl {

    private String id;

    private final WorkflowsService workflowsService;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    Div n8Container;

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        id = beforeEnterEvent.getRouteParameters().get("id").
                orElse(null);

        loadFlow(id);
    }

    public WorkflowView(WorkflowsService workflowsService) {
        this.workflowsService = workflowsService;

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

        lblName.setText("Name");
        lblName.setWidth("100%");

        lblDescription.setText("Description");
        lblDescription.setWidth("100%");

        add(verticalLayout);


        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        horizontalLayout.add(verticalLayout2);

        verticalLayout.add(horizontalLayout);

        n8Container = new Div();
        n8Container.setId("n8n-container");
        n8Container.setWidth("100%");
        n8Container.setHeight("600px");

        verticalLayout.add(n8Container);
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

        MenuHelper.createIconItem(menuBar, "/icons/pencil.svg", "Update", event -> {
            if (!id.isEmpty()) {
                event.getSource().getUI().ifPresent(ui ->
                        ui.getPage().open("http://localhost:5678/workflow/" + id, "_blank"));
            }
        });

        MenuHelper.createIconItem(menuBar, "/icons/trash.svg", "Delete", event -> {
            deleteDialog().open();
        });

        return menuBar;
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure you want to permanently delete this workflow?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            workflowsService.delete(id);
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }


    @SneakyThrows
    public Html workflowPreview(WorkflowData workflowData) {

        ObjectMapper mapper = new ObjectMapper();
        String workflow = mapper.writeValueAsString(workflowData);

        String n8nHtml = "<n8n-demo workflow='" + workflow + "'></n8n-demo>";

        return new Html(n8nHtml);
    }


}