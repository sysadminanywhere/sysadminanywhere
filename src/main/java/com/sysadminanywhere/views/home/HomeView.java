package com.sysadminanywhere.views.home;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;

@PageTitle("Home")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "home", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
@PermitAll
public class HomeView extends VerticalLayout {

    private final LdapService ldapService;

    public HomeView(LdapService ldapService) {
        this.ldapService = ldapService;
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        H3 lblDomain = new H3();
        lblDomain.setText(ldapService.getDomainName().toUpperCase());
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText(ldapService.getDefaultNamingContext().toUpperCase());
        lblDistinguishedName.setWidth("100%");

        int computers = ldapService.search("(objectClass=computer)").size();
        int users = ldapService.search("(&(objectClass=user)(objectCategory=person))").size();
        int groups = ldapService.search("(objectClass=group)").size();
        int printers = ldapService.search("(objectClass=printQueue)").size();
        int contacts = ldapService.search("(&(objectClass=contact)(objectCategory=person))").size();

        ApexCharts chart = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.BAR)
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withHorizontal(false)
                                .withColumnWidth("55%")
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false).build())
                .withStroke(StrokeBuilder.get()
                        .withShow(true)
                        .withWidth(2.0)
                        .withColors("transparent")
                        .build())
                .withSeries(new Series<>("Computers",computers),
                        new Series<>("Users",users),
                        new Series<>("Groups",groups),
                        new Series<>("Printers",printers),
                        new Series<>("Contacts",contacts))
                .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
        chart.setHeight("300px");
        chart.setWidth("300px");

        verticalLayout.add(lblDomain, lblDistinguishedName, chart);

        add(verticalLayout, chart);
    }

}
