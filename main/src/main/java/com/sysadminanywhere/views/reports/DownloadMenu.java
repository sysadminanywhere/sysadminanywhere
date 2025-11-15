//package com.sysadminanywhere.views.reports;
//
//import com.vaadin.flow.component.html.Anchor;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.function.SerializableSupplier;
//import com.vaadin.flow.server.StreamResource;
//import org.vaadin.reports.PrintPreviewReport;
//
//import java.util.List;
//
//public class DownloadMenu<T> {
//
//    private Class<T> typeArgumentClass;
//
//    public DownloadMenu(Class<T> typeArgumentClass) {
//        this.typeArgumentClass = typeArgumentClass;
//    }
//
//    public VerticalLayout getDownloadMenu(PrintPreviewReport<T> report, String fileName, SerializableSupplier<List<? extends T>> itemsSupplier) {
//
//        StreamResource streamResourcePdf = report.getStreamResource(fileName + ".pdf", itemsSupplier, PrintPreviewReport.Format.PDF);
//        Anchor pdf = new Anchor(streamResourcePdf, "Download PDF");
//
//        HorizontalLayout menu = new HorizontalLayout();
//        menu.add(pdf);
//
//        VerticalLayout verticalLayout = new VerticalLayout();
//        verticalLayout.add(menu, report);
//
//        return verticalLayout;
//    }
//
//}