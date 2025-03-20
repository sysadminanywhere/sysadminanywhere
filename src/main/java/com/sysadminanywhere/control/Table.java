package com.sysadminanywhere.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.dom.Style;

public class Table extends Composite<Div> implements HasSize {

    Div div = new Div();
    H4 title = new H4();
    NativeTable table = new NativeTable();
    NativeTableBody tbody = new NativeTableBody();
    String titleText;
    String nameColumnWidth;
    String valueColumnWidth;

    public Table(String titleText) {
        this.titleText = titleText;
        this.nameColumnWidth = "200px";
        this.valueColumnWidth = "";
    }

    public Table(String titleText, String nameColumnWidth) {
        this.titleText = titleText;
        this.nameColumnWidth = nameColumnWidth;
        this.valueColumnWidth = "";
    }

    public Table(String titleText, String nameColumnWidth, String valueColumnWidth) {
        this.titleText = titleText;
        this.nameColumnWidth = nameColumnWidth;
        this.valueColumnWidth = valueColumnWidth;
    }

    @Override
    public Div initContent() {
        div.getStyle().setMarginTop("10px");
        div.getStyle().setMarginBottom("10px");

        title.setText(titleText);
        title.getStyle().setMarginBottom("10px");

        table.add(tbody);
        div.add(title, table);
        return div;
    }

    public void add(String name, Component component) {
        NativeTableRow row = new NativeTableRow();
        NativeTableCell nameCell = new NativeTableCell(name);
        nameCell.getStyle().setMarginRight("20px");
        NativeTableCell valueCell = new NativeTableCell(component);

        if (!nameColumnWidth.isEmpty())
            nameCell.setMinWidth(nameColumnWidth);

        if (!valueColumnWidth.isEmpty())
            valueCell.setMinWidth(valueColumnWidth);

        row.add(nameCell, valueCell);
        tbody.add(row);
    }

    public void add(String name, String value) {
        NativeLabel label = new NativeLabel(value);
        label.getStyle().setFontWeight(Style.FontWeight.BOLD);
        add(name, label);
    }

}