package com.sysadminanywhere.control;

import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.model.GroupItem;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
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

import java.util.ArrayList;
import java.util.List;

public class MemberOf extends Composite<Div> implements HasComponents, HasSize {

    LdapService ldapService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    ListBox<GroupItem> listMemberOf = new ListBox<>();
    List<GroupItem> items;

    Div div = new Div();

    Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
    Button minusButton = new Button(new Icon(VaadinIcon.MINUS));

    EntryDto entry;
    private final String selected = "";

    public MemberOf(MessageSource messageSource, LocaleService localeService) {
        this.messageSource = messageSource;
        this.localeService = localeService;
        setMinWidth("300px");

        listMemberOf.setRenderer(new TextRenderer<>(GroupItem::getName));

        listMemberOf.addValueChangeListener(event -> {
            minusButton.setEnabled(event != null && event.getValue() != null && !event.getValue().getDistinguishedName().isEmpty());
        });
    }

    public void update(LdapService ldapService, String cn) {
        this.ldapService = ldapService;

        List<EntryDto> list = this.ldapService.search("(cn=" + cn + ")");
        listMemberOf.clear();

        if (!list.isEmpty()) {
            entry = list.get(0);
            Object attribute = entry.getAttributes().get("memberof");

            int primaryGroupId = 0;
            if (entry.getAttributes().get("primarygroupid") != null)
                primaryGroupId = Integer.parseInt(entry.getAttributes().get("primarygroupid").toString());

            items = new ArrayList<>();

            if (primaryGroupId != 0)
                items.add(new GroupItem(ADHelper.getPrimaryGroup(primaryGroupId), ""));

            if (attribute != null && attribute instanceof List) {
                for (Object v : (List<Object>) attribute) {
                    String value = v.toString();
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

        H5 title = new H5(getMessage("member_of.title"));
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
        dialog.setHeader(getMessage("member_of.delete"));
        dialog.setText(getMessage("member_of.delete_confirmation"));

        dialog.setCancelable(true);

        dialog.setConfirmText(getMessage("member_of.delete"));
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(item -> {
            boolean result = ldapService.deleteMember(entry.getDn(), group.getDistinguishedName());

            if (result) {
                items.remove(group);
                listMemberOf.setItems(items);
            }
        });

        return dialog;
    }

    private Dialog addDialog() {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle(getMessage("member_of.groups"));
        dialog.setWidth("600px");
        dialog.setHeight("500px");

        ListBox<GroupItem> groups = new ListBox<>();
        List<GroupItem> groupItems = new ArrayList<>();

        groups.setRenderer(new TextRenderer<>(GroupItem::getName));

        Button saveButton = new Button(getMessage("member_of.add"), e -> {
            boolean result = ldapService.addMember(entry.getDn(), groups.getValue().getDistinguishedName());

            if (result) {
                items.add(new GroupItem(groups.getValue().getName(), groups.getValue().getDistinguishedName()));
                listMemberOf.setItems(items);
                minusButton.setEnabled(false);
                dialog.close();
            }
        });
        saveButton.setEnabled(false);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button(getMessage("member_of.cancel"), e -> dialog.close());

        List<EntryDto> result = ldapService.search("(objectClass=group)");

        for (EntryDto group : result) {
            groupItems.add(new GroupItem(group.getAttributes().get("name").toString(), group.getDn()));
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

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

}
