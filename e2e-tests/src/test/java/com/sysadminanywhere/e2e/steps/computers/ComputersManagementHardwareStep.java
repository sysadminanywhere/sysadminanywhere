package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementHardwareStep {
    private final Page page;

    public ComputersManagementHardwareStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Navigate back to computer details page
        page.navigate(page.url().replaceAll("/software$", "/details"));
        page.waitForTimeout(1000);

        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Hardware" menu item
        page.locator("vaadin-menu-bar-item").getByText("Hardware").or(page.locator("vaadin-menu-bar-item").getByText("Оборудование")).or(page.locator("vaadin-menu-bar-item").getByText("Matériel")).or(page.locator("vaadin-menu-bar-item").getByText("Hardware")).click();

        // Wait for navigation to hardware page
        page.waitForURL("**/management/computers/*/hardware");
        page.waitForTimeout(1000);

        // Verify hardware page is loaded (uses ListBox, not grid)
        page.locator("vaadin-list-box").waitFor();
    }
}
