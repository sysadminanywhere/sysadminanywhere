package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class AutomationsViewStep {
    private final Page page;

    public AutomationsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Automation menu item (button with teams-nav-button class)
        page.locator("vaadin-button.teams-nav-button").nth(3).click();

        // Wait for AutomationsView URL
        page.waitForURL("**/automation/workflows");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Automations") || title.equals("Автоматизация") || title.equals("Automatisation") || title.equals("Automatisierung") : "Unexpected page title: " + title;
    }
}
