package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class InventoryHardwareViewStep {
    private final Page page;

    public InventoryHardwareViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Hardware menu item
        page.locator("vaadin-side-nav-item").getByText("Hardware").or(page.locator("vaadin-side-nav-item").getByText("Оборудование")).or(page.locator("vaadin-side-nav-item").getByText("Matériel")).click();

        // Wait for InventoryHardwareView URL
        page.waitForURL("**/inventory/hardware");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Hardware") || title.equals("Оборудование") || title.equals("Matériel") : "Unexpected page title: " + title;
    }
}
