package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementPerformanceStep {
    private final Page page;

    public ComputersManagementPerformanceStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Navigate back to computer details page
        page.navigate(page.url().replaceAll("/hardware$", "/details"));
        page.waitForTimeout(1000);

        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Performance" menu item
        page.locator("vaadin-menu-bar-item").getByText("Performance").or(page.locator("vaadin-menu-bar-item").getByText("Производительность")).or(page.locator("vaadin-menu-bar-item").getByText("Performance")).or(page.locator("vaadin-menu-bar-item").getByText("Leistung")).click();

        // Wait for navigation to performance page
        page.waitForURL("**/management/computers/*/performance");
        page.waitForTimeout(2000);

        // Verify performance page is loaded (wait for H3 with computer name)
        page.locator("h3").getByText("WIN-4VEK5HJAPSG").waitFor();
    }
}
