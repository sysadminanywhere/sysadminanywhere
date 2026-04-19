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
}
