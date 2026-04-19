package com.sysadminanywhere.e2e.steps.reports.users;

import com.microsoft.playwright.Page;

public class UsersReportsVerifyStep {
    private final Page page;

    public UsersReportsVerifyStep(Page page) {
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
            page.waitForURL("**/reports/report?entry=users&id=*");
            page.waitForTimeout(1000);

            // Navigate back to users reports list
            page.locator("vaadin-side-nav-item").getByText("Users reports").or(page.locator("vaadin-side-nav-item").getByText("Отчеты по пользователям")).or(page.locator("vaadin-side-nav-item").getByText("Rapports utilisateurs")).or(page.locator("vaadin-side-nav-item").getByText("Benutzerberichte")).click();
            page.waitForURL("**/reports/users");
            page.waitForTimeout(500);
        }
    }
}
