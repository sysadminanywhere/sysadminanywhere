package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

import java.util.Objects;

public class LoginStep {
    private final Page page;

    public LoginStep(Page page) {
        this.page = page;
    }

    public void execute() {
        page.navigate(System.getenv().getOrDefault("E2E_BASE_URL", "http://localhost:8080") + "/");

        page.waitForLoadState();

        page.locator("input[name='username']").first().fill(Objects.requireNonNull(System.getenv("E2E_USERNAME"), "E2E_USERNAME"));
        page.locator("input[name='password']").first().fill(Objects.requireNonNull(System.getenv("E2E_PASSWORD"), "E2E_PASSWORD"));
        page.locator("vaadin-login-overlay vaadin-button").first().click();

        page.waitForURL("**/");
    }
}
