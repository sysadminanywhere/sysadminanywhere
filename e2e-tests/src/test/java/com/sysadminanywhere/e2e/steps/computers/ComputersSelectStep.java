package com.sysadminanywhere.e2e.steps.computers;

import com.microsoft.playwright.Page;

public class ComputersSelectStep {
    private final Page page;

    public ComputersSelectStep(Page page) {
        this.page = page;
    }

    public void execute() {
        // Click on the computer with name WIN-4VEK5HJAPSG in the grid
        page.locator("vaadin-grid").getByText("WIN-4VEK5HJAPSG").click();

        // Wait for navigation to details page
        page.waitForURL("**/management/computers/*/details");
        page.waitForTimeout(1000);

        // Verify computer data on details page (h3 displays the cn/name)
        page.locator("h3").getByText("WIN-4VEK5HJAPSG").waitFor();
    }
}
