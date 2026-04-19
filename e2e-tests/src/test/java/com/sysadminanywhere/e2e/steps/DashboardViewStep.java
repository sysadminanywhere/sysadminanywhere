package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class DashboardViewStep {
    private final Page page;

    public DashboardViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Check that DashboardView is loaded by URL
        page.waitForURL("http://localhost:8080/");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Dashboard") || title.equals("Панель управления") || title.equals("Tableau de bord") : "Unexpected page title: " + title;
    }

}
