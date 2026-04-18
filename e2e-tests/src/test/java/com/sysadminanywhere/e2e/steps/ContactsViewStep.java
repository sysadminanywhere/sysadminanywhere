package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class ContactsViewStep {
    private final Page page;

    public ContactsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Contacts menu item
        page.locator("vaadin-side-nav-item").getByText("Contacts").or(page.locator("vaadin-side-nav-item").getByText("Контакты")).or(page.locator("vaadin-side-nav-item").getByText("Kontakte")).click();

        // Wait for ContactsView URL
        page.waitForURL("**/management/contacts");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Contacts") || title.equals("Контакты") || title.equals("Kontakte") : "Unexpected page title: " + title;
    }

    public void createNewContact() {
        // Click on the second menu bar item (the "New" button with plus icon)
        page.locator("vaadin-menu-bar").locator("vaadin-menu-bar-item").nth(1).click();

        // Wait for dialog to appear
        page.waitForTimeout(3000);

        // Wait for any dialog overlay to be visible
        page.locator("vaadin-dialog-overlay").first().waitFor();

        // Fill in display name (target the input inside the first text field after container)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(1).locator("input").fill("Test Contact");

        // Fill in first name (input inside second text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(2).locator("input").fill("Test");

        // Fill in initials (input inside third text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(3).locator("input").fill("TC");

        // Fill in last name (input inside fourth text field)
        page.locator("vaadin-dialog-overlay").locator("vaadin-text-field").nth(4).locator("input").fill("Contact");

        // Click on "Save" button
        page.locator("vaadin-button").getByText("Save").or(page.locator("vaadin-button").getByText("Сохранить")).or(page.locator("vaadin-button").getByText("Enregistrer")).or(page.locator("vaadin-button").getByText("Speichern")).click();

        // Wait for dialog to close
        page.waitForTimeout(2000);

        // Wait for grid to refresh
        page.waitForTimeout(2000);

        // Verify that the created contact appears in the list (grid displays cn/display name)
        page.locator("vaadin-grid").getByText("Test Contact").waitFor();
    }

    public void editContact() {
        // Click on the contact in the grid
        page.locator("vaadin-grid").getByText("Test Contact").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/contacts/*/details");
        page.waitForTimeout(1000);

        // Verify contact data on details page (h3 displays the cn/display name)
        page.locator("h3").getByText("Test Contact").waitFor();

        // Click on "Update" menu item
        page.locator("vaadin-menu-bar-item").getByText("Update").or(page.locator("vaadin-menu-bar-item").getByText("Обновить")).or(page.locator("vaadin-menu-bar-item").getByText("Mettre à jour")).or(page.locator("vaadin-menu-bar-item").getByText("Aktualisieren")).click();

        // Wait for edit dialog to appear (target "Updating contact" dialog specifically)
        page.waitForTimeout(500);
        var updateDialog = page.getByLabel("Updating contact").or(page.getByLabel("Обновление контакта")).or(page.getByLabel("Mise à jour du contact")).or(page.getByLabel("Kontakt aktualisieren"));
        updateDialog.waitFor();

        // Update display name (first text field in Name tab, inside the specific dialog)
        updateDialog.locator("vaadin-text-field").nth(0).locator("input").fill("Test Updated Contact");

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

    public void deleteContact() {
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
