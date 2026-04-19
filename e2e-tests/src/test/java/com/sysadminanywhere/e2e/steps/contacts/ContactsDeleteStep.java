package com.sysadminanywhere.e2e.steps.contacts;

import com.microsoft.playwright.Page;

public class ContactsDeleteStep {
    private final Page page;

    public ContactsDeleteStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on the contact in the grid
        page.locator("vaadin-grid").getByText("Test Contact").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/contacts/*/details");
        page.waitForTimeout(1000);

        // Verify contact data on details page (h3 displays the cn/display name)
        page.locator("h3").getByText("Test Contact").waitFor();

        // Click on "Delete" menu item
        page.locator("vaadin-menu-bar-item").getByText("Delete").or(page.locator("vaadin-menu-bar-item").getByText("Удалить")).or(page.locator("vaadin-menu-bar-item").getByText("Supprimer")).or(page.locator("vaadin-menu-bar-item").getByText("Löschen")).click();

        // Wait for delete confirmation dialog to appear
        page.waitForTimeout(500);
        page.locator("vaadin-confirm-dialog-overlay").waitFor();

        // Click on "Delete" button in confirmation dialog
        page.locator("vaadin-button").getByText("Delete").or(page.locator("vaadin-button").getByText("Удалить")).or(page.locator("vaadin-button").getByText("Supprimer")).or(page.locator("vaadin-button").getByText("Löschen")).click();

        // Wait for delete to complete and navigation back to contacts list
        page.waitForURL("**/management/contacts");
        page.waitForTimeout(2000);

        // Verify that the contact is no longer in the list
        assert page.locator("vaadin-grid").getByText("Test Contact").count() == 0 : "Contact 'Test Updated Contact' should not be in the list";
    }
}
