package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersManagementRebootStep {
    private final Page page;

    public ComputersManagementRebootStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Navigate back to computer details page
        page.navigate(page.url().replaceAll("/performance$", "/details"));
        page.waitForTimeout(1000);

        // Click on "Management" menu item
        page.locator("vaadin-menu-bar-item").getByText("Management").or(page.locator("vaadin-menu-bar-item").getByText("Управление")).or(page.locator("vaadin-menu-bar-item").getByText("Gestion")).or(page.locator("vaadin-menu-bar-item").getByText("Verwaltung")).click();

        // Wait for submenu to appear
        page.waitForTimeout(500);

        // Click on "Reboot" menu item
        page.locator("vaadin-menu-bar-item").getByText("Reboot").or(page.locator("vaadin-menu-bar-item").getByText("Перезагрузить")).or(page.locator("vaadin-menu-bar-item").getByText("Redémarrer")).or(page.locator("vaadin-menu-bar-item").getByText("Neustarten")).click();

        // Wait for reboot confirmation dialog to appear
        page.waitForTimeout(500);
        page.locator("vaadin-confirm-dialog-overlay").waitFor();

        // Click on "Reboot" button in confirmation dialog
        page.locator("vaadin-button").getByText("Reboot").or(page.locator("vaadin-button").getByText("Перезагрузить")).or(page.locator("vaadin-button").getByText("Redémarrer")).or(page.locator("vaadin-button").getByText("Neustarten")).click();

        // Wait for reboot command to be sent
        page.waitForTimeout(2000);
    }
}
