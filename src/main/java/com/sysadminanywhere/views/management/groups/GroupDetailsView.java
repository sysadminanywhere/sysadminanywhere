package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.service.GroupsService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
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

@PageTitle("Group details")
@Route(value = "management/groups/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class GroupDetailsView extends Div implements BeforeEnterObserver {

    private String id;
    private final GroupsService groupsService;
    GroupEntry group;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        if (id != null) {
            group = groupsService.getByCN(id);

            if (group != null) {
                lblName.setText(group.getCn());
                lblDescription.setText(group.getDescription());
            }
        }
    }

    public GroupDetailsView(GroupsService groupsService) {
        this.groupsService = groupsService;

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText("Name");
        lblName.setWidth("100%");

        lblDescription.setText("Description");
        lblDescription.setWidth("100%");

        add(verticalLayout);

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        menuBar.addItem("Edit", event -> {
            menuBar.getUI().ifPresent(ui ->
                    ui.navigate("management/groups/test/edit"));
        });
        menuBar.addItem("Delete", event -> {
            menuBar.getUI().ifPresent(ui ->
                    ui.navigate("management/groups/test/edit"));
        });

        VerticalLayout verticalLayout2 = new VerticalLayout();
        verticalLayout2.add(lblName);
        verticalLayout2.add(lblDescription);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.add(verticalLayout2, menuBar);

        verticalLayout.add(horizontalLayout);
    }

    private ConfirmDialog deleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure you want to permanently delete this group?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            groupsService.delete(group.getDistinguishedName());
            dialog.getUI().ifPresent(ui ->
                    ui.getPage().getHistory().back());
        });

        return dialog;
    }

}