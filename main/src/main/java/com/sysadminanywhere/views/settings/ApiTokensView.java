package com.sysadminanywhere.views.settings;

import com.sysadminanywhere.client.directory.ApiTokensServiceClient;
import com.sysadminanywhere.common.directory.dto.ApiTokenCreateRequest;
import com.sysadminanywhere.common.directory.dto.ApiTokenCreatedResponse;
import com.sysadminanywhere.common.directory.dto.ApiTokenSummary;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RolesAllowed("ADMIN")
@Route(value = "settings/api-tokens")
public class ApiTokensView extends VerticalLayout implements HasDynamicTitle {
    private static final List<String> SCOPES = List.of(
            "directory:read", "directory:write", "remote:execute", "inventory:read", "incidents:read", "incidents:write");
    private final ApiTokensServiceClient apiTokensService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Grid<ApiTokenSummary> grid = new Grid<>();

    public ApiTokensView(ApiTokensServiceClient apiTokensService, MessageSource messageSource, LocaleService localeService) {
        this.apiTokensService = apiTokensService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassName("review-page");
        setPadding(false);
        setSpacing(true);

        Button create = new Button(message("api_tokens_view.create"), event -> create());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button refresh = new Button(message("common.refresh"), event -> refresh());
        HorizontalLayout header = new HorizontalLayout(create, refresh);
        header.addClassName("review-toolbar");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        grid.addColumn(ApiTokenSummary::name).setHeader(message("api_tokens_view.name")).setFlexGrow(1);
        grid.addColumn(item -> String.join(", ", item.scopes())).setHeader(message("api_tokens_view.scopes")).setFlexGrow(2);
        grid.addColumn(item -> item.expiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setHeader(message("api_tokens_view.expires")).setAutoWidth(true);
        grid.addColumn(item -> item.active() ? message("api_tokens_view.active")
                        : item.revoked() ? message("api_tokens_view.revoked") : message("api_tokens_view.expired"))
                .setHeader(message("api_tokens_view.status")).setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Button revoke = new Button(message("api_tokens_view.revoke"), event -> confirmRevoke(item));
            revoke.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            revoke.setEnabled(item.active());
            return revoke;
        }).setHeader(message("api_tokens_view.actions")).setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setSizeFull();
        add(header, grid);
        expand(grid);
        refresh();
    }

    private void create() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(message("api_tokens_view.create"));
        dialog.setWidth("560px");
        dialog.setMaxWidth("calc(100vw - 32px)");
        TextField name = new TextField(message("api_tokens_view.name"));
        name.setWidthFull();
        CheckboxGroup<String> scopes = new CheckboxGroup<>(message("api_tokens_view.scopes"));
        scopes.setItems(SCOPES);
        scopes.setItemLabelGenerator(scope -> message("api_tokens_view.scope." + scope.replace(':', '.')));
        scopes.setValue(Set.of("directory:read"));
        IntegerField expires = new IntegerField(message("api_tokens_view.expires_days"));
        expires.setMin(1);
        expires.setMax(365);
        expires.setValue(30);
        expires.setStepButtonsVisible(true);
        VerticalLayout form = new VerticalLayout(name, scopes, expires);
        form.setPadding(false);
        form.setWidthFull();
        dialog.add(form);

        Button save = new Button(message("api_tokens_view.create"), event -> {
            if (name.isEmpty() || scopes.isEmpty()) {
                showError(message("api_tokens_view.validation"));
                return;
            }
            try {
                ApiTokenCreatedResponse result = apiTokensService.create(
                        new ApiTokenCreateRequest(name.getValue().trim(), scopes.getValue().stream().toList(), expires.getValue()));
                dialog.close();
                refresh();
                showToken(result);
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(save);
        Button cancel = new Button(message("common.cancel"), event -> dialog.close());
        dialog.getFooter().add(cancel);
        dialog.open();
    }

    private void showToken(ApiTokenCreatedResponse result) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(message("api_tokens_view.created_title"));
        dialog.setWidth("640px");
        dialog.setMaxWidth("calc(100vw - 32px)");
        TextArea token = new TextArea(message("api_tokens_view.token"));
        token.setValue(result.token());
        token.setReadOnly(true);
        token.setWidthFull();
        token.setMinHeight("110px");
        VerticalLayout content = new VerticalLayout(new com.vaadin.flow.component.html.Paragraph(message("api_tokens_view.copy_once")), token);
        content.setPadding(false);
        content.setWidthFull();
        dialog.add(content);
        dialog.getFooter().add(new Button(message("common.close"), event -> dialog.close()));
        dialog.open();
    }

    private void confirmRevoke(ApiTokenSummary token) {
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader(message("api_tokens_view.revoke"));
        confirm.setText(message("api_tokens_view.revoke_confirm", token.name()));
        confirm.setCancelable(true);
        confirm.setConfirmText(message("api_tokens_view.revoke"));
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(event -> {
            try {
                apiTokensService.revoke(token.id());
                refresh();
                Notification.show(message("api_tokens_view.revoked_notice"));
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });
        confirm.open();
    }

    private void refresh() {
        try {
            grid.setItems(apiTokensService.list());
        } catch (Exception exception) {
            showError(exception.getMessage());
            grid.setItems(List.of());
        }
    }

    private void showError(String text) {
        Notification notification = Notification.show(text == null || text.isBlank() ? message("common.error") : text);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return message("api_tokens_view.title");
    }
}
