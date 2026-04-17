package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class SettingsViewStep {
    private final Page page;

    public SettingsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Settings menu item (button with teams-nav-button class, bottom menu)
        page.locator("vaadin-button.teams-nav-button").nth(7).click();

        // Wait for Settings URL
        page.waitForURL("**/settings/**");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Settings") || title.equals("Настройки") || title.equals("Paramètres") || title.equals("Einstellungen") : "Unexpected page title: " + title;
    }
}
