package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class SettingsSettingsViewStep {
    private final Page page;

    public SettingsSettingsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Settings menu item
        page.locator("vaadin-side-nav-item").getByText("Settings").or(page.locator("vaadin-side-nav-item").getByText("Настройки")).or(page.locator("vaadin-side-nav-item").getByText("Paramètres")).or(page.locator("vaadin-side-nav-item").getByText("Einstellungen")).click();

        // Wait for SettingsView URL
        page.waitForURL("**/settings/settings");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Settings") || title.equals("Настройки") || title.equals("Paramètres") || title.equals("Einstellungen") : "Unexpected page title: " + title;
    }
}
