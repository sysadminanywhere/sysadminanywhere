package com.sysadminanywhere.service;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.common.directory.model.UserAccountControls;
import com.sysadminanywhere.model.SecurityAuditSnapshot;
import com.sysadminanywhere.model.SecurityFinding;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SecurityAuditService {
    private static final Set<String> PROTECTED_GROUPS = Set.of(
            "administrators", "domain admins", "enterprise admins", "schema admins",
            "domain controllers", "account operators", "server operators", "backup operators",
            "print operators", "dnsadmins", "key admins", "enterprise key admins");

    private static final String[] USER_ATTRIBUTES = {
            "cn", "distinguishedname", "displayname", "samaccountname", "mail", "manager",
            "memberof", "admincount", "serviceprincipalname", "useraccountcontrol"};
    private static final String[] GROUP_ATTRIBUTES = {
            "cn", "distinguishedname", "admincount", "member", "memberof", "grouptype"};
    private static final String[] COMPUTER_ATTRIBUTES = {
            "cn", "distinguishedname", "dnshostname", "serviceprincipalname", "useraccountcontrol"};

    private final UsersService usersService;
    private final GroupsService groupsService;
    private final ComputersService computersService;

    public SecurityAuditService(UsersService usersService, GroupsService groupsService, ComputersService computersService) {
        this.usersService = usersService;
        this.groupsService = groupsService;
        this.computersService = computersService;
    }

    public SecurityAuditSnapshot scan() {
        LocalDateTime checkedAt = LocalDateTime.now();
        try {
            List<UserEntry> users = usersService.getAll("", USER_ATTRIBUTES);
            List<GroupEntry> groups = groupsService.getAll("", GROUP_ATTRIBUTES);
            List<ComputerEntry> computers = computersService.getAll("", COMPUTER_ATTRIBUTES);
            if (users == null || groups == null || computers == null) {
                throw new IllegalStateException("Directory service is unavailable");
            }
            List<SecurityFinding> findings = new ArrayList<>();

            List<UserEntry> privilegedUsers = users.stream().filter(this::isPrivilegedUser).toList();
            privilegedUsers.forEach(user -> findings.add(new SecurityFinding(
                    "PRIVILEGED_USER", "HIGH", "USER", display(user.getDisplayName(), user.getCn()),
                    user.getDistinguishedName(), "adminCount=1 or membership in a protected group")));

            List<GroupEntry> privilegedGroups = groups.stream().filter(this::isPrivilegedGroup).toList();
            privilegedGroups.forEach(group -> findings.add(new SecurityFinding(
                    "PRIVILEGED_GROUP", "HIGH", "GROUP", group.getCn(), group.getDistinguishedName(),
                    "Protected or adminCount group; members: " + size(group.getMembers()))));

            List<UserEntry> spnUsers = users.stream().filter(user -> hasText(user.getServicePrincipalName())).toList();
            spnUsers.forEach(user -> findings.add(new SecurityFinding(
                    "USER_SPN", "WARNING", "USER", display(user.getDisplayName(), user.getCn()),
                    user.getDistinguishedName(), "Service principal name: " + user.getServicePrincipalName())));

            List<ComputerEntry> spnComputers = computers.stream().filter(computer ->
                    computer.getServicePrincipalNames() != null && !computer.getServicePrincipalNames().isEmpty()).toList();
            spnComputers.forEach(computer -> findings.add(new SecurityFinding(
                    "COMPUTER_SPN", "INFO", "COMPUTER", display(computer.getDnsHostName(), computer.getCn()),
                    computer.getDistinguishedName(), "Service principal names: " + size(computer.getServicePrincipalNames()))));

            List<UserEntry> missingContact = users.stream().filter(user ->
                    !hasText(user.getEmailAddress()) || !hasText(user.getManager())).toList();
            missingContact.forEach(user -> findings.add(new SecurityFinding(
                    "USER_HYGIENE", "WARNING", "USER", display(user.getDisplayName(), user.getCn()),
                    user.getDistinguishedName(), missingContactDetails(user))));

            users.stream().filter(this::hasWeakAuthenticationSetting).forEach(user -> findings.add(new SecurityFinding(
                    "WEAK_AUTHENTICATION", "HIGH", "USER", display(user.getDisplayName(), user.getCn()),
                    user.getDistinguishedName(), weakAuthenticationDetails(user))));

            return new SecurityAuditSnapshot(checkedAt, privilegedUsers.size(), privilegedGroups.size(),
                    spnUsers.size(), spnComputers.size(), missingContact.size(), findings, null);
        } catch (Exception exception) {
            return new SecurityAuditSnapshot(checkedAt, 0, 0, 0, 0, 0, Collections.emptyList(),
                    exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
        }
    }

    private boolean isPrivilegedUser(UserEntry user) {
        if (user.getAdminCount() == 1) return true;
        return user.getMemberOf() != null && user.getMemberOf().stream().anyMatch(this::isProtectedGroupDn);
    }

    private boolean isPrivilegedGroup(GroupEntry group) {
        return group.getAdminCount() == 1 || (group.getCn() != null && PROTECTED_GROUPS.contains(group.getCn().toLowerCase(Locale.ROOT)));
    }

    private boolean isProtectedGroupDn(String distinguishedName) {
        if (distinguishedName == null) return false;
        String normalized = distinguishedName.toLowerCase(Locale.ROOT);
        return PROTECTED_GROUPS.stream().anyMatch(name -> normalized.contains("cn=" + name));
    }

    private String missingContactDetails(UserEntry user) {
        List<String> missing = new ArrayList<>();
        if (!hasText(user.getEmailAddress())) missing.add("email");
        if (!hasText(user.getManager())) missing.add("manager");
        return "Missing: " + String.join(", ", missing);
    }

    private boolean hasWeakAuthenticationSetting(UserEntry user) {
        int control = user.getUserAccountControl();
        return hasControl(control, UserAccountControls.USE_DES_KEY_ONLY)
                || hasControl(control, UserAccountControls.DONT_REQUIRE_PREAUTH)
                || hasControl(control, UserAccountControls.PASSWD_NOTREQD)
                || hasControl(control, UserAccountControls.DONT_EXPIRE_PASSWD);
    }

    private String weakAuthenticationDetails(UserEntry user) {
        List<String> settings = new ArrayList<>();
        int control = user.getUserAccountControl();
        if (hasControl(control, UserAccountControls.USE_DES_KEY_ONLY)) settings.add("DES-only encryption");
        if (hasControl(control, UserAccountControls.DONT_REQUIRE_PREAUTH)) settings.add("Kerberos pre-authentication disabled");
        if (hasControl(control, UserAccountControls.PASSWD_NOTREQD)) settings.add("password not required");
        if (hasControl(control, UserAccountControls.DONT_EXPIRE_PASSWD)) settings.add("password never expires");
        return "Weak account settings: " + String.join(", ", settings);
    }

    private boolean hasControl(int value, UserAccountControls control) {
        return (value & control.getValue()) == control.getValue();
    }

    private String display(String preferred, String fallback) {
        return hasText(preferred) ? preferred : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int size(List<?> values) {
        return values == null ? 0 : values.size();
    }

}
