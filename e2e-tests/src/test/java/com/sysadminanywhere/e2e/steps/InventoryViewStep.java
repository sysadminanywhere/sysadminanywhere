package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class InventoryViewStep {
    private final Page page;

    public InventoryViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Inventory menu item (button with teams-nav-button class)
        page.locator("vaadin-button.teams-nav-button").nth(4).click();

        // Wait for InventorySoftwareView URL
        page.waitForURL("**/inventory/software");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Software") || title.equals("Программное обеспечение") || title.equals("Logiciels") : "Unexpected page title: " + title;
    }
}
