package com.sysadminanywhere.e2e.steps.reports.computers;

import com.microsoft.playwright.Page;

public class ComputerReportsVerifyStep {
    private final Page page;

    public ComputerReportsVerifyStep(Page page) {
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
            page.waitForURL("**/reports/report?entry=computers&id=*");
            page.waitForTimeout(1000);

            // Navigate back to computer reports list
            page.locator("vaadin-side-nav-item").getByText("Computer reports").or(page.locator("vaadin-side-nav-item").getByText("Отчеты по компьютерам")).or(page.locator("vaadin-side-nav-item").getByText("Rapports ordinateurs")).or(page.locator("vaadin-side-nav-item").getByText("Computerberichte")).click();
            page.waitForURL("**/reports/computers");
            page.waitForTimeout(500);
        }
    }
}
