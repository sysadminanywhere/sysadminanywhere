package com.sysadminanywhere.control;

import com.sysadminanywhere.model.Container;
import com.sysadminanywhere.model.Containers;
import com.sysadminanywhere.service.LdapService;
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
    private String selected = "";

    public ContainerField(LdapService ldapService) {
        setLabel("Container");

        this.ldapService = ldapService;
        HorizontalLayout layout = new HorizontalLayout(container, button);
        layout.setFlexGrow(1, container);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layout.setSpacing(false);
        layout.getThemeList().add("spacing-s");

        button.addClickListener(e -> showTree().open());

        add(layout);
    }

    @Override
    protected String generateModelValue() {
        return "";
    }

    @Override
    protected void setPresentationValue(String s) {
        container.setValue(s);
    }

    private Dialog showTree() {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Containers");
        dialog.setWidth("600px");
        dialog.setHeight("500px");

        Tree<Container> tree = new Tree<>(Container::getName);

        Containers containers = ldapService.getContainers();
        tree.setItems(containers.getRootContainers(), containers::getChildContainers);
        tree.setHeightFull();

        dialog.add(tree);

        Button saveButton = new Button("Select", e -> {
            if(!selected.isEmpty())
                container.setValue(selected);

            dialog.close();
        });
        saveButton.setEnabled(false);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());

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

}