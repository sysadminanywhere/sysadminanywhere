package com.sysadminanywhere.views.automation;

import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.workflow.Workflow;
import com.sysadminanywhere.service.WorkflowsService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
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
import jakarta.annotation.security.PermitAll;

@PageTitle("Workflow")
@Route(value = "automation/workflows/:id?/details")
@PermitAll
public class WorkflowView extends Div implements BeforeEnterObserver, MenuControl {

    private String id;

    private final WorkflowsService workflowsService;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        id = beforeEnterEvent.getRouteParameters().get("id").
                orElse(null);

        loadFlow(id);
    }

    public WorkflowView(WorkflowsService workflowsService) {
        this.workflowsService = workflowsService;

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
    }

    private void loadFlow(String id) {
        Workflow workflow = workflowsService.getWorkflow(id);

        lblName.setText(workflow.getName());
        lblDescription.setText(workflow.getDescription());
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

        return menuBar;
    }

}