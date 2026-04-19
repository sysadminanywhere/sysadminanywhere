package com.sysadminanywhere.e2e.steps.users;

import com.microsoft.playwright.Page;

public class UsersCreateStep {
    private final Page page;

    public UsersCreateStep(Page page) {
        this.page = page;
    }

    public void execute() {
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
}
