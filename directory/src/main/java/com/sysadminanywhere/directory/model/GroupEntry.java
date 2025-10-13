package com.sysadminanywhere.directory.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupEntry {

    @NotNull
    @AD(name = "cn", IsReadOnly = true)
    private String cn;

    @AD(name = "whencreated", IsReadOnly = true)
    private LocalDateTime created;

    @AD(name = "description")
    private String description;

    @NotNull
    @AD(name = "distinguishedname", IsReadOnly = true)
    private String distinguishedName;

    @AD(name = "whenchanged", IsReadOnly = true)
    private LocalDateTime modified;

    @AD(name = "objectcategory", IsReadOnly = true)
    private String objectCategory;

    @AD(name = "objectclass", IsReadOnly = true)
    private List<String> objectClass;

    @AD(name = "objectguid", IsReadOnly = true)
    private UUID objectGUID;

    @AD(name = "objectsid", IsReadOnly = true)
    private ADSID sid;

    @AD(name = "samaccountname")
    private String samAccountName;

    @AD(name = "managedby")
    private String managedBy;

    @AD(name = "memberof", IsReadOnly = true)
    private List<String> memberOf;

    @AD(name = "member", IsReadOnly = true)
    private List<String> members;

    @AD(name = "grouptype")
    private long groupType;

    @AD(name = "iscriticalsystemobject")
    private boolean isCriticalSystemObject;

    @AD(name = "samaccounttype")
    private int samAccountType;

    @AD(name = "systemflags")
    private int systemFlags;

    @AD(name = "adspath")
    private String adsPath;

    @AD(name = "name")
    private String name;

    @AD(name = "instancetype")
    private int instanceType;

    @AD(name = "admincount")
    private int adminCount;

    @AD(name = "primarygroupid")
    private int primaryGroupId;

}