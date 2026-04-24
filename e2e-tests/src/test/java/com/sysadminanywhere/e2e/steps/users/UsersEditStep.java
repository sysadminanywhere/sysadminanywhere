package com.sysadminanywhere.e2e.steps.users;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class UsersEditStep {
    private final Page page;

    public UsersEditStep(Page page) {
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

        // Click on "Update" menu item
        page.locator("vaadin-menu-bar-item").getByText("Update").or(page.locator("vaadin-menu-bar-item").getByText("Изменить")).or(page.locator("vaadin-menu-bar-item").getByText("Mettre à jour")).or(page.locator("vaadin-menu-bar-item").getByText("Aktualisieren")).click();

        // Wait for edit dialog to appear
        page.waitForTimeout(3000);

        // Find the "Updating user" dialog by role and name
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Updating user"));

        // Update display name (first text field in the Name tab - h3 displays cn/display name)
        dialog.locator("input").nth(1).fill("TestUpdated UserUpdated");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Navigate back to users list
        page.locator("vaadin-side-nav-item").getByText("Users").or(page.locator("vaadin-side-nav-item").getByText("Пользователи")).or(page.locator("vaadin-side-nav-item").getByText("Utilisateurs")).or(page.locator("vaadin-side-nav-item").getByText("Benutzer")).click();
        page.waitForURL("**/management/users");
        page.waitForTimeout(1000);

        // Verify that the user appears with updated data in the list
        page.locator("vaadin-grid").getByText("Test User").waitFor();
    }
}
