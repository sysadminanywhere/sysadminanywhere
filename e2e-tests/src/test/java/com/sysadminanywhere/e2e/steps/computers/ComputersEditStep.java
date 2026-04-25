package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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

        // Wait for edit dialog to appear
        page.waitForTimeout(3000);

        // Find the "Updating computer" dialog by role and name
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Updating computer"));

        // Update description (first text field, inside the specific dialog)
        dialog.locator("input").nth(1).fill("Updated Description");

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
