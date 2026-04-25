package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementSoftwareStep {
    private final Page page;

    public ComputersManagementSoftwareStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Navigate back to computer details page
        page.navigate(page.url().replaceAll("/events$", "/details"));
        page.waitForTimeout(1000);

        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Software" menu item
        page.locator("vaadin-menu-bar-item").getByText("Software").or(page.locator("vaadin-menu-bar-item").getByText("Программы")).or(page.locator("vaadin-menu-bar-item").getByText("Logiciels")).or(page.locator("vaadin-menu-bar-item").getByText("Software")).click();

        // Wait for navigation to software page
        page.waitForURL("**/management/computers/*/software");
        page.waitForTimeout(1000);

        // Verify software page is loaded
        page.locator("vaadin-grid").waitFor();
    }
}
