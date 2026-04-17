package com.sysadminanywhere.e2e;

import com.sysadminanywhere.e2e.steps.AuditViewStep;
import com.sysadminanywhere.e2e.steps.ContainersViewStep;
import com.sysadminanywhere.e2e.steps.DashboardViewStep;
import com.sysadminanywhere.e2e.steps.DomainViewStep;
import com.sysadminanywhere.e2e.steps.LoginStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class E2ETest extends BaseTest {

    @Test
    @EnabledIfSystemProperty(named = "e2e.tests.enabled", matches = "true")
    void testE2E() throws InterruptedException {
        Thread.sleep(40000);

        // Step 1: Login
        LoginStep loginStep = new LoginStep(page);
        loginStep.execute();

        // Step 2: Check DashboardView
        DashboardViewStep dashboardViewStep = new DashboardViewStep(page);
        dashboardViewStep.execute();

        // Step 3: Navigate to DomainView
        DomainViewStep domainViewStep = new DomainViewStep(page);
        domainViewStep.execute();

        // Step 4: Navigate to AuditView
        AuditViewStep auditViewStep = new AuditViewStep(page);
        auditViewStep.execute();

        // Step 5: Navigate to ContainersView
        ContainersViewStep containersViewStep = new ContainersViewStep(page);
        containersViewStep.execute();
    }
}
