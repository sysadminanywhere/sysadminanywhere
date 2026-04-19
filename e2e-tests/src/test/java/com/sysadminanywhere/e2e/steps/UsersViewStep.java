package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class UsersViewStep {
    private final Page page;

    public UsersViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Users menu item
        page.locator("vaadin-side-nav-item").getByText("Users").or(page.locator("vaadin-side-nav-item").getByText("Пользователи")).or(page.locator("vaadin-side-nav-item").getByText("Utilisateurs")).or(page.locator("vaadin-side-nav-item").getByText("Benutzer")).click();

        // Wait for UsersView URL
        page.waitForURL("**/management/users");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Users") || title.equals("Пользователи") || title.equals("Utilisateurs") || title.equals("Benutzer") : "Unexpected page title: " + title;
    }

}