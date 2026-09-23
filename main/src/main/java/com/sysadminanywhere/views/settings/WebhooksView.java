package com.sysadminanywhere.views.settings;

import com.sysadminanywhere.model.WebhookSubscription;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.WebhookService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;

@RolesAllowed("ADMIN")
@Route(value = "settings/webhooks")
public class WebhooksView extends VerticalLayout implements HasDynamicTitle {
    private final WebhookService webhookService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Grid<WebhookSubscription> grid = new Grid<>();

    public WebhooksView(WebhookService webhookService, MessageSource messageSource, LocaleService localeService) {
        this.webhookService = webhookService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull(); setPadding(true); setSpacing(true);
        H2 title = new H2(message("webhooks_view.title"));
        Button add = new Button(message("webhooks_view.new"), event -> edit(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button refresh = new Button(message("common.refresh"), event -> refresh());
        HorizontalLayout header = new HorizontalLayout(title, add, refresh);
        header.setWidthFull(); header.setAlignItems(Alignment.CENTER); header.setFlexGrow(1, title);

        grid.addColumn(WebhookSubscription::getUrl).setHeader(message("webhooks_view.url")).setFlexGrow(1);
        grid.addColumn(item -> item.getEvents() == null ? "*" : String.join(", ", item.getEvents()))
                .setHeader(message("webhooks_view.events")).setFlexGrow(1);
        grid.addColumn(item -> item.isEnabled() ? message("common.yes") : message("common.no"))
                .setHeader(message("webhooks_view.enabled")).setAutoWidth(true);
        grid.addItemClickListener(event -> edit(event.getItem()));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setSizeFull();
        add(header, grid); expand(grid); refresh();
    }

    private void edit(WebhookSubscription source) {
        WebhookSubscription value = source == null ? new WebhookSubscription(null, true, "", "", List.of("incident.created", "incident.updated", "incident.closed", "user.created", "group.created"))
                : new WebhookSubscription(source.getId(), source.isEnabled(), source.getUrl(), source.getSecret(), source.getEvents());
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(source == null ? message("webhooks_view.new") : message("webhooks_view.edit"));
        TextField url = new TextField(message("webhooks_view.url")); url.setWidthFull(); url.setValue(value.getUrl());
        PasswordField secret = new PasswordField(message("webhooks_view.secret")); secret.setWidthFull(); secret.setValue(value.getSecret() == null ? "" : value.getSecret());
        TextField events = new TextField(message("webhooks_view.events")); events.setWidthFull(); events.setValue(value.getEvents() == null ? "*" : String.join(",", value.getEvents()));
        events.setHelperText("incident.created, user.created, group.created, *");
        Checkbox enabled = new Checkbox(message("webhooks_view.enabled"), value.isEnabled());
        VerticalLayout form = new VerticalLayout(url, secret, events, enabled); form.setPadding(false); form.setWidth("520px");
        dialog.add(form);
        Button save = new Button(message("webhooks_view.save"), event -> {
            try {
                value.setUrl(url.getValue()); value.setSecret(secret.getValue()); value.setEnabled(enabled.getValue());
                value.setEvents(Arrays.stream(events.getValue().split(",")).map(String::trim).filter(item -> !item.isBlank()).toList());
                webhookService.save(value); dialog.close(); refresh(); notifySuccess(message("webhooks_view.saved"));
            } catch (Exception exception) { notifyError(exception.getMessage()); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); dialog.getFooter().add(save);
        if (source != null) {
            Button test = new Button(message("webhooks_view.test"), event -> {
                try { webhookService.test(source.getId()); notifySuccess(message("webhooks_view.test_started")); }
                catch (Exception exception) { notifyError(exception.getMessage()); }
            });
            dialog.getFooter().add(test);
            Button delete = new Button(message("webhooks_view.delete"), event -> confirmDelete(source, dialog));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR); dialog.getFooter().add(delete);
        }
        dialog.open();
    }

    private void confirmDelete(WebhookSubscription subscription, Dialog editor) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(message("webhooks_view.delete")); confirm.setText(message("webhooks_view.delete_confirm"));
        confirm.setCancelable(true); confirm.setConfirmText(message("webhooks_view.delete"));
        confirm.addConfirmListener(event -> { webhookService.delete(subscription.getId()); editor.close(); refresh(); });
        confirm.open();
    }

    private void refresh() { grid.setItems(webhookService.list()); }
    private void notifySuccess(String text) { Notification.show(text); }
    private void notifyError(String text) { Notification notification = Notification.show(text == null ? message("common.error") : text); notification.addThemeVariants(NotificationVariant.LUMO_ERROR); }
    private String message(String key) { return messageSource.getMessage(key, null, localeService.getCurrentLocale()); }
    @Override public String getPageTitle() { return message("webhooks_view.title"); }
}
