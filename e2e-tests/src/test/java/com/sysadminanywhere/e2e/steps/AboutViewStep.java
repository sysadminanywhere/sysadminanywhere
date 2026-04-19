package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class AboutViewStep {
    private final Page page;

    public AboutViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on About menu item
        page.locator("vaadin-side-nav-item").getByText("About").or(page.locator("vaadin-side-nav-item").getByText("О программе")).or(page.locator("vaadin-side-nav-item").getByText("À propos")).or(page.locator("vaadin-side-nav-item").getByText("Über")).click();

        // Wait for AboutView URL
        page.waitForURL("**/settings/about");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("About") || title.equals("О программе") || title.equals("À propos") || title.equals("Über") : "Unexpected page title: " + title;
    }
}
