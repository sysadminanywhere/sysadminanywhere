package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.service.GroupsService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Group details")
@Route(value = "management/groups/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
@Uses(ListBox.class)
@Uses(TabSheet.class)
public class GroupDetailsView extends Div implements BeforeEnterObserver {

    private String id;
    private final GroupsService groupsService;
    private GroupEntry group;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    ListBox<String> listMemberOf = new ListBox<>();
    ListBox<String> listMembers = new ListBox<>();

    Binder<GroupEntry> binder = new Binder<>(GroupEntry.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        updateView();
    }

    private Runnable updateRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        };
    }

    private void updateView() {
        if (id != null) {
            group = groupsService.getByCN(id);

            if (group != null) {
                binder.readBean(group);

                lblName.setText(group.getCn());
                lblDescription.setText(group.getDescription());

                listMemberOf.clear();
                if (group.getMemberOf() != null) {
                    List<String> items = new ArrayList<>();
                    if (group.getPrimaryGroupId() != 0)
                        items.add(ADHelper.getPrimaryGroup(group.getPrimaryGroupId()));
                    for (String item : group.getMemberOf()) {
                        items.add(ADHelper.ExtractCN(item));
                    }
                    listMemberOf.setItems(items);
                }

                listMembers.clear();
                if (group.getMembers() != null) {
                    List<String> items = new ArrayList<>();
                    for (String item : group.getMembers()) {
                        items.add(ADHelper.ExtractCN(item));
                    }
                    listMembers.setItems(items);
                }
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
        MenuHelper.createIconItem(menuBar, VaadinIcon.EDIT, "Update", event -> {
            updateDialog().open();
        });
        MenuHelper.createIconItem(menuBar, VaadinIcon.TRASH, "Delete", event -> {
            deleteDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout horizontalLayout2 = new HorizontalLayout(menuBar);
        horizontalLayout2.setWidthFull();
        horizontalLayout2.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        horizontalLayout.add(verticalLayout2, horizontalLayout2);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtGroupType = new TextField("Group type");
        binder.bind(txtGroupType, GroupEntry::getADGroupType, null);
        txtGroupType.setReadOnly(true);

        formLayout.add(txtGroupType);

        verticalLayout.add(formLayout);

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Member of", listMemberOf);
        tabSheet.add("Members", listMembers);
        add(tabSheet);

        verticalLayout.add(new Hr(), tabSheet);
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

    private Dialog updateDialog() {
        return new UpdateGroupDialog(groupsService, group, updateRunnable());
    }

}