package com.sysadminanywhere.views.settings;

import com.sysadminanywhere.control.Card;
import com.sysadminanywhere.model.DisplayNamePattern;
import com.sysadminanywhere.model.LoginPattern;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.button.Button;

import java.util.Arrays;
import java.util.stream.Collectors;


@PageTitle("Settings")
@Route(value = "settings", layout = MainLayout.class)
@PermitAll
public class SettingsView extends VerticalLayout {

    public SettingsView() {
        add(getAbout(), getUserPatterns(), getFeedback());
    }

    private Card getAbout() {
        Card card = new Card("About Sysadmin Anywhere");
        card.setWidthFull();
        Span version = new Span("Version: 0.9.0");
        Anchor homePage = new Anchor("https://sysadminanywhere.com/", "Home page", AnchorTarget.BLANK);
        Anchor sourceCode = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/", "Source code", AnchorTarget.BLANK);

        card.add(new VerticalLayout(version, homePage, sourceCode));
        return card;
    }

    private Card getUserPatterns() {
        Card card = new Card("User patterns");
        card.setWidthFull();

        ComboBox<String> cmbDisplayNamePattern = new ComboBox<>("Display name pattern");
        cmbDisplayNamePattern.setMinWidth("400px");
        cmbDisplayNamePattern.setItems(Arrays.stream(DisplayNamePattern.values()).map(DisplayNamePattern::getTitle).collect(Collectors.toList()));

        ComboBox<String> cmbLoginPattern = new ComboBox<>("User account name pattern");
        cmbLoginPattern.setMinWidth("400px");
        cmbLoginPattern.setItems(Arrays.stream(LoginPattern.values()).map(LoginPattern::getTitle).collect(Collectors.toList()));

        TextField txtDefaultPassword = new TextField("Set default password for new users");
        txtDefaultPassword.setMinWidth("400px");

        Button saveButton = new Button("Save", e -> {
            String displayNamePattern = DisplayNamePattern.NONE.name();
            for (DisplayNamePattern pattern : DisplayNamePattern.values()) {
                if (pattern.getTitle().equalsIgnoreCase(cmbDisplayNamePattern.getValue()))
                    displayNamePattern = pattern.name();
            }

            String loginPattern = LoginPattern.NONE.name();
            for (LoginPattern pattern : LoginPattern.values()) {
                if(pattern.getTitle().equalsIgnoreCase(cmbLoginPattern.getValue()))
                    loginPattern = pattern.name();
            }

            String defaultPassword = txtDefaultPassword.getValue();
        });
        saveButton.setEnabled(false);

        card.add(new VerticalLayout(cmbDisplayNamePattern, cmbLoginPattern, txtDefaultPassword, saveButton));
        return card;
    }

    private Card getFeedback() {
        Card card = new Card("Feedback");
        card.setWidthFull();

        Anchor newBugReport = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/issues/new?template=bug_report.md", "New bug report", AnchorTarget.BLANK);
        Anchor newFeatureRequest = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/issues/new?template=feature_request.md", "New feature request", AnchorTarget.BLANK);

        card.add(new VerticalLayout(newBugReport, newFeatureRequest));

        return card;
    }

}