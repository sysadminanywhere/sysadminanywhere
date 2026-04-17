package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class ContainersViewStep {
    private final Page page;

    public ContainersViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Management menu item first (button with teams-nav-button class)
        page.locator("vaadin-button.teams-nav-button").nth(1).click();

        // Click on Containers menu item
        page.locator("vaadin-side-nav-item").getByText("Containers").or(page.locator("vaadin-side-nav-item").getByText("Контейнеры")).or(page.locator("vaadin-side-nav-item").getByText("Container")).or(page.locator("vaadin-side-nav-item").getByText("Conteneurs")).click();

        // Wait for ContainersView URL
        page.waitForURL("**/management/containers");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Containers") || title.equals("Контейнеры") || title.equals("Container") || title.equals("Conteneurs") : "Unexpected page title: " + title;
    }
}
