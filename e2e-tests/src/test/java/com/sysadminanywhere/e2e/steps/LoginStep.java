package com.sysadminanywhere.e2e.steps;

import com.microsoft.playwright.Page;

public class LoginStep {
    private final Page page;

    public LoginStep(Page page) {
        this.page = page;
    }

    public void execute() {
        page.navigate("http://localhost:8080/");

        page.locator("vaadin-login-overlay-wrapper vaadin-login-form vaadin-text-field input").fill("admin");
        page.locator("vaadin-login-overlay-wrapper vaadin-login-form vaadin-password-field input").fill("Secret2#");
        page.locator("vaadin-login-overlay-wrapper vaadin-login-form vaadin-password-field input").press("Enter");

        page.waitForURL("http://localhost:8080/");
    }
}
