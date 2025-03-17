package com.sysadminanywhere.control;

import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

import java.util.ArrayList;
import java.util.List;

public class Members extends Composite<Div> implements HasComponents, HasSize {

    ListBox<String> listMembers = new ListBox<>();
    Div div = new Div();

    Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
    Button minusButton = new Button(new Icon(VaadinIcon.MINUS));

    public Members() {
        setMinWidth("300px");

        listMembers.addValueChangeListener(event -> {
            if (event != null)
                minusButton.setEnabled(true);
        });
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

        plusButton = new Button(new Icon(VaadinIcon.PLUS));
        plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        minusButton = new Button(new Icon(VaadinIcon.MINUS));
        minusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        minusButton.setEnabled(false);

        H5 title = new H5("Members");
        title.getStyle().setMarginTop("10px");
        title.setWidth("50%");

        HorizontalLayout layoutWithoutSpacing = new HorizontalLayout(plusButton, minusButton);
        layoutWithoutSpacing.setSpacing(false);
        layoutWithoutSpacing.getThemeList().add("spacing-xs");
        layoutWithoutSpacing.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layoutWithoutSpacing.setWidth("50%");

        HorizontalLayout layout = new HorizontalLayout(title, layoutWithoutSpacing);
        layout.setWidthFull();

        div.add(layout, listMembers);

        return div;
    }

}