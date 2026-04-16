package com.sysadminanywhere.control;

import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.vaadin.tatu.Tree;

public class ContainerField extends CustomField<String> {

    private final TextField container = new TextField();
    private final Button button = new Button("...");

    private final LdapService ldapService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private String selected = "";

    public ContainerField(LdapService ldapService, MessageSource messageSource, LocaleService localeService) {
        this.ldapService = ldapService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setLabel(getMessage("container_field.label"));

        HorizontalLayout layout = new HorizontalLayout(container, button);
        layout.setFlexGrow(1, container);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layout.setSpacing(false);
        layout.getThemeList().add("spacing-s");

        container.addValueChangeListener(e->{
            this.updateValue();
        });

        button.addClickListener(e -> showTree().open());

        add(layout);
    }

    @Override
    protected String generateModelValue() {
        return container.getValue();
    }

    @Override
    protected void setPresentationValue(String s) {
        container.setValue(s);
    }

    private Dialog showTree() {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle(getMessage("container_field.dialog_title"));
        dialog.setWidth("600px");
        dialog.setHeight("500px");

        Tree<Container> tree = new Tree<>(Container::getName);

        Containers containers = ldapService.getContainers();
        tree.setItems(containers.getRootContainers(), containers::getChildContainers);
        tree.setHeightFull();

        dialog.add(tree);

        Button saveButton = new Button(getMessage("container_field.select"), e -> {
            if(!selected.isEmpty())
                container.setValue(selected);

            dialog.close();
        });
        saveButton.setEnabled(false);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button(getMessage("container_field.cancel"), e -> dialog.close());

        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(saveButton);

        tree.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selected = event.getValue().getDistinguishedName();
                saveButton.setEnabled(true);
            } else {
                saveButton.setEnabled(false);
            }
        });

        return dialog;
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

}
