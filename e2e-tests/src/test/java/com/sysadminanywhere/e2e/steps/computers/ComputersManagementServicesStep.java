package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementServicesStep {
    private final Page page;

    public ComputersManagementServicesStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Navigate back to computer details page
        page.navigate(page.url().replaceAll("/processes$", "/details"));
        page.waitForTimeout(1000);

        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Services" menu item
        page.locator("vaadin-menu-bar-item").getByText("Services").or(page.locator("vaadin-menu-bar-item").getByText("Службы")).or(page.locator("vaadin-menu-bar-item").getByText("Services")).or(page.locator("vaadin-menu-bar-item").getByText("Dienste")).click();

        // Wait for navigation to services page
        page.waitForURL("**/management/computers/*/services");
        page.waitForTimeout(1000);

        // Verify services page is loaded
        page.locator("vaadin-grid").waitFor();
    }
}
