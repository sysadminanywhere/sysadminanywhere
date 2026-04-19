package com.sysadminanywhere.e2e.steps.reports.groups;

import com.microsoft.playwright.Page;

public class GroupsReportsVerifyStep {
    private final Page page;

    public GroupsReportsVerifyStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Get all report items in the list box
        var listBox = page.locator("vaadin-list-box");
        int reportCount = listBox.locator("vaadin-item").count();

        // Iterate through each report and verify it opens
        for (int i = 0; i < reportCount; i++) {
            // Click on the report item
            listBox.locator("vaadin-item").nth(i).click();

            // Wait for report preview to load
            page.waitForURL("**/reports/report?entry=groups&id=*");
            page.waitForTimeout(1000);

            // Navigate back to group reports list
            page.locator("vaadin-side-nav-item").getByText("Group reports").or(page.locator("vaadin-side-nav-item").getByText("Отчеты по группам")).or(page.locator("vaadin-side-nav-item").getByText("Rapports groupes")).or(page.locator("vaadin-side-nav-item").getByText("Gruppenberichte")).click();
            page.waitForURL("**/reports/groups");
            page.waitForTimeout(500);
        }
    }
}
