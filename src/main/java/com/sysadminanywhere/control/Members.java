package com.sysadminanywhere.control;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.service.LdapService;
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

public class Members extends Composite<Div> implements HasComponents, HasSize {

    ListBox<String> listMembers = new ListBox<>();
    Div div = new Div();
    private boolean showTitle = true;

    public Members(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public Members() {
        this.showTitle = true;
    }

    public void update(LdapService ldapService, String cn) {
        List<Entry> list = ldapService.search("(cn=" + cn + ")");
        listMembers.clear();

        if (!list.isEmpty()) {
            Entry entry = list.get(0);
            Attribute attribute = entry.get("member");

            if (attribute != null) {
                List<String> items = new ArrayList<>();
                for (Value v : attribute) {
                    items.add(ADHelper.ExtractCN(v.getString()));
                }
                listMembers.setItems(items);
            }
        }
    }

    @Override
    public Div initContent() {
        listMembers.getStyle().setMarginTop("10px");
        if (showTitle) {
            H5 title = new H5("Members");
            title.getStyle().setMarginTop("10px");
            div.add(title, listMembers);
        } else {
            div.add(listMembers);
        }
        return div;
    }

}