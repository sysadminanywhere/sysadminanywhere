package com.sysadminanywhere.views.about;

import com.sysadminanywhere.control.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("About")
@Route(value = "settings/about")
@PermitAll
public class AboutView extends VerticalLayout {

    public AboutView() {
        add(getAbout(), getFeedback());
    }

    private Card getAbout() {
        Card card = new Card("Sysadmin Anywhere");
        card.setWidthFull();
        Span version = new Span("Version: 2.1.0");
        Anchor homePage = new Anchor("https://sysadminanywhere.com/", "Home page", AnchorTarget.BLANK);
        Anchor sourceCode = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/", "Source code", AnchorTarget.BLANK);

        card.add(new VerticalLayout(version, homePage, sourceCode));
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