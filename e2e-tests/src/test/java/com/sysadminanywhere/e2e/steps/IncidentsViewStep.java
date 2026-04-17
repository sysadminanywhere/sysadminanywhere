package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class IncidentsViewStep {
    private final Page page;

    public IncidentsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Incidents menu item (button with teams-nav-button class)
        page.locator("vaadin-button.teams-nav-button").nth(2).click();

        // Wait for IncidentsView URL
        page.waitForURL("**/incidents");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Incidents") || title.equals("Инциденты") : "Unexpected page title: " + title;
    }
}
