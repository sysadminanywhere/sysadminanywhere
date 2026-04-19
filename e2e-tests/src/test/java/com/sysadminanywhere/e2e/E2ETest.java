package com.sysadminanywhere.e2e;

import com.sysadminanywhere.e2e.steps.AboutViewStep;
import com.sysadminanywhere.e2e.steps.AccountViewStep;
import com.sysadminanywhere.e2e.steps.AuditViewStep;
import com.sysadminanywhere.e2e.steps.AutomationsViewStep;
import com.sysadminanywhere.e2e.steps.ComputerReportsViewStep;
import com.sysadminanywhere.e2e.steps.ComputersViewStep;
import com.sysadminanywhere.e2e.steps.ContactsViewStep;
import com.sysadminanywhere.e2e.steps.ContainersViewStep;
import com.sysadminanywhere.e2e.steps.DashboardViewStep;
import com.sysadminanywhere.e2e.steps.DomainViewStep;
import com.sysadminanywhere.e2e.steps.GroupReportsViewStep;
import com.sysadminanywhere.e2e.steps.GroupsViewStep;
import com.sysadminanywhere.e2e.steps.IncidentsViewStep;
import com.sysadminanywhere.e2e.steps.InventoryHardwareViewStep;
import com.sysadminanywhere.e2e.steps.InventoryViewStep;
import com.sysadminanywhere.e2e.steps.LoginStep;
import com.sysadminanywhere.e2e.steps.MeViewStep;
import com.sysadminanywhere.e2e.steps.PrintersViewStep;
import com.sysadminanywhere.e2e.steps.ReportsViewStep;
import com.sysadminanywhere.e2e.steps.SettingsSettingsViewStep;
import com.sysadminanywhere.e2e.steps.SettingsViewStep;
import com.sysadminanywhere.e2e.steps.UsersReportsViewStep;
import com.sysadminanywhere.e2e.steps.UsersViewStep;
import com.sysadminanywhere.e2e.steps.computers.ComputersCreateStep;
import com.sysadminanywhere.e2e.steps.computers.ComputersDeleteStep;
import com.sysadminanywhere.e2e.steps.computers.ComputersEditStep;
import com.sysadminanywhere.e2e.steps.contacts.ContactsCreateStep;
import com.sysadminanywhere.e2e.steps.contacts.ContactsDeleteStep;
import com.sysadminanywhere.e2e.steps.contacts.ContactsEditStep;
import com.sysadminanywhere.e2e.steps.groups.GroupsCreateStep;
import com.sysadminanywhere.e2e.steps.groups.GroupsDeleteStep;
import com.sysadminanywhere.e2e.steps.groups.GroupsEditStep;
import com.sysadminanywhere.e2e.steps.reports.computers.ComputerReportsVerifyStep;
import com.sysadminanywhere.e2e.steps.reports.groups.GroupsReportsVerifyStep;
import com.sysadminanywhere.e2e.steps.reports.users.UsersReportsVerifyStep;
import com.sysadminanywhere.e2e.steps.users.UsersCreateStep;
import com.sysadminanywhere.e2e.steps.users.UsersDeleteStep;
import com.sysadminanywhere.e2e.steps.users.UsersEditStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class E2ETest extends BaseTest {

    @Test
    @EnabledIfSystemProperty(named = "e2e.tests.enabled", matches = "true")
    void testE2E() throws InterruptedException {
        Thread.sleep(60000);

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

        // Step 6: Navigate to UsersView
        UsersViewStep usersViewStep = new UsersViewStep(page);
        usersViewStep.execute();

        // Step 6.1: Create new user
        UsersCreateStep usersCreateStep = new UsersCreateStep(page);
        usersCreateStep.execute();

        // Step 6.2: Edit the created user
        UsersEditStep usersEditStep = new UsersEditStep(page);
        usersEditStep.execute();

        // Step 6.3: Delete the user
        UsersDeleteStep usersDeleteStep = new UsersDeleteStep(page);
        usersDeleteStep.execute();

        // Step 7: Navigate to ComputersView
        ComputersViewStep computersViewStep = new ComputersViewStep(page);
        computersViewStep.execute();

        // Step 7.1: Create new computer
        ComputersCreateStep computersCreateStep = new ComputersCreateStep(page);
        computersCreateStep.execute();

        // Step 7.2: Edit the created computer
        ComputersEditStep computersEditStep = new ComputersEditStep(page);
        computersEditStep.execute();

        // Step 7.3: Delete the computer
        ComputersDeleteStep computersDeleteStep = new ComputersDeleteStep(page);
        computersDeleteStep.execute();

        // Step 8: Navigate to GroupsView
        GroupsViewStep groupsViewStep = new GroupsViewStep(page);
        groupsViewStep.execute();

        // Step 8.1: Create new group
        GroupsCreateStep groupsCreateStep = new GroupsCreateStep(page);
        groupsCreateStep.execute();

        // Step 8.2: Edit the created group
        GroupsEditStep groupsEditStep = new GroupsEditStep(page);
        groupsEditStep.execute();

        // Step 8.3: Delete the group
        GroupsDeleteStep groupsDeleteStep = new GroupsDeleteStep(page);
        groupsDeleteStep.execute();

        // Step 9: Navigate to PrintersView
        PrintersViewStep printersViewStep = new PrintersViewStep(page);
        printersViewStep.execute();

        // Step 10: Navigate to ContactsView
        ContactsViewStep contactsViewStep = new ContactsViewStep(page);
        contactsViewStep.execute();

        // Step 10.1: Create new contact
        ContactsCreateStep contactsCreateStep = new ContactsCreateStep(page);
        contactsCreateStep.execute();

        // Step 10.2: Edit the created contact
        ContactsEditStep contactsEditStep = new ContactsEditStep(page);
        contactsEditStep.execute();

        // Step 10.3: Delete the contact
        ContactsDeleteStep contactsDeleteStep = new ContactsDeleteStep(page);
        contactsDeleteStep.execute();

        // Step 11: Navigate to IncidentsView
        IncidentsViewStep incidentsViewStep = new IncidentsViewStep(page);
        incidentsViewStep.execute();

        // Step 12: Navigate to AutomationsView
        AutomationsViewStep automationsViewStep = new AutomationsViewStep(page);
        automationsViewStep.execute();

        // Step 13: Navigate to InventoryView
        InventoryViewStep inventoryViewStep = new InventoryViewStep(page);
        inventoryViewStep.execute();

        // Step 14: Navigate to InventoryHardwareView
        InventoryHardwareViewStep inventoryHardwareViewStep = new InventoryHardwareViewStep(page);
        inventoryHardwareViewStep.execute();

        // Step 15: Navigate to ReportsView
        ReportsViewStep reportsViewStep = new ReportsViewStep(page);
        reportsViewStep.execute();

        // Step 16: Navigate to UsersReportsView
        UsersReportsViewStep usersReportsViewStep = new UsersReportsViewStep(page);
        usersReportsViewStep.execute();

        // Step 16.1: Verify all user reports
        UsersReportsVerifyStep usersReportsVerifyStep = new UsersReportsVerifyStep(page);
        usersReportsVerifyStep.execute();

        // Step 17: Navigate to ComputerReportsView
        ComputerReportsViewStep computerReportsViewStep = new ComputerReportsViewStep(page);
        computerReportsViewStep.execute();

        // Step 17.1: Verify all computer reports
        ComputerReportsVerifyStep computerReportsVerifyStep = new ComputerReportsVerifyStep(page);
        computerReportsVerifyStep.execute();

        // Step 18: Navigate to GroupReportsView
        GroupReportsViewStep groupReportsViewStep = new GroupReportsViewStep(page);
        groupReportsViewStep.execute();

        // Step 18.1: Verify all group reports
        GroupsReportsVerifyStep groupsReportsVerifyStep = new GroupsReportsVerifyStep(page);
        groupsReportsVerifyStep.execute();

        // Step 19: Navigate to AccountView
        AccountViewStep accountViewStep = new AccountViewStep(page);
        accountViewStep.execute();

        // Step 20: Navigate to MeView
        MeViewStep meViewStep = new MeViewStep(page);
        meViewStep.execute();

        // Step 21: Navigate to SettingsView
        SettingsViewStep settingsViewStep = new SettingsViewStep(page);
        settingsViewStep.execute();

        // Step 22: Navigate to SettingsSettingsView
        SettingsSettingsViewStep settingsSettingsViewStep = new SettingsSettingsViewStep(page);
        settingsSettingsViewStep.execute();

        // Step 23: Navigate to AboutView
        AboutViewStep aboutViewStep = new AboutViewStep(page);
        aboutViewStep.execute();
    }
}
