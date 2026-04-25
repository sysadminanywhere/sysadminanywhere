package com.sysadminanywhere.e2e.steps.users;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

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

        // Find the "New user" dialog by role and name
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("New user"));

        // Fill in all input elements in the dialog
        dialog.locator("input").nth(1).fill("Test User");
        dialog.locator("input").nth(2).fill("Test");
        dialog.locator("input").nth(3).fill("");
        dialog.locator("input").nth(4).fill("User");
        dialog.locator("input").nth(5).fill("testuser");
        dialog.locator("input").nth(6).fill("TestPassword123!");
        dialog.locator("input").nth(7).fill("TestPassword123!");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for save to complete
        page.waitForTimeout(1000);

        // Verify that the created user appears in the list (grid displays cn/display name)
        page.locator("vaadin-grid").getByText("Test User").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    }
}
