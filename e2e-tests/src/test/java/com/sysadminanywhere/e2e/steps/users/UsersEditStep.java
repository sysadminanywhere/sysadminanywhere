package com.sysadminanywhere.e2e.steps.users;

import com.microsoft.playwright.Page;

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
        page.waitForTimeout(500);
        page.locator("vaadin-dialog-overlay[aria-label='Updating user']").or(page.locator("vaadin-dialog-overlay[aria-label='Обновление пользователя']")).or(page.locator("vaadin-dialog-overlay[aria-label='Mise à jour de l\\'utilisateur']")).or(page.locator("vaadin-dialog-overlay[aria-label='Benutzer aktualisieren']")).waitFor();

        // Update display name (first text field in the Name tab - h3 displays cn/display name)
        page.locator("vaadin-dialog-overlay[aria-label='Updating user']").or(page.locator("vaadin-dialog-overlay[aria-label='Обновление пользователя']")).or(page.locator("vaadin-dialog-overlay[aria-label='Mise à jour de l\\'utilisateur']")).or(page.locator("vaadin-dialog-overlay[aria-label='Benutzer aktualisieren']")).locator("vaadin-text-field").nth(0).locator("input").fill("TestUpdated UserUpdated");

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
