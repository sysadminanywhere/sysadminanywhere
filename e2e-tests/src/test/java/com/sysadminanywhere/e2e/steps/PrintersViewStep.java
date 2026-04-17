package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class PrintersViewStep {
    private final Page page;

    public PrintersViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Printers menu item
        page.locator("vaadin-side-nav-item").getByText("Printers").or(page.locator("vaadin-side-nav-item").getByText("Принтеры")).or(page.locator("vaadin-side-nav-item").getByText("Imprimantes")).or(page.locator("vaadin-side-nav-item").getByText("Drucker")).click();

        // Wait for PrintersView URL
        page.waitForURL("**/management/printers");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Printers") || title.equals("Принтеры") || title.equals("Imprimantes") || title.equals("Drucker") : "Unexpected page title: " + title;
    }
}
