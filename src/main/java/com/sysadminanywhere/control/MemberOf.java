package com.sysadminanywhere.control;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.listbox.ListBox;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;

import java.util.ArrayList;
import java.util.List;

public class MemberOf extends Composite<Div> implements HasComponents, HasSize {

    ListBox<String> listMemberOf = new ListBox<>();
    Div div = new Div();
    private boolean showTitle = true;

    public MemberOf(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public MemberOf() {
        this.showTitle = true;
    }

    public void update(LdapService ldapService, String cn) {
        List<Entry> list = ldapService.search("(cn=" + cn + ")");
        listMemberOf.clear();

        if (!list.isEmpty()) {
            Entry entry = list.get(0);
            Attribute attribute = entry.get("memberOf");

            int primaryGroupId = 0;
            if (entry.get("primarygroupid") != null)
                primaryGroupId = Integer.parseInt(entry.get("primarygroupid").get().getString());

            if (attribute != null) {
                List<String> items = new ArrayList<>();
                if (primaryGroupId != 0)
                    items.add(ADHelper.getPrimaryGroup(primaryGroupId));
                for (Value v : attribute) {
                    items.add(ADHelper.ExtractCN(v.getString()));
                }
                listMemberOf.setItems(items);
            }
        }
    }

    @Override
    public Div initContent() {
        listMemberOf.getStyle().setMarginTop("10px");
        if (showTitle) {
            H5 title = new H5("Member of");
            title.getStyle().setMarginTop("10px");
            div.add(title, listMemberOf);
        } else {
            div.add(listMemberOf);
        }
        return div;
    }

}