package com.sysadminanywhere.views.about;

import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.VersionService;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.card.Card;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;

@PageTitle("about_view.title")
@Route(value = "settings/about")
@PermitAll
public class AboutView extends VerticalLayout {

    private final VersionService versionService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public AboutView(VersionService versionService, MessageSource messageSource, LocaleService localeService) {
        this.versionService = versionService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        add(getAbout(), getFeedback());
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private Card getAbout() {
        Card card = new Card();
        card.setTitle(getMessage("about_view.sysadmin_anywhere"));
        card.setWidthFull();
        Span version = new Span(getMessage("about_view.version") + ": " + versionService.getVersion());
        Anchor homePage = new Anchor("https://sysadminanywhere.com/", getMessage("about_view.home_page"), AnchorTarget.BLANK);
        Anchor docsPage = new Anchor("https://docs.sysadminanywhere.com/", getMessage("about_view.documentation"), AnchorTarget.BLANK);
        Anchor sourceCode = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/", getMessage("about_view.source_code"), AnchorTarget.BLANK);

        card.add(new VerticalLayout(version, homePage, docsPage, sourceCode));
        return card;
    }

    private Card getFeedback() {
        Card card = new Card();
        card.setTitle(getMessage("about_view.feedback"));
        card.setWidthFull();

        Anchor newBugReport = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/issues/new?template=bug_report.md", getMessage("about_view.new_bug_report"), AnchorTarget.BLANK);
        Anchor newFeatureRequest = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/issues/new?template=feature_request.md", getMessage("about_view.new_feature_request"), AnchorTarget.BLANK);

        card.add(new VerticalLayout(newBugReport, newFeatureRequest));

        return card;
    }

}
