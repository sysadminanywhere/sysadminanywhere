package com.sysadminanywhere.domain;

import com.sysadminanywhere.common.directory.dto.OrganizationEmployee;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class OrganizationChart {
    public enum ReportingIssue { MISSING_MANAGER, CYCLE }

    public record Filters(String search, String country, String office, String department,
                          OrganizationEmployee manager, boolean includeDisabled) {
    }

    public record Selection(List<OrganizationEmployee> matches, Set<OrganizationEmployee> withManagers) {
    }

    private final List<OrganizationEmployee> employees;
    private final Map<OrganizationEmployee, OrganizationEmployee> managers = new HashMap<>();
    private final Map<OrganizationEmployee, ReportingIssue> issues = new HashMap<>();
    private final Map<OrganizationEmployee, List<OrganizationEmployee>> reports = new HashMap<>();
    private final List<OrganizationEmployee> roots;

    public OrganizationChart(List<OrganizationEmployee> source) {
        Set<String> seen = new HashSet<>();
        employees = source.stream()
                .filter(employee -> seen.add(identity(employee)))
                .sorted(Comparator.comparing(OrganizationChart::name, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(OrganizationChart::identity))
                .toList();
        Map<LdapName, OrganizationEmployee> byDn = new HashMap<>();
        for (OrganizationEmployee employee : employees) {
            LdapName dn = dn(employee.distinguishedName());
            if (dn != null) {
                byDn.putIfAbsent(dn, employee);
            }
        }
        for (OrganizationEmployee employee : employees) {
            if (text(employee.managerDn()).isEmpty()) {
                continue;
            }
            OrganizationEmployee manager = byDn.get(dn(employee.managerDn()));
            if (manager == null) {
                issues.put(employee, ReportingIssue.MISSING_MANAGER);
            } else {
                managers.put(employee, manager);
            }
        }
        detachCycles();
        roots = employees.stream().filter(employee -> !managers.containsKey(employee)).toList();
        for (OrganizationEmployee employee : employees) {
            OrganizationEmployee manager = managers.get(employee);
            if (manager != null) {
                reports.computeIfAbsent(manager, ignored -> new ArrayList<>()).add(employee);
            }
        }
    }

    public List<OrganizationEmployee> employees() {
        return employees;
    }

    public List<OrganizationEmployee> roots() {
        return roots;
    }

    public OrganizationEmployee managerOf(OrganizationEmployee employee) {
        return managers.get(employee);
    }

    public List<OrganizationEmployee> reportsOf(OrganizationEmployee employee) {
        return List.copyOf(reports.getOrDefault(employee, List.of()));
    }

    public ReportingIssue issueOf(OrganizationEmployee employee) {
        return issues.get(employee);
    }

    public Selection select(Filters filters) {
        String search = text(filters.search()).toLowerCase(Locale.ROOT);
        Set<OrganizationEmployee> branch = descendants(filters.manager());
        List<OrganizationEmployee> matches = employees.stream()
                .filter(employee -> filters.includeDisabled() || !employee.disabled())
                .filter(employee -> filters.manager() == null || branch.contains(employee))
                .filter(employee -> matchesValue(filters.country(), employee.country()))
                .filter(employee -> matchesValue(filters.office(), employee.office()))
                .filter(employee -> matchesValue(filters.department(), employee.department()))
                .filter(employee -> matchesSearch(employee, search))
                .toList();
        Set<OrganizationEmployee> withManagers = new HashSet<>();
        for (OrganizationEmployee employee : matches) {
            for (OrganizationEmployee node = employee; node != null && withManagers.add(node); node = managers.get(node)) {
                // Each manager path is visited at most once.
            }
        }
        return new Selection(matches, Set.copyOf(withManagers));
    }

    private Set<OrganizationEmployee> descendants(OrganizationEmployee manager) {
        if (manager == null) {
            return Set.of();
        }
        Set<OrganizationEmployee> descendants = new HashSet<>();
        var queue = new ArrayDeque<>(reportsOf(manager));
        while (!queue.isEmpty()) {
            OrganizationEmployee employee = queue.removeFirst();
            descendants.add(employee);
            queue.addAll(reports.getOrDefault(employee, List.of()));
        }
        return descendants;
    }

    private void detachCycles() {
        Set<OrganizationEmployee> visited = new HashSet<>();
        for (OrganizationEmployee employee : employees) {
            Set<OrganizationEmployee> path = new LinkedHashSet<>();
            OrganizationEmployee current = employee;
            while (current != null && !visited.contains(current) && path.add(current)) {
                current = managers.get(current);
            }
            if (path.contains(current)) {
                OrganizationEmployee first = current;
                do {
                    issues.put(current, ReportingIssue.CYCLE);
                    current = managers.remove(current);
                } while (!first.equals(current));
            }
            visited.addAll(path);
        }
    }

    public static String name(OrganizationEmployee employee) {
        String name = text(employee.name());
        return name.isEmpty() ? text(employee.cn()) : name;
    }

    public static String text(String value) {
        return value == null ? "" : value.strip();
    }

    private static String identity(OrganizationEmployee employee) {
        if (employee.id() != null) {
            return employee.id().toString();
        }
        // The LDAP DN is the fallback for directories without objectGUID.
        return text(employee.distinguishedName()).toLowerCase(Locale.ROOT);
    }

    private static boolean matchesValue(String selection, String value) {
        return selection == null || selection.equalsIgnoreCase(text(value));
    }

    private static boolean matchesSearch(OrganizationEmployee employee, String search) {
        return name(employee).toLowerCase(Locale.ROOT).contains(search)
                || text(employee.cn()).toLowerCase(Locale.ROOT).contains(search)
                || text(employee.login()).toLowerCase(Locale.ROOT).contains(search)
                || text(employee.title()).toLowerCase(Locale.ROOT).contains(search);
    }

    private static LdapName dn(String value) {
        if (text(value).isEmpty()) {
            return null;
        }
        try {
            return new LdapName(value);
        } catch (InvalidNameException ignored) {
            return null;
        }
    }
}
