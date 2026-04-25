package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class LoginStep {
    private final Page page;

    public LoginStep(Page page) {
        this.page = page;
    }

    public void execute() {
        page.navigate("http://localhost:8080/");

        page.waitForLoadState();

        page.locator("input[name='username']").first().fill("admin");
        page.locator("input[name='password']").first().fill("Secret2#");
        page.locator("vaadin-login-overlay vaadin-button").first().click();

        page.waitForURL("**/");
    }
}
