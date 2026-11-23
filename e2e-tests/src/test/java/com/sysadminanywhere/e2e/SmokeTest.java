package com.sysadminanywhere.e2e;

import com.microsoft.playwright.Locator.GetByTextOptions;
import com.sysadminanywhere.e2e.steps.LoginStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@EnabledIfSystemProperty(named = "e2e.smoke.enabled", matches = "true")
class SmokeTest extends BaseTest {
    @Test
    void signsInSearchesDirectoryAndReadsInventory() {
        context.addInitScript("localStorage.setItem('sysadmin-onboarding-tour', 'completed')");
        new LoginStep(page).execute();
        String baseUrl = System.getenv().getOrDefault("E2E_BASE_URL", "http://localhost:8080");
        String searchName = System.getenv().getOrDefault("E2E_SEARCH_NAME", "Smoke Operator");
        String hardwareName = System.getenv().getOrDefault("E2E_HARDWARE_NAME", "Smoke Processor");

        page.navigate(baseUrl + "/domain/search");
        var query = page.locator(".global-search-toolbar vaadin-text-field input");
        query.fill(searchName);
        page.locator(".global-search-toolbar vaadin-button").click();
        var exact = new GetByTextOptions().setExact(true);
        assertThat(page.locator(".global-search-results vaadin-grid").getByText(searchName, exact)).isVisible();

        page.navigate(baseUrl + "/inventory/hardware");
        assertThat(page.locator("vaadin-grid").getByText(hardwareName, exact)).isVisible();
    }
}
