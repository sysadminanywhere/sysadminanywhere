package com.sysadminanywhere.control;

import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.context.MessageSource;

import java.util.List;

/** A short, repeatable introduction to the main parts of the application. */
public class OnboardingTour extends Dialog {

    private static final String TOUR_STORAGE_KEY = "sysadmin-onboarding-tour";

    private record Step(String titleKey, String descriptionKey, String target) {
    }

    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final List<Step> steps = List.of(
            new Step("tour.step.navigation.title", "tour.step.navigation.description", "[data-tour='primary-navigation']"),
            new Step("tour.step.sections.title", "tour.step.sections.description", "[data-tour='secondary-navigation']"),
            new Step("tour.step.content.title", "tour.step.content.description", "[data-tour='page-title']"),
            new Step("tour.step.help.title", "tour.step.help.description", "[data-tour='secondary-navigation']")
    );

    private final Span counter = new Span();
    private final H3 title = new H3();
    private final Paragraph description = new Paragraph();
    private final Button previous = new Button();
    private final Button next = new Button();
    private int currentStep;

    public OnboardingTour(MessageSource messageSource, LocaleService localeService) {
        this.messageSource = messageSource;
        this.localeService = localeService;

        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setWidth("min(560px, calc(100vw - 32px))");
        addClassName("onboarding-tour");

        Button close = new Button(getMessage("tour.skip"), event -> finish());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(counter, close);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        title.getStyle().set("margin", "var(--lumo-space-m) 0 0");
        description.getStyle().set("margin", "var(--lumo-space-s) 0 var(--lumo-space-m)");

        previous.setText(getMessage("tour.previous"));
        previous.addClickListener(event -> showStep(currentStep - 1));
        previous.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        next.addClickListener(event -> {
            if (currentStep == steps.size() - 1) {
                finish();
            } else {
                showStep(currentStep + 1);
            }
        });
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout actions = new HorizontalLayout(previous, next);
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        add(new VerticalLayout(header, title, description, actions));
        showStep(0);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        highlight(steps.get(currentStep).target());
    }

    public static void open(UI ui, MessageSource messageSource, LocaleService localeService) {
        new OnboardingTour(messageSource, localeService).open();
    }

    public static void openIfNeeded(UI ui, MessageSource messageSource, LocaleService localeService) {
        ui.getPage().executeJs("return localStorage.getItem($0);", TOUR_STORAGE_KEY)
                .then(String.class, value -> {
                    if (!"completed".equals(value)) {
                        open(ui, messageSource, localeService);
                    }
                });
    }

    private void showStep(int step) {
        currentStep = Math.max(0, Math.min(step, steps.size() - 1));
        Step current = steps.get(currentStep);
        counter.setText(getMessage("tour.step_counter", currentStep + 1, steps.size()));
        title.setText(getMessage(current.titleKey()));
        description.setText(getMessage(current.descriptionKey()));
        previous.setEnabled(currentStep > 0);
        next.setText(getMessage(currentStep == steps.size() - 1 ? "tour.finish" : "tour.next"));
        highlight(current.target());
    }

    private void highlight(String target) {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "document.querySelectorAll('.tour-highlight').forEach(e => e.classList.remove('tour-highlight'));" +
                        "const target = document.querySelector($0);" +
                        "if (target) target.classList.add('tour-highlight');", target));
    }

    private void finish() {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "localStorage.setItem($0, 'completed');" +
                        "document.querySelectorAll('.tour-highlight').forEach(e => e.classList.remove('tour-highlight'));",
                TOUR_STORAGE_KEY));
        close();
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.getCurrentLocale());
    }
}
