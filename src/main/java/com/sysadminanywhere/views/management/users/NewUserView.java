package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

@PageTitle("New user")
@Route(value = "management/users/new", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class NewUserView extends Composite<VerticalLayout> {

    public NewUserView() {
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H3 h32 = new H3();

        FormLayout formLayout2Col = new FormLayout();
        TextField textField2 = new TextField();
        TextField textField4 = new TextField();
        DatePicker datePicker2 = new DatePicker();
        TextField textField6 = new TextField();
        EmailField emailField2 = new EmailField();
        TextField textField8 = new TextField();

        HorizontalLayout layoutRow = new HorizontalLayout();
        Button buttonSave = new Button();
        Button buttonCancel = new Button();

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);

        layoutColumn2.setWidth("100%");
        layoutColumn2.setMaxWidth("800px");
        layoutColumn2.setHeight("min-content");

        h32.setText("Adding new user");
        h32.setWidth("100%");

        formLayout2Col.setWidth("100%");
        textField2.setLabel("First Name");
        textField4.setLabel("Last Name");
        datePicker2.setLabel("Birthday");
        textField6.setLabel("Phone Number");
        emailField2.setLabel("Email");
        textField8.setLabel("Occupation");
        layoutRow.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");

        buttonSave.setText("Save");
        buttonSave.setWidth("min-content");
        buttonSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonSave.addClickListener(e ->
                buttonSave.getUI().ifPresent(ui ->
                        ui.getPage().getHistory().back()));

        buttonCancel.setText("Cancel");
        buttonCancel.setWidth("min-content");
        buttonCancel.addClickListener(e ->
                buttonCancel.getUI().ifPresent(ui ->
                        ui.getPage().getHistory().back()));

        getContent().add(layoutColumn2);
        layoutColumn2.add(h32);
        layoutColumn2.add(formLayout2Col);

        formLayout2Col.add(textField2);
        formLayout2Col.add(textField4);
        formLayout2Col.add(datePicker2);
        formLayout2Col.add(textField6);
        formLayout2Col.add(emailField2);
        formLayout2Col.add(textField8);

        layoutColumn2.add(layoutRow);
        layoutRow.add(buttonSave);
        layoutRow.add(buttonCancel);
    }

}
