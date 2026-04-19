package com.sysadminanywhere.e2e.steps.groups;

import com.microsoft.playwright.Page;

public class GroupsDeleteStep {
    private final Page page;

    public GroupsDeleteStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Scroll to end of list
        page.locator("vaadin-grid").hover();
        for (int i = 0; i < 20; i++) {
            page.mouse().wheel(0, 500);
            page.waitForTimeout(100);
        }

        // Click on the group in the grid
        page.locator("vaadin-grid").getByText("Test Group").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/groups/*/details");
        page.waitForTimeout(1000);

        // Verify group data on details page (h3 displays the cn/name)
        page.locator("h3").getByText("Test Group").waitFor();

        // Click on "Delete" menu item
        page.locator("vaadin-menu-bar-item").getByText("Delete").or(page.locator("vaadin-menu-bar-item").getByText("Удалить")).or(page.locator("vaadin-menu-bar-item").getByText("Supprimer")).or(page.locator("vaadin-menu-bar-item").getByText("Löschen")).click();

        // Wait for delete confirmation dialog to appear
        page.waitForTimeout(500);
        page.locator("vaadin-confirm-dialog-overlay").waitFor();

        // Click on "Delete" button in confirmation dialog
        page.locator("vaadin-button").getByText("Delete").or(page.locator("vaadin-button").getByText("Удалить")).or(page.locator("vaadin-button").getByText("Supprimer")).or(page.locator("vaadin-button").getByText("Löschen")).click();

        // Wait for delete to complete and navigation back to groups list
        page.waitForURL("**/management/groups");
        page.waitForTimeout(2000);

        // Verify that the group is no longer in the list
        assert page.locator("vaadin-grid").getByText("Test Group").count() == 0 : "Group 'Test Group' should not be in the list";
    }
}
