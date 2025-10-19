package com.sysadminanywhere.control;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.model.GroupItem;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;

import java.util.ArrayList;
import java.util.List;

public class MemberOf extends Composite<Div> implements HasComponents, HasSize {

    LdapService ldapService;

    ListBox<GroupItem> listMemberOf = new ListBox<>();
    List<GroupItem> items;

    Div div = new Div();

    Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
    Button minusButton = new Button(new Icon(VaadinIcon.MINUS));

    Entry entry;
    private String selected = "";

    public MemberOf() {
        setMinWidth("300px");

        listMemberOf.setRenderer(new TextRenderer<>(GroupItem::getName));

        listMemberOf.addValueChangeListener(event -> {
            if (event != null && event.getValue() != null && !event.getValue().getDistinguishedName().isEmpty())
                minusButton.setEnabled(true);
            else
                minusButton.setEnabled(false);
        });
    }

    public void update(LdapService ldapService, String cn) {
        this.ldapService = ldapService;

        List<Entry> list = this.ldapService.search("(cn=" + cn + ")");
        listMemberOf.clear();

        if (!list.isEmpty()) {
            entry = list.get(0);
            Attribute attribute = entry.get("memberOf");

            int primaryGroupId = 0;
            if (entry.get("primarygroupid") != null)
                primaryGroupId = Integer.parseInt(entry.get("primarygroupid").get().getString());

            items = new ArrayList<>();

            if (primaryGroupId != 0)
                items.add(new GroupItem(ADHelper.getPrimaryGroup(primaryGroupId), ""));

            if (attribute != null) {
                for (Value v : attribute) {
                    String value = v.getString();
                    String key = ADHelper.ExtractCN(value);
                    items.add(new GroupItem(key, value));
                }
            }

            listMemberOf.setItems(items);
        }
    }

    @Override
    public Div initContent() {
        listMemberOf.getStyle().setMarginTop("10px");

        plusButton = new Button(new Icon(VaadinIcon.PLUS));
        plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        minusButton = new Button(new Icon(VaadinIcon.MINUS));
        minusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        minusButton.setEnabled(false);

        plusButton.addClickListener(event -> {
            addDialog().open();
        });

        minusButton.addClickListener(event -> {
            deleteDialog(listMemberOf.getValue()).open();
        });

        H5 title = new H5("Member of");
        title.getStyle().setMarginTop("10px");
        title.setWidth("50%");

        HorizontalLayout layoutWithoutSpacing = new HorizontalLayout(plusButton, minusButton);
        layoutWithoutSpacing.setSpacing(false);
        layoutWithoutSpacing.getThemeList().add("spacing-xs");
        layoutWithoutSpacing.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layoutWithoutSpacing.setWidth("50%");

        HorizontalLayout layout = new HorizontalLayout(title, layoutWithoutSpacing);
        layout.setWidthFull();

        div.add(layout, listMemberOf);

        return div;
    }

    private ConfirmDialog deleteDialog(GroupItem group) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure you want to remove from this group?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            boolean result = ldapService.deleteMember(entry, group.getDistinguishedName());

            if (result) {
                items.remove(group);
                listMemberOf.setItems(items);
            }
        });

        return dialog;
    }

    private Dialog addDialog() {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Groups");
        dialog.setWidth("600px");
        dialog.setHeight("500px");

        ListBox<GroupItem> groups = new ListBox<>();
        List<GroupItem> groupItems = new ArrayList<>();

        groups.setRenderer(new TextRenderer<>(GroupItem::getName));

        Button saveButton = new Button("Add", e -> {
            boolean result = ldapService.addMember(entry, groups.getValue().getDistinguishedName());

            if (result) {
                items.add(new GroupItem(groups.getValue().getName(), groups.getValue().getDistinguishedName()));
                listMemberOf.setItems(items);
                minusButton.setEnabled(false);
                dialog.close();
            }
        });
        saveButton.setEnabled(false);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        List<Entry> result = ldapService.search("(objectClass=group)");

        for (Entry group : result) {
            groupItems.add(new GroupItem(group.get("name").get().getString(), group.getDn().getName()));
        }

        groups.addValueChangeListener(event -> {
            if (event != null)
                saveButton.setEnabled(true);
        });

        groups.setItems(groupItems);
        dialog.add(groups);

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        return dialog;
    }

}