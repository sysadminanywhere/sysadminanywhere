package com.sysadminanywhere.service;

import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
import com.sysadminanywhere.common.directory.model.GroupType;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupsService {

    private final LdapService ldapService;

    ResolveService<GroupEntry> resolveService = new ResolveService<>(GroupEntry.class);

    public GroupsService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @SneakyThrows
    public Page<GroupEntry> getAll(Pageable pageable, String filters) {
        List<Entry> result = ldapService.search("(&(objectClass=group)" + filters + ")", pageable.getSort());
        return resolveService.getADPage(result, pageable);
    }

    public List<GroupEntry> getAll(String filters) {
        List<Entry> result = ldapService.search("(&(objectClass=group)" + filters + ")");
        return resolveService.getADList(result);
    }

    public GroupEntry getByCN(String cn) {
        List<Entry> result = ldapService.search("(&(objectClass=group)(cn=" + cn + "))");
        Optional<Entry> entry = result.stream().findFirst();

        if (entry.isPresent())
            return resolveService.getADValue(entry.get());
        else
            return null;
    }

    @SneakyThrows
    public GroupEntry add(String distinguishedName, GroupEntry group, GroupScope groupScope, boolean isSecurity) {
        String dn;

        if (distinguishedName == null || distinguishedName.isEmpty()) {
            dn = "cn=" + group.getCn() + "," + ldapService.getUsersContainer();
        } else {
            dn = "cn=" + group.getCn() + "," + distinguishedName;
        }

        if (group.getSamAccountName() == null || group.getSamAccountName().isEmpty())
            group.setSamAccountName(group.getCn());

        group.setGroupType(getGroupType(groupScope, isSecurity));

        Entry entry = new DefaultEntry(
                dn,
                "sAMAccountName", group.getSamAccountName(),
                "objectClass:group",
                "groupType", String.valueOf(group.getGroupType()),
                "cn", group.getCn()
        );

        ldapService.add(entry);

        GroupEntry newGroup = getByCN(group.getCn());

        if (group.getDescription() != null && !group.getDescription().isEmpty())
            ldapService.updateProperty(newGroup.getDistinguishedName(), "location", group.getDescription());

        return newGroup;
    }

    public GroupEntry update(GroupEntry group) {
        ModifyRequest modifyRequest = resolveService.getModifyRequest(group, getByCN(group.getCn()));
        ldapService.update(modifyRequest);

        return getByCN(group.getCn());
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        Entry entry = new DefaultEntry(distinguishedName);
        ldapService.delete(entry);
    }

    public String getGroupTypeName(long groupType)
    {
        if (groupType == 2L) {
            return "Global distribution group";
        } else if (groupType == 4L) {
            return "Domain local distribution group";
        } else if (groupType == 8L) {
            return "Universal distribution group";
        } else if (groupType == -2147483646L) {
            return "Global security group";
        } else if (groupType == -2147483644L) {
            return "Domain local security group";
        } else if (groupType == -2147483640L) {
            return "Universal security group";
        } else if (groupType == -2147483643L) {
            return "BuiltIn Group";
        }
        return "";
    }

    public long getGroupType(GroupScope groupScope, boolean isSecurity)
    {
        long groupType = 0;
        if (isSecurity) {
            if (groupScope == GroupScope.Global)
                groupType = (GroupType.GLOBAL.getValue() | GroupType.SECURITY.getValue());
            if (groupScope == GroupScope.Local)
                groupType = (GroupType.DOMAIN_LOCAL.getValue() | GroupType.SECURITY.getValue());
            if (groupScope == GroupScope.Universal)
                groupType = (GroupType.UNIVERSAL.getValue() | GroupType.SECURITY.getValue());
        } else {
            if (groupScope == GroupScope.Global)
                groupType = GroupType.GLOBAL.getValue();
            if (groupScope == GroupScope.Local)
                groupType = GroupType.DOMAIN_LOCAL.getValue();
            if (groupScope == GroupScope.Universal)
                groupType = GroupType.UNIVERSAL.getValue();
        }
        return groupType;
    }

    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}