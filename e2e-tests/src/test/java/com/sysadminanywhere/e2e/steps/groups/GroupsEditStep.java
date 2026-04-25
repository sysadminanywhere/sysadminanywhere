package com.sysadminanywhere.e2e.steps.groups;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class GroupsEditStep {
    private final Page page;

    public GroupsEditStep(Page page) {
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

        // Click on "Update" menu item
        page.locator("vaadin-menu-bar-item").getByText("Update").or(page.locator("vaadin-menu-bar-item").getByText("Обновить")).or(page.locator("vaadin-menu-bar-item").getByText("Mettre à jour")).or(page.locator("vaadin-menu-bar-item").getByText("Aktualisieren")).click();

        // Wait for edit dialog to appear
        page.waitForTimeout(3000);

        // Find the "Updating group" dialog by role and name
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Updating group"));

        // Update description using text-field locator
        dialog.locator("vaadin-text-field").filter(new Locator.FilterOptions().setHasText("Description")).locator("input").fill("Updated Description");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Navigate back to groups list
        page.locator("vaadin-side-nav-item").getByText("Groups").or(page.locator("vaadin-side-nav-item").getByText("Группы")).or(page.locator("vaadin-side-nav-item").getByText("Groupes")).or(page.locator("vaadin-side-nav-item").getByText("Gruppen")).click();
        page.waitForURL("**/management/groups");
        page.waitForTimeout(1000);

        // Scroll to end of list
        page.locator("vaadin-grid").hover();
        for (int i = 0; i < 20; i++) {
            page.mouse().wheel(0, 500);
            page.waitForTimeout(100);
        }

        // Verify that the group appears in the list
        page.locator("vaadin-grid").getByText("Test Group").waitFor();
    }
}
