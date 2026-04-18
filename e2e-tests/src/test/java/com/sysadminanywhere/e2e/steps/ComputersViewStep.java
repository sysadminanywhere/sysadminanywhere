package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class ComputersViewStep {
    private final Page page;

    public ComputersViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Computers menu item
        page.locator("vaadin-side-nav-item").getByText("Computers").or(page.locator("vaadin-side-nav-item").getByText("Компьютеры")).or(page.locator("vaadin-side-nav-item").getByText("Ordinateurs")).or(page.locator("vaadin-side-nav-item").getByText("Computer")).click();

        // Wait for ComputersView URL
        page.waitForURL("**/management/computers");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Computers") || title.equals("Компьютеры") || title.equals("Ordinateurs") || title.equals("Computer") : "Unexpected page title: " + title;
    }

    public void createNewComputer() {
        // Click on the second menu bar item (the "New" button with plus icon)
        page.locator("vaadin-menu-bar").locator("vaadin-menu-bar-item").nth(1).click();

        // Wait for dialog to appear
        page.waitForTimeout(3000);

        // Wait for any dialog overlay to be visible
        page.locator("vaadin-dialog-overlay").first().waitFor();

        // Fill in name (target the input inside the first text field after container)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(1).locator("input").fill("Test Computer");

        // Fill in description (input inside second text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(2).locator("input").fill("Test Description");

        // Fill in location (input inside third text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(3).locator("input").fill("Test Location");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Wait for grid to refresh
        page.waitForTimeout(2000);

        // Verify that the created computer appears in the list (grid displays cn/name)
        page.locator("vaadin-grid").getByText("Test Computer").waitFor();
    }

    public void editComputer() {
        // Click on the computer in the grid
        page.locator("vaadin-grid").getByText("Test Computer").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/computers/*/details");
        page.waitForTimeout(1000);

        // Verify computer data on details page (h3 displays the cn/name)
        page.locator("h3").getByText("Test Computer").waitFor();

        // Click on "Update" menu item
        page.locator("vaadin-menu-bar-item").getByText("Update").or(page.locator("vaadin-menu-bar-item").getByText("Обновить")).or(page.locator("vaadin-menu-bar-item").getByText("Mettre à jour")).or(page.locator("vaadin-menu-bar-item").getByText("Aktualisieren")).click();

        // Wait for edit dialog to appear (target "Updating computer" dialog specifically)
        page.waitForTimeout(500);
        var updateDialog = page.getByLabel("Updating computer").or(page.getByLabel("Обновление компьютера")).or(page.getByLabel("Mise à jour de l'ordinateur")).or(page.getByLabel("Computer aktualisieren"));
        updateDialog.waitFor();

        // Update description (first text field, inside the specific dialog)
        updateDialog.locator("vaadin-text-field").nth(0).locator("input").fill("Updated Description");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Navigate back to computers list
        page.locator("vaadin-side-nav-item").getByText("Computers").or(page.locator("vaadin-side-nav-item").getByText("Компьютеры")).or(page.locator("vaadin-side-nav-item").getByText("Ordinateurs")).or(page.locator("vaadin-side-nav-item").getByText("Computer")).click();
        page.waitForURL("**/management/computers");
        page.waitForTimeout(1000);

        // Verify that the computer appears in the list
        page.locator("vaadin-grid").getByText("Test Computer").waitFor();
    }

    public void deleteComputer() {
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
