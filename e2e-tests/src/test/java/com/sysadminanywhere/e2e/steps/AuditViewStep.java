package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class AuditViewStep {
    private final Page page;

    public AuditViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Audit menu item
        page.locator("vaadin-side-nav-item").getByText("Audit").or(page.locator("vaadin-side-nav-item").getByText("Аудит")).click();

        // Wait for AuditView URL
        page.waitForURL("**/domain/audit");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Audit") || title.equals("Аудит") : "Unexpected page title: " + title;
    }
}
