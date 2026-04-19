package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersDeleteStep {
    private final Page page;

    public ComputersDeleteStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on the computer in the grid
        page.locator("vaadin-grid").getByText("Test Computer").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/computers/*/details");
        page.waitForTimeout(1000);

        // Verify computer data on details page (h3 displays the cn/name)
        page.locator("h3").getByText("Test Computer").waitFor();

        // Click on "Delete" menu item
        page.locator("vaadin-menu-bar-item").getByText("Delete").or(page.locator("vaadin-menu-bar-item").getByText("Удалить")).or(page.locator("vaadin-menu-bar-item").getByText("Supprimer")).or(page.locator("vaadin-menu-bar-item").getByText("Löschen")).click();

        // Wait for delete confirmation dialog to appear
        page.waitForTimeout(500);
        page.locator("vaadin-confirm-dialog-overlay").waitFor();

        // Click on "Delete" button in confirmation dialog
        page.locator("vaadin-button").getByText("Delete").or(page.locator("vaadin-button").getByText("Удалить")).or(page.locator("vaadin-button").getByText("Supprimer")).or(page.locator("vaadin-button").getByText("Löschen")).click();

        // Wait for delete to complete and navigation back to computers list
        page.waitForURL("**/management/computers");
        page.waitForTimeout(2000);

        // Verify that the computer is no longer in the list
        assert page.locator("vaadin-grid").getByText("Test Computer").count() == 0 : "Computer 'Test Computer' should not be in the list";
    }
}
