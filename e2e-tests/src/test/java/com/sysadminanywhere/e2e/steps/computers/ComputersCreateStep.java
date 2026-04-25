package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ComputersCreateStep {
    private final Page page;

    public ComputersCreateStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on the second menu bar item (the "New" button with plus icon)
        page.locator("vaadin-menu-bar").locator("vaadin-menu-bar-item").nth(1).click();

        // Wait for dialog to appear
        page.waitForTimeout(3000);

        // Find the "New computer" dialog by role and name
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("New computer"));

        // Fill in name (target the input inside the first text field after container)
        dialog.locator("input").nth(1).fill("Test Computer");

        // Fill in description (input inside second text field)
        dialog.locator("input").nth(2).fill("Test Description");

        // Fill in location (input inside third text field)
        dialog.locator("input").nth(3).fill("Test Location");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Wait for grid to refresh
        page.waitForTimeout(2000);

        // Verify that the created computer appears in the list (grid displays cn/name)
        page.locator("vaadin-grid").getByText("Test Computer").waitFor();
    }
}
