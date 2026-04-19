package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersEditStep {
    private final Page page;

    public ComputersEditStep(Page page) {
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
}
