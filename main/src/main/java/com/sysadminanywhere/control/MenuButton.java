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

        this.setTooltipText(label);

        normalButton();
    }

    public void selected(boolean isSelected) {
        this.isSelected = isSelected;

        if (isSelected)
            selectedButton();
        else
            normalButton();
    }

    private void normalButton() {
        if (svgIcon != null)
            svgIcon.setColor("grey");
        this.getStyle().setBorder("none");
        this.getStyle().setBackground("transparent");
        this.getElement().removeAttribute("active");
    }

    private void selectedButton() {
        if (svgIcon != null)
            svgIcon.setColor("var(--lumo-primary-color)");
        this.getStyle().setBorder("1px");
        this.getStyle().setBackground("#F6F8F9");
        this.getElement().setAttribute("active", true);
    }

    public static String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }

        String[] words = fullName.trim().split("\\s+");
        String initials = words[0].substring(0, 1).toUpperCase();

        if (words.length > 1) {
            initials += words[1].substring(0, 1).toUpperCase();
        }

        return initials;
    }

}