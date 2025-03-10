package com.sysadminanywhere.control;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.SvgIcon;

public class MenuButton extends Button {

    boolean isSelected = false;
    SvgIcon svgIcon;

    public MenuButton(String label, String imagePath) {
        svgIcon = new SvgIcon(imagePath);
        svgIcon.setColor("grey");

        this.setIcon(svgIcon);

        this.setWidth("48px");
        this.setHeight("48px");

        this.getStyle().setBorderRadius("10px");
        this.getStyle().setMargin("0px");
        this.setClassName("teams-nav-button");

        normalButton();
    }

    public void selected(boolean isSelected) {
        this.isSelected = isSelected;

        if(isSelected)
            selectedButton();
        else
            normalButton();
    }

    private void normalButton() {
        svgIcon.setColor("grey");
        this.getStyle().setBorder("none");
        this.getStyle().setBackground("transparent");
        this.getElement().removeAttribute("active");
    }

    private void selectedButton() {
        svgIcon.setColor("var(--lumo-primary-color)");
        this.getStyle().setBorder("1px");
        this.getStyle().setBackground("#F6F8F9");
        this.getElement().setAttribute("active", true);
    }

}