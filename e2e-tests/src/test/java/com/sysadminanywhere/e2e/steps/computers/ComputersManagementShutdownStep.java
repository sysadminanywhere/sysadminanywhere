package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementShutdownStep {
    private final Page page;

    public ComputersManagementShutdownStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Shutdown" menu item
        page.locator("vaadin-menu-bar-item").getByText("Shutdown").or(page.locator("vaadin-menu-bar-item").getByText("Выключить")).or(page.locator("vaadin-menu-bar-item").getByText("Arrêter")).or(page.locator("vaadin-menu-bar-item").getByText("Herunterfahren")).click();

        // Wait for shutdown confirmation dialog to appear
        page.waitForTimeout(500);
        page.locator("vaadin-confirm-dialog-overlay").waitFor();

        // Click on "Shutdown" button in confirmation dialog
        page.locator("vaadin-button").getByText("Shutdown").or(page.locator("vaadin-button").getByText("Выключить")).or(page.locator("vaadin-button").getByText("Arrêter")).or(page.locator("vaadin-button").getByText("Herunterfahren")).click();

        // Wait for shutdown command to be sent
        page.waitForTimeout(2000);
    }
}
