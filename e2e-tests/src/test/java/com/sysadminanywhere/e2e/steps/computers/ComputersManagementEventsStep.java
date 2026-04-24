package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementEventsStep {
    private final Page page;

    public ComputersManagementEventsStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Navigate back to computer details page
        page.navigate(page.url().replaceAll("/services$", "/details"));
        page.waitForTimeout(1000);

        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Events" menu item
        page.locator("vaadin-menu-bar-item").getByText("Events").or(page.locator("vaadin-menu-bar-item").getByText("События")).or(page.locator("vaadin-menu-bar-item").getByText("Événements")).or(page.locator("vaadin-menu-bar-item").getByText("Ereignisse")).click();

        // Wait for navigation to events page
        page.waitForURL("**/management/computers/*/events");
        page.waitForTimeout(1000);

        // Verify events page is loaded
        page.locator("vaadin-grid").waitFor();
    }
}
