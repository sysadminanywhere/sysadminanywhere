package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class AccountViewStep {
    private final Page page;

    public AccountViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Account menu item (button with teams-nav-button class, bottom menu)
        page.locator("vaadin-button.teams-nav-button").nth(6).click();

        // Wait for Account URL
        page.waitForURL("**/account/**");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Me") || title.equals("Профиль") || title.equals("Moi") || title.equals("Ich") : "Unexpected page title: " + title;
    }
}
