package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.GroupsServiceClient;
import com.sysadminanywhere.common.directory.dto.AddGroupDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.GroupScope;
import com.sysadminanywhere.common.directory.model.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupsService {

    private final LdapService ldapService;
    private final GroupsServiceClient groupsServiceClient;

    public GroupsService(LdapService ldapService, GroupsServiceClient groupsServiceClient) {
        this.ldapService = ldapService;
        this.groupsServiceClient = groupsServiceClient;
    }

    public Page<GroupEntry> getAll(Pageable pageable, String filters, String... attributes) {
        try {
            return groupsServiceClient.getAll(pageable, filters, attributes);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public List<GroupEntry> getAll(String filters, String... attributes) {
        try {
            return groupsServiceClient.getList(filters, attributes);
        } catch (Exception e) {
            return null;
        }
    }

    public List<GroupEntry> getAll() {
        List<EntryDto> list = ldapService.searchWithAttributes("(objectClass=group)",
                "cn", "grouptype");

        List<GroupEntry> items = new ArrayList<>();

        if(list != null) {
            for (EntryDto entryDto : list) {
                GroupEntry item = new GroupEntry();
                item.setCn(entryDto.getAttributes().get("cn").toString());
                item.setGroupType(Integer.parseInt(entryDto.getAttributes().get("grouptype").toString()));
                items.add(item);
            }
        }

        return items;
    }

    public GroupEntry getByCN(String cn) {
        return groupsServiceClient.getByCN(cn);
    }

    public GroupEntry add(String distinguishedName, GroupEntry group, GroupScope groupScope, boolean isSecurity) {
        return groupsServiceClient.add(new AddGroupDto(distinguishedName, group, groupScope, isSecurity));
    }

    public GroupEntry update(GroupEntry group) {
        return groupsServiceClient.update(group);
    }

    public void delete(String distinguishedName) {
        groupsServiceClient.delete(distinguishedName);
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