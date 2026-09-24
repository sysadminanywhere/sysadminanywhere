package com.sysadminanywhere.views.about;

import com.sysadminanywhere.control.OnboardingTour;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

@Route(value = "settings/help")
@RolesAllowed({"ADMIN", "READER"})
public class HelpView extends VerticalLayout implements HasDynamicTitle {

    private final MessageSource messageSource;
    private final LocaleService localeService;

    public HelpView(MessageSource messageSource, LocaleService localeService) {
        this.messageSource = messageSource;
        this.localeService = localeService;
        setWidthFull();
        add(getWelcomeCard(), getNavigationCard(), getWorkflowsCard(), getSupportCard());
    }

    private Card getWelcomeCard() {
        Card card = new Card();
        card.setWidthFull();
        card.setTitle(getMessage("help_view.welcome_title"));
        Paragraph text = new Paragraph(getMessage("help_view.welcome_text"));
        Button tour = new Button(getMessage("help_view.start_tour"), event ->
                OnboardingTour.open(getUI().orElseThrow(), messageSource, localeService));
        tour.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        card.add(new VerticalLayout(text, tour));
        return card;
    }

    private Card getNavigationCard() {
        Card card = new Card();
        card.setWidthFull();
        card.setTitle(getMessage("help_view.navigation_title"));
        VerticalLayout content = new VerticalLayout();
        addHelpItem(content, "help_view.access_roles_title", "help_view.access_roles_text");
        addHelpItem(content, "help_view.dashboard_title", "help_view.dashboard_text");
        addHelpItem(content, "help_view.search_title", "help_view.search_text");
        addHelpItem(content, "help_view.management_title", "help_view.management_text");
        addHelpItem(content, "help_view.incidents_title", "help_view.incidents_text");
        addHelpItem(content, "help_view.inventory_title", "help_view.inventory_text");
        addHelpItem(content, "help_view.inventory_health_title", "help_view.inventory_health_text");
        addHelpItem(content, "help_view.reports_title", "help_view.reports_text");
        addHelpItem(content, "help_view.scheduled_reports_title", "help_view.scheduled_reports_text");
        card.add(content);
        return card;
    }

    private Card getWorkflowsCard() {
        Card card = new Card();
        card.setWidthFull();
        card.setTitle(getMessage("help_view.workflows_title"));
        VerticalLayout content = new VerticalLayout();
        addHelpItem(content, "help_view.users_title", "help_view.users_text");
        addHelpItem(content, "help_view.import_users_title", "help_view.import_users_text");
        addHelpItem(content, "help_view.bulk_title", "help_view.bulk_text");
        addHelpItem(content, "help_view.bulk_directory_title", "help_view.bulk_directory_text");
        addHelpItem(content, "help_view.bulk_group_title", "help_view.bulk_group_text");
        addHelpItem(content, "help_view.bulk_move_title", "help_view.bulk_move_text");
        addHelpItem(content, "help_view.audit_title", "help_view.audit_text");
        addHelpItem(content, "help_view.change_history_title", "help_view.change_history_text");
        addHelpItem(content, "help_view.domain_health_title", "help_view.domain_health_text");
        addHelpItem(content, "help_view.security_audit_title", "help_view.security_audit_text");
        addHelpItem(content, "help_view.status_title", "help_view.status_text");
        addHelpItem(content, "help_view.computers_title", "help_view.computers_text");
        addHelpItem(content, "help_view.settings_title", "help_view.settings_text");
        addHelpItem(content, "help_view.api_tokens_title", "help_view.api_tokens_text");
        addHelpItem(content, "help_view.webhooks_title", "help_view.webhooks_text");
        card.add(content);
        return card;
    }

    private Card getSupportCard() {
        Card card = new Card();
        card.setWidthFull();
        card.setTitle(getMessage("help_view.support_title"));
        Anchor docs = new Anchor("https://docs.sysadminanywhere.com/", getMessage("help_view.documentation"), AnchorTarget.BLANK);
        Anchor issues = new Anchor("https://github.com/sysadminanywhere/sysadminanywhere/issues", getMessage("help_view.report_issue"), AnchorTarget.BLANK);
        card.add(new VerticalLayout(new Paragraph(getMessage("help_view.support_text")), docs, issues));
        return card;
    }

    private void addHelpItem(VerticalLayout parent, String titleKey, String textKey) {
        H4 title = new H4(getMessage(titleKey));
        title.getStyle().set("margin", "var(--lumo-space-s) 0 0");
        parent.add(title, new Span(getMessage(textKey)));
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return getMessage("help_view.title");
    }
}
