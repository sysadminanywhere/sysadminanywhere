package com.sysadminanywhere.control;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;

public class MenuButton extends Button {

    boolean isSelected = false;
    SvgIcon svgIcon;

    public MenuButton(String label, String imagePath) {
        svgIcon = new SvgIcon(imagePath);
        svgIcon.addClassName("teams-nav-button-icon");

        Span visibleLabel = new Span(label);
        visibleLabel.addClassName("teams-nav-button-label");

        Div content = new Div(svgIcon, visibleLabel);
        content.addClassName("teams-nav-button-content");
        this.setIcon(content);
        this.setClassName("teams-nav-button");
        this.setTooltipText(label);
        this.setAriaLabel(label);

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
        this.getElement().removeAttribute("active");
    }

    private void selectedButton() {
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
