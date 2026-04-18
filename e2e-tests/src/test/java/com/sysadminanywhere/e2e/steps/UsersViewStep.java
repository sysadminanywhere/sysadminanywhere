package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class UsersViewStep {
    private final Page page;

    public UsersViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Users menu item
        page.locator("vaadin-side-nav-item").getByText("Users").or(page.locator("vaadin-side-nav-item").getByText("Пользователи")).or(page.locator("vaadin-side-nav-item").getByText("Utilisateurs")).or(page.locator("vaadin-side-nav-item").getByText("Benutzer")).click();

        // Wait for UsersView URL
        page.waitForURL("**/management/users");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Users") || title.equals("Пользователи") || title.equals("Utilisateurs") || title.equals("Benutzer") : "Unexpected page title: " + title;
    }

    public void createNewUser() {
        // Click on the second menu bar item (the "New" button with plus icon)
        page.locator("vaadin-menu-bar").locator("vaadin-menu-bar-item").nth(1).click();

        // Wait for dialog to appear
        page.waitForTimeout(3000);

        // Wait for any dialog overlay to be visible
        page.locator("vaadin-dialog-overlay").first().waitFor();

        // Fill in display name (target the input inside the first text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(1).locator("input").fill("Test User");

        // Fill in first name (input inside second text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(2).locator("input").fill("Test");

        // Fill in last name (input inside fourth text field - third is initials)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(4).locator("input").fill("User");

        // Fill in account name (input inside fifth text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(5).locator("input").fill("testuser");

        // Fill in password (input inside first password field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-password-field").nth(0).locator("input").fill("TestPassword123!");

        // Fill in password confirmation (input inside second password field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-password-field").nth(1).locator("input").fill("TestPassword123!");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for save to complete
        page.waitForTimeout(1000);

        // Verify that the created user appears in the list (grid displays cn/display name)
        page.locator("vaadin-grid").getByText("Test User").waitFor();
    }

    public void editUser() {
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

    public void deleteUser() {
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