package com.sysadminanywhere.views.management.groups;

import com.sysadminanywhere.control.MemberOf;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.ad.GroupEntry;
import com.sysadminanywhere.service.GroupsService;
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
@Route(value = "management/groups/:id?/details")
@PermitAll
@Uses(Icon.class)
@Uses(ListBox.class)
@Uses(TabSheet.class)
public class GroupDetailsView extends Div implements BeforeEnterObserver, MenuControl {

    private String id;
    private final GroupsService groupsService;
    private GroupEntry group;

    H3 lblName = new H3();
    H5 lblDescription = new H5();
    MemberOf memberOf = new MemberOf(false);
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

                memberOf.update(groupsService.getLdapService(), id);

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

        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        horizontalLayout.add(verticalLayout2);

        verticalLayout.add(horizontalLayout);

        FormLayout formLayout = new FormLayout();

        TextField txtGroupType = new TextField("Group type");
        binder.bind(txtGroupType, GroupEntry::getADGroupType, null);
        txtGroupType.setReadOnly(true);

        formLayout.add(txtGroupType);

        verticalLayout.add(formLayout);

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Member of", memberOf);
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

    @Override
    public MenuBar getMenu() {
        MenuBar menuBar = new MenuBar();
        MenuHelper.createIconItem(menuBar, "/icons/pencil.svg", "Update", event -> {
            updateDialog().open();
        });
        MenuHelper.createIconItem(menuBar, "/icons/trash.svg", "Delete", event -> {
            deleteDialog().open();
        });

        menuBar.addThemeVariants(MenuBarVariant.LUMO_END_ALIGNED);

        return menuBar;
    }
}