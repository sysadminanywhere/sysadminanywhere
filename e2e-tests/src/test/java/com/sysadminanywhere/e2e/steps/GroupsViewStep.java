package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class GroupsViewStep {
    private final Page page;

    public GroupsViewStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on Groups menu item
        page.locator("vaadin-side-nav-item").getByText("Groups").or(page.locator("vaadin-side-nav-item").getByText("Группы")).or(page.locator("vaadin-side-nav-item").getByText("Groupes")).or(page.locator("vaadin-side-nav-item").getByText("Gruppen")).click();

        // Wait for GroupsView URL
        page.waitForURL("**/management/groups");

        // Wait for title to update
        page.waitForTimeout(1000);

        // Check page title
        String title = page.title();
        assert title.equals("Groups") || title.equals("Группы") || title.equals("Groupes") || title.equals("Gruppen") : "Unexpected page title: " + title;
    }
}
