package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class DomainViewStep {
    private final Page page;

    public DomainViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Domain menu item
        page.locator("vaadin-side-nav-item").getByText("Domain").or(page.locator("vaadin-side-nav-item").getByText("Домен")).or(page.locator("vaadin-side-nav-item").getByText("Domaine")).or(page.locator("vaadin-side-nav-item").getByText("Domäne")).click();

        // Wait for DomainView URL
        page.waitForURL("**/domain/info");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Domain") || title.equals("Домен") || title.equals("Domaine") || title.equals("Domäne") : "Unexpected page title: " + title;
    }
}
