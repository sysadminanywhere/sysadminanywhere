package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class ReportsViewStep {
    private final Page page;

    public ReportsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Reports menu item (button with teams-nav-button class)
        page.locator("vaadin-button.teams-nav-button").nth(5).click();

        // Wait for Reports URL (default redirects to users)
        page.waitForURL("**/reports/**");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Users reports") || title.equals("Отчеты по пользователям") || title.equals("Rapports utilisateurs") || title.equals("Benutzerberichte") : "Unexpected page title: " + title;
    }
}
