package com.sysadminanywhere.domain;

import com.sysadminanywhere.common.directory.dto.OrganizationEmployee;
import com.sysadminanywhere.domain.OrganizationChart.Filters;
import com.sysadminanywhere.domain.OrganizationChart.ReportingIssue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationChartTest {
    @Test
    void officeFilterKeepsCrossCountryManagersWithoutCountingThem() {
        var director = employee("Director", null, "PL", "Warsaw", true);
        var manager = employee("Manager", dn("Director").toLowerCase(Locale.ROOT), "DE", "Berlin", false);
        var report = employee("Specialist", dn("Manager"), "DE", "Berlin", false);
        var peer = employee("Peer", dn("Director"), "PL", "Warsaw", false);
        var chart = new OrganizationChart(List.of(report, manager, director, peer));

        var result = chart.select(new Filters(null, "de", "berlin", null, null, false));

        assertThat(result.matches()).containsExactly(manager, report);
        assertThat(result.withManagers()).containsExactlyInAnyOrder(director, manager, report);
        assertThat(chart.select(new Filters("specialist", null, null, null, director, false)).matches()).containsExactly(report);
        assertThat(chart.reportsOf(director)).containsExactly(manager, peer);
    }

    @Test
    void invalidManagersAndCyclesDoNotHideEmployeesOrTheirReports() {
        var first = employee("First", dn("Second"), "", "", false);
        var second = employee("Second", dn("First"), "", "", false);
        var report = employee("Report", dn("Second"), "", "", false);
        var orphan = employee("Orphan", dn("Missing"), "DE", "Berlin", false);
        var chart = new OrganizationChart(List.of(first, second, report, orphan));

        assertThat(chart.roots()).containsExactly(first, orphan, second);
        assertThat(chart.issueOf(first)).isEqualTo(ReportingIssue.CYCLE);
        assertThat(chart.issueOf(second)).isEqualTo(ReportingIssue.CYCLE);
        assertThat(chart.issueOf(orphan)).isEqualTo(ReportingIssue.MISSING_MANAGER);
        assertThat(chart.managerOf(report)).isEqualTo(second);
        assertThat(chart.select(new Filters("", "", "", null, null, false)).matches()).containsExactly(first, report, second);
    }

    private static OrganizationEmployee employee(String name, String manager, String country, String office, boolean disabled) {
        return new OrganizationEmployee(UUID.randomUUID(), dn(name), name, name, name.toLowerCase(Locale.ROOT),
                "Engineer", "Operations", country, office, manager, disabled);
    }

    private static String dn(String name) {
        return "CN=" + name + ",OU=Test,DC=example,DC=com";
    }
}
