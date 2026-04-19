package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class GroupsViewStep {
    private final Page page;

    public GroupsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Groups menu item
        page.locator("vaadin-side-nav-item").getByText("Groups").or(page.locator("vaadin-side-nav-item").getByText("Группы")).or(page.locator("vaadin-side-nav-item").getByText("Groupes")).or(page.locator("vaadin-side-nav-item").getByText("Gruppen")).click();

        // Wait for GroupsView URL
        page.waitForURL("**/management/groups");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Groups") || title.equals("Группы") || title.equals("Groupes") || title.equals("Gruppen") : "Unexpected page title: " + title;
    }

    public void createNewGroup() {
        // Click on the second menu bar item (the "New" button with plus icon)
        page.locator("vaadin-menu-bar").locator("vaadin-menu-bar-item").nth(1).click();

        // Wait for dialog to appear
        page.waitForTimeout(3000);

        // Wait for any dialog overlay to be visible
        page.locator("vaadin-dialog-overlay").first().waitFor();

        // Fill in name (target the input inside the first text field after container)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(1).locator("input").fill("Test Group");

        // Radio buttons have default values (Global and Security) set in the dialog

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close and grid to refresh
        page.waitForTimeout(5000);

        // Scroll to end of list
        scrollToEndOfList();

        // Verify that the created group appears in the list (grid displays cn/name)
        page.locator("vaadin-grid").getByText("Test Group").waitFor();
    }

    public void editGroup() {
        // Scroll to end of list
        scrollToEndOfList();

        // Click on the group in the grid
        page.locator("vaadin-grid").getByText("Test Group").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/groups/*/details");
        page.waitForTimeout(1000);

        // Verify group data on details page (h3 displays the cn/name)
        page.locator("h3").getByText("Test Group").waitFor();

        // Click on "Update" menu item
        page.locator("vaadin-menu-bar-item").getByText("Update").or(page.locator("vaadin-menu-bar-item").getByText("Обновить")).or(page.locator("vaadin-menu-bar-item").getByText("Mettre à jour")).or(page.locator("vaadin-menu-bar-item").getByText("Aktualisieren")).click();

        // Wait for edit dialog to appear (target "Updating group" dialog specifically)
        page.waitForTimeout(500);
        var updateDialog = page.getByLabel("Updating group").or(page.getByLabel("Обновление группы")).or(page.getByLabel("Mise à jour du groupe")).or(page.getByLabel("Gruppe aktualisieren"));
        updateDialog.waitFor();

        // Update description (first text field, inside the specific dialog)
        updateDialog.locator("vaadin-text-field").nth(0).locator("input").fill("Updated Description");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Navigate back to groups list
        page.locator("vaadin-side-nav-item").getByText("Groups").or(page.locator("vaadin-side-nav-item").getByText("Группы")).or(page.locator("vaadin-side-nav-item").getByText("Groupes")).or(page.locator("vaadin-side-nav-item").getByText("Gruppen")).click();
        page.waitForURL("**/management/groups");
        page.waitForTimeout(1000);

        // Scroll to end of list
        scrollToEndOfList();

        // Verify that the group appears in the list
        page.locator("vaadin-grid").getByText("Test Group").waitFor();
    }

    public void deleteGroup() {
        // Scroll to end of list
        scrollToEndOfList();

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

    public void scrollToEndOfList() {
        // Scroll to the end of the vaadin-grid using mouse wheel
        page.locator("vaadin-grid").hover();
        for (int i = 0; i < 20; i++) {
            page.mouse().wheel(0, 500);
            page.waitForTimeout(100);
        }
    }
}
