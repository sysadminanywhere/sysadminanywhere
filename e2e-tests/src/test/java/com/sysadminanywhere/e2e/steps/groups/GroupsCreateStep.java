package com.sysadminanywhere.e2e.steps.groups;

import com.microsoft.playwright.Page;

public class GroupsCreateStep {
    private final Page page;

    public GroupsCreateStep(Page page) {
        this.page = page;
    }

    public void execute() {
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
        page.locator("vaadin-grid").hover();
        for (int i = 0; i < 20; i++) {
            page.mouse().wheel(0, 500);
            page.waitForTimeout(100);
        }

        // Verify that the created group appears in the list (grid displays cn/name)
        page.locator("vaadin-grid").getByText("Test Group").waitFor();
    }
}
