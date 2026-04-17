package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class UsersReportsViewStep {
    private final Page page;

    public UsersReportsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Users reports menu item
        page.locator("vaadin-side-nav-item").getByText("Users reports").or(page.locator("vaadin-side-nav-item").getByText("Отчеты по пользователям")).or(page.locator("vaadin-side-nav-item").getByText("Rapports utilisateurs")).or(page.locator("vaadin-side-nav-item").getByText("Benutzerberichte")).click();

        // Wait for UsersReportsView URL
        page.waitForURL("**/reports/users");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Users reports") || title.equals("Отчеты по пользователям") || title.equals("Rapports utilisateurs") || title.equals("Benutzerberichte") : "Unexpected page title: " + title;
    }
}
