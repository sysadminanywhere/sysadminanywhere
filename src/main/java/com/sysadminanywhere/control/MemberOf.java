package com.sysadminanywhere.control;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberOf extends Composite<Div> implements HasComponents, HasSize {

    LdapService ldapService;

    ListBox<String> listMemberOf = new ListBox<>();
    Div div = new Div();

    List<String> items;
    Map<String, String> map;

    Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
    Button minusButton = new Button(new Icon(VaadinIcon.MINUS));

    Entry entry;

    public MemberOf() {
        setMinWidth("300px");

        listMemberOf.addValueChangeListener(event -> {
            if (event != null)
                minusButton.setEnabled(true);
        });
    }

    public void update(LdapService ldapService, String cn) {
        this.ldapService = ldapService;

        List<Entry> list = this.ldapService.search("(cn=" + cn + ")");
        listMemberOf.clear();

        map = new HashMap<>();

        if (!list.isEmpty()) {
            entry = list.get(0);
            Attribute attribute = entry.get("memberOf");

            int primaryGroupId = 0;
            if (entry.get("primarygroupid") != null)
                primaryGroupId = Integer.parseInt(entry.get("primarygroupid").get().getString());

            items = new ArrayList<>();

            if (primaryGroupId != 0)
                items.add(ADHelper.getPrimaryGroup(primaryGroupId));

            if (attribute != null) {
                for (Value v : attribute) {
                    String value = v.getString();
                    String key = ADHelper.ExtractCN(value);
                    map.put(key, value);
                    items.add(key);
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

    private ConfirmDialog deleteDialog(String group) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete");
        dialog.setText("Are you sure?");

        dialog.setCancelable(true);

        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            ldapService.deleteMember(entry, map.get(group));

            map.remove(group);
            items.remove(group);
            listMemberOf.setItems(items);
        });

        return dialog;
    }


}