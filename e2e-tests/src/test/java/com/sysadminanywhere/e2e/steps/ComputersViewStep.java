package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class ComputersViewStep {
    private final Page page;

    public ComputersViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Computers menu item
        page.locator("vaadin-side-nav-item").getByText("Computers").or(page.locator("vaadin-side-nav-item").getByText("Компьютеры")).or(page.locator("vaadin-side-nav-item").getByText("Ordinateurs")).or(page.locator("vaadin-side-nav-item").getByText("Computer")).click();

        // Wait for ComputersView URL
        page.waitForURL("**/management/computers");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Computers") || title.equals("Компьютеры") || title.equals("Ordinateurs") || title.equals("Computer") : "Unexpected page title: " + title;
    }
}
