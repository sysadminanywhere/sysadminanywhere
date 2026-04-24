package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementProcessesStep {
    private final Page page;

    public ComputersManagementProcessesStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Processes" menu item
        page.locator("vaadin-menu-bar-item").getByText("Processes").or(page.locator("vaadin-menu-bar-item").getByText("Процессы")).or(page.locator("vaadin-menu-bar-item").getByText("Processus")).or(page.locator("vaadin-menu-bar-item").getByText("Prozesse")).click();

        // Wait for navigation to processes page
        page.waitForURL("**/management/computers/*/processes");
        page.waitForTimeout(1000);

        // Verify processes page is loaded
        page.locator("vaadin-grid").waitFor();
    }
}
