package com.sysadminanywhere.views.about;

import com.sysadminanywhere.service.DependencyHealthService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.util.Map;

@Route("settings/dependencies")
@RolesAllowed("ADMIN")
public class DependencyHealthView extends VerticalLayout implements HasDynamicTitle {
    private final MessageSource messages;
    private final LocaleService locale;
    private final DependencyHealthService service;
    private final Grid<Map.Entry<String, String>> grid = new Grid<>();

    public DependencyHealthView(DependencyHealthService service, MessageSource messages, LocaleService locale) {
        this.service = service; this.messages = messages; this.locale = locale;
        setSizeFull();
        Button refresh = new Button(msg("dependency_health.refresh"), event -> refresh());
        add(new H2(msg("dependency_health.title")), refresh);
        grid.addColumn(Map.Entry::getKey).setHeader(msg("dependency_health.service"));
        grid.addColumn(Map.Entry::getValue).setHeader(msg("dependency_health.status"));
        grid.setSizeFull(); add(grid); expand(grid); refresh();
    }

    private void refresh() { grid.setItems(service.check().services().entrySet()); }
    private String msg(String key) { return messages.getMessage(key, null, locale.getCurrentLocale()); }
    @Override public String getPageTitle() { return msg("dependency_health.title"); }
}
