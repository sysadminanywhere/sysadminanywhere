package com.sysadminanywhere.e2e;

import com.microsoft.playwright.Locator.GetByTextOptions;
import com.sysadminanywhere.e2e.steps.LoginStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@EnabledIfSystemProperty(named = "e2e.smoke.enabled", matches = "true")
class SmokeTest extends BaseTest {
    @Test
    @EnabledIfSystemProperty(named = "e2e.organization.enabled", matches = "true")
    void organizationSearchKeepsTheManagementChainAndGroupsByOffice() {
        context.addInitScript("localStorage.setItem('sysadmin-onboarding-tour', 'completed')");
        new LoginStep(page).execute();
        String baseUrl = System.getenv().getOrDefault("E2E_BASE_URL", "http://localhost:8080");
        var exact = new GetByTextOptions().setExact(true);

        page.navigate(baseUrl + "/management/users/organization");
        page.locator("#organization-search input").fill("Support Specialist");
        var tree = page.locator("#organization-tree");
        assertThat(tree.getByText("Smoke Director", exact)).isVisible();
        assertThat(tree.getByText("Smoke Specialist", exact)).isVisible();
        assertThat(tree.getByText("Smoke Analyst", exact)).hasCount(0);

        page.locator("vaadin-tab").getByText("By office", exact).click();
        assertThat(tree.getByText("Berlin", exact).first()).isVisible();
        assertThat(tree.getByText("Smoke Specialist", exact)).isVisible();
        assertThat(tree.getByText("Smoke Director", exact)).hasCount(0);
    }

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
