package com.sysadminanywhere.views.reports;

import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

@RolesAllowed("ADMIN")
@Route(value = "reports/others")
@Uses(Icon.class)
public class OtherReportsView extends Div implements HasDynamicTitle {

    private final MessageSource messageSource;
    private final LocaleService localeService;

    public OtherReportsView(MessageSource messageSource, LocaleService localeService) {
        this.messageSource = messageSource;
        this.localeService = localeService;
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    public String getPageTitle() {
        return getMessage("other_reports_view.title");
    }

}
