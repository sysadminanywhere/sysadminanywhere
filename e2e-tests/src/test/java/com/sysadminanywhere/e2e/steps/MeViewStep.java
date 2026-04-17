package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class MeViewStep {
    private final Page page;

    public MeViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Me menu item
        page.locator("vaadin-side-nav-item").getByText("Me").or(page.locator("vaadin-side-nav-item").getByText("Профиль")).or(page.locator("vaadin-side-nav-item").getByText("Moi")).or(page.locator("vaadin-side-nav-item").getByText("Ich")).click();

        // Wait for MeView URL
        page.waitForURL("**/account/me");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Me") || title.equals("Профиль") || title.equals("Moi") || title.equals("Ich") : "Unexpected page title: " + title;
    }
}
