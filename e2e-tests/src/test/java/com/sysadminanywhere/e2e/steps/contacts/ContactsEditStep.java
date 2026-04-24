package com.sysadminanywhere.e2e.steps.contacts;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ContactsEditStep {
    private final Page page;

    public ContactsEditStep(Page page) {
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

        // Click on "Update" menu item
        page.locator("vaadin-menu-bar-item").getByText("Update").or(page.locator("vaadin-menu-bar-item").getByText("Обновить")).or(page.locator("vaadin-menu-bar-item").getByText("Mettre à jour")).or(page.locator("vaadin-menu-bar-item").getByText("Aktualisieren")).click();

        // Wait for edit dialog to appear
        page.waitForTimeout(3000);

        // Find the "Updating contact" dialog by role and name
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Updating contact"));

        // Update display name (first text field in Name tab, inside the specific dialog)
        dialog.locator("input").nth(1).fill("Test Updated Contact");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Navigate back to contacts list
        page.locator("vaadin-side-nav-item").getByText("Contacts").or(page.locator("vaadin-side-nav-item").getByText("Контакты")).or(page.locator("vaadin-side-nav-item").getByText("Kontakte")).click();
        page.waitForURL("**/management/contacts");
        page.waitForTimeout(1000);

        // Verify that the contact appears in the list
        page.locator("vaadin-grid").getByText("Test Contact").waitFor();
    }
}
