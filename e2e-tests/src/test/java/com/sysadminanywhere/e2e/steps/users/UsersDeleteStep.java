package com.sysadminanywhere.e2e.steps.users;

import com.microsoft.playwright.Page;

public class UsersDeleteStep {
    private final Page page;

    public UsersDeleteStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on the user in the grid
        page.locator("vaadin-grid").getByText("Test User").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/users/*/details");
        page.waitForTimeout(1000);

        // Verify user data on details page (h3 displays the cn/display name)
        page.locator("h3").getByText("Test User").waitFor();

        // Click on "Delete" menu item
        page.locator("vaadin-menu-bar-item").getByText("Delete").or(page.locator("vaadin-menu-bar-item").getByText("Удалить")).or(page.locator("vaadin-menu-bar-item").getByText("Supprimer")).or(page.locator("vaadin-menu-bar-item").getByText("Löschen")).click();

        // Wait for delete confirmation dialog to appear
        page.waitForTimeout(500);
        page.locator("vaadin-confirm-dialog-overlay").waitFor();

        // Click on "Delete" button in confirmation dialog
        page.locator("vaadin-button").getByText("Delete").or(page.locator("vaadin-button").getByText("Удалить")).or(page.locator("vaadin-button").getByText("Supprimer")).or(page.locator("vaadin-button").getByText("Löschen")).click();

        // Wait for delete to complete and navigation back to users list
        page.waitForURL("**/management/users");
        page.waitForTimeout(2000);

        // Verify that the user is no longer in the list
        assert page.locator("vaadin-grid").getByText("Test User").count() == 0 : "User 'Test User' should not be in the list";
    }
}
