package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class ComputerReportsViewStep {
    private final Page page;

    public ComputerReportsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Computer reports menu item
        page.locator("vaadin-side-nav-item").getByText("Computer reports").or(page.locator("vaadin-side-nav-item").getByText("Отчеты по компьютерам")).or(page.locator("vaadin-side-nav-item").getByText("Rapports ordinateurs")).or(page.locator("vaadin-side-nav-item").getByText("Computerberichte")).click();

        // Wait for ComputerReportsView URL
        page.waitForURL("**/reports/computers");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Computer reports") || title.equals("Отчеты по компьютерам") || title.equals("Rapports ordinateurs") || title.equals("Computerberichte") : "Unexpected page title: " + title;
    }
}
