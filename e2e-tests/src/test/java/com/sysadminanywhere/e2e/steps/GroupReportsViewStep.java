package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class GroupReportsViewStep {
    private final Page page;

    public GroupReportsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Group reports menu item
        page.locator("vaadin-side-nav-item").getByText("Group reports").or(page.locator("vaadin-side-nav-item").getByText("Отчеты по группам")).or(page.locator("vaadin-side-nav-item").getByText("Rapports groupes")).or(page.locator("vaadin-side-nav-item").getByText("Gruppenberichte")).click();

        // Wait for GroupReportsView URL
        page.waitForURL("**/reports/groups");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Group reports") || title.equals("Отчеты по группам") || title.equals("Rapports groupes") || title.equals("Gruppenberichte") : "Unexpected page title: " + title;
    }
}
